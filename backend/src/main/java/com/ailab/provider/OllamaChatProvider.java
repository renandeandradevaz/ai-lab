package com.ailab.provider;

import com.ailab.operations.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class OllamaChatProvider implements ChatProvider {

    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public OllamaChatProvider(ChatClient.Builder chatClientBuilder, OrderTools orderTools) {
        this.chatClient = chatClientBuilder.build();
        this.orderTools = orderTools;
    }

    @Override
    public String chat(String message) {
        var request = chatClient.prompt()
                .system("""
                        You are an AI Operations Copilot for a fictional e-commerce company.
                        Help customers understand their orders using the available tools.
                        Use a tool whenever the user asks about an order or delivery.
                        Never invent order data, status, dates, or prices.
                        When an order result includes delivery information, report both the order status and delivery status or estimated date.
                        Treat deliveryStatus as a status, never as a date; estimatedDeliveryDate is the date field.
                         When reporting a date, preserve the complete date including the year exactly as returned by the tool.
                         If an order ID is missing, ask the customer for it.
                         Explain when a requested order or delivery cannot be found.
                         For policy questions, use the provided KNOWLEDGE CONTEXT as the only source of policy facts. Answer the exact question directly before adding optional explanation. If the question asks for a deadline or number, state the exact number. Cite the source filename and page in the answer. If the context is insufficient, say that the policy documents do not contain enough information.
                         Only read order information in this version; do not claim to cancel, refund, or modify orders.
                         """)
                .user(message);
        if (!message.contains("KNOWLEDGE CONTEXT")) {
            request = request.tools(orderTools);
        }
        String content = request.call().content();
        return content == null ? "" : content;
    }

    @Override
    public Flux<String> stream(String message) {
        var request = chatClient.prompt()
                .system("""
                        You are an AI Operations Copilot for a fictional e-commerce company.
                         Use the available read-only order tools for order and delivery questions.
                         Never invent data and ask for an order ID when it is missing.
                         When reporting a date, preserve the complete date including the year exactly as returned by the tool.
                         For policy questions, use only the provided KNOWLEDGE CONTEXT, answer directly, state exact numbers, and cite the source filename and page. Say when the context is insufficient.
                         """)
                .user(message);
        if (!message.contains("KNOWLEDGE CONTEXT")) {
            request = request.tools(orderTools);
        }
        return request.stream().content();
    }

    @Override
    public String summarize(String previousSummary, String conversation) {
        String prior = previousSummary == null || previousSummary.isBlank()
                ? "There is no previous summary."
                : previousSummary;
        return complete("""
                Summarize this customer support conversation for future turns.
                Preserve order IDs, customer IDs, statuses, dates, decisions, unresolved questions, and pending actions.
                Do not invent facts. Return only the summary in plain text.

                Previous summary:
                %s

                New conversation messages:
                %s
                """.formatted(prior, conversation));
    }

    @Override
    public String generateSubject(String previousSubject, String conversation) {
        return complete("""
                Create a concise subject for this customer support conversation.
                Use no more than 80 characters. Return only the subject, without quotes.
                Do not invent details that are not present in the conversation.

                Previous subject:
                %s

                Conversation:
                %s
                """.formatted(previousSubject, conversation));
    }

    private String complete(String instruction) {
        String content = chatClient.prompt()
                .system("You summarize and label conversations accurately and concisely.")
                .user(instruction)
                .call()
                .content();
        return content == null ? "" : content.trim();
    }
}
