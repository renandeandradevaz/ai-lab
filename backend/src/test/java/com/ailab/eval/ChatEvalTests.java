package com.ailab.eval;

import static org.assertj.core.api.Assertions.assertThat;

import com.ailab.chat.ConversationChatService;
import com.ailab.conversation.ConversationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("eval")
@SpringBootTest
class ChatEvalTests {

    private static final String DATASET = "/evals/chat-cases.json";
    private static final Pattern POSITIVE_DELIVERED = Pattern.compile(
            "(?i)(?<!not )(?<!não )(?<!nao )\\b(delivered|entregue)\\b");
    private static final Map<String, List<String>> FACT_ALIASES = Map.of(
            "delayed", List.of("delayed", "atrasado", "atrasada", "atraso"));

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConversationChatService chatService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ToolCallRecorder toolCallRecorder;

    @Test
    void evaluatesNaturalLanguageQuestionsWithOllama() throws IOException {
        List<ChatEvalCase> cases = loadCases();
        List<String> failures = new ArrayList<>();
        int runs = Integer.getInteger("eval.runs", 1);
        int totalRuns = cases.size() * runs;
        int passed = 0;

        for (ChatEvalCase evalCase : cases) {
            for (int run = 1; run <= runs; run++) {
                String conversationId = UUID.randomUUID().toString();
                try {
                    toolCallRecorder.clear();
                    var result = chatService.chat(evalCase.userId(), conversationId, evalCase.input());
                    String failure = evaluate(evalCase, result.message(), toolCallRecorder.calls());
                    if (failure == null) {
                        passed++;
                        System.out.printf("[PASS] %s run %d%n", evalCase.name(), run);
                    } else {
                        failures.add(evalCase.name() + " run " + run + ": " + failure);
                        System.out.printf("[FAIL] %s run %d: %s%n", evalCase.name(), run, failure);
                    }
                    System.out.printf("  tool calls: %s%n  response: %s%n",
                            toolCallRecorder.calls(), result.message());
                } catch (RuntimeException exception) {
                    failures.add(evalCase.name() + " run " + run + ": " + exception.getMessage());
                    System.out.printf("[ERROR] %s run %d: %s%n", evalCase.name(), run, exception);
                } finally {
                    conversationRepository.deleteById(conversationId);
                    toolCallRecorder.clear();
                }
            }
        }

        System.out.printf("Eval summary: %d/%d passed (%.1f%%)%n", passed, totalRuns,
                totalRuns == 0 ? 100.0 : passed * 100.0 / totalRuns);
        assertThat(failures).as("Ollama eval failures").isEmpty();
    }

    private List<ChatEvalCase> loadCases() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(DATASET)) {
            assertThat(input).as("eval dataset %s", DATASET).isNotNull();
            return objectMapper.readValue(input, new TypeReference<>() {
            });
        }
    }

    private String evaluate(ChatEvalCase evalCase, String response, List<ToolCallRecorder.ToolCall> calls) {
        if (calls.size() != 1) {
            return "expected one tool call, got " + calls;
        }

        ToolCallRecorder.ToolCall call = calls.getFirst();
        if (!evalCase.expectedTool().equals(call.name())) {
            return "expected tool " + evalCase.expectedTool() + ", got " + call.name();
        }
        if (!evalCase.expectedArguments().equals(call.arguments())) {
            return "expected arguments " + evalCase.expectedArguments() + ", got " + call.arguments();
        }
        if (response == null || response.isBlank()) {
            return "response was empty";
        }

        for (String fact : evalCase.expectedFacts()) {
            if (!containsFact(response, fact)) {
                return "response did not contain expected fact: " + fact;
            }
        }
        for (String forbiddenFact : evalCase.forbiddenFacts()) {
            if (containsForbiddenFact(response, forbiddenFact)) {
                return "response contained forbidden fact: " + forbiddenFact;
            }
        }
        return null;
    }

    private boolean containsFact(String response, String fact) {
        String normalizedResponse = response.toLowerCase(Locale.ROOT);
        String normalizedFact = fact.toLowerCase(Locale.ROOT);
        if (normalizedResponse.contains(normalizedFact)) {
            return true;
        }
        if (FACT_ALIASES.getOrDefault(normalizedFact, List.of()).stream()
                .anyMatch(normalizedResponse::contains)) {
            return true;
        }
        if (!fact.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        LocalDate date = LocalDate.parse(fact);
        return normalizedResponse.contains(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                || normalizedResponse.contains(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))
                || normalizedResponse.contains(date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)).toLowerCase(Locale.ROOT))
                || normalizedResponse.contains(date.format(DateTimeFormatter.ofPattern(
                        "d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))).toLowerCase(Locale.ROOT));
    }

    private boolean containsForbiddenFact(String response, String fact) {
        if (fact.equalsIgnoreCase("DELIVERED")) {
            return POSITIVE_DELIVERED.matcher(response).find();
        }
        return response.toLowerCase(Locale.ROOT).contains(fact.toLowerCase(Locale.ROOT));
    }
}
