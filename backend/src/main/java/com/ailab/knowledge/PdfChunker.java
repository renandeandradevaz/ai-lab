package com.ailab.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PdfChunker {

    private static final Pattern NUMBERED_HEADING = Pattern.compile("^\\d+(?:\\.\\d+)*\\.\\s+.+$");

    private final int targetWords;
    private final int overlapWords;

    public PdfChunker(
            @Value("${ailab.knowledge.chunk-target-words:220}") int targetWords,
            @Value("${ailab.knowledge.chunk-overlap-words:35}") int overlapWords) {
        this.targetWords = Math.max(50, targetWords);
        this.overlapWords = Math.max(0, Math.min(overlapWords, this.targetWords / 3));
    }

    public List<PdfChunk> chunkPage(String pageText, int pageNumber) {
        List<PdfChunk> chunks = new ArrayList<>();
        List<String> currentWords = new ArrayList<>();
        String currentSection = null;

        String normalized = pageText.replace('\r', ' ').replaceAll("[ \\t]+", " ");
        String[] paragraphs = normalized.split("\\n\\s*\\n");
        for (String rawParagraph : paragraphs) {
            String paragraph = rawParagraph.replaceAll("\\s*\\n\\s*", " ").trim();
            if (paragraph.isBlank()) {
                continue;
            }

            String firstLine = rawParagraph.strip().split("\\R", 2)[0].trim();
            if (isHeading(firstLine)) {
                if (!currentWords.isEmpty()) {
                    chunks.add(new PdfChunk(pageNumber, currentSection, String.join(" ", currentWords)));
                    currentWords.clear();
                }
                currentSection = firstLine;
                paragraph = paragraph.substring(Math.min(paragraph.length(), firstLine.length())).trim();
            }
            if (paragraph.isBlank()) {
                continue;
            }

            List<String> words = words(paragraph);
            if (words.size() > targetWords) {
                if (!currentWords.isEmpty()) {
                    chunks.add(new PdfChunk(pageNumber, currentSection, String.join(" ", currentWords)));
                    currentWords.clear();
                }
                addLongParagraph(chunks, words, pageNumber, currentSection);
            } else if (currentWords.size() + words.size() > targetWords && !currentWords.isEmpty()) {
                chunks.add(new PdfChunk(pageNumber, currentSection, String.join(" ", currentWords)));
                List<String> overlap = tail(currentWords);
                currentWords.clear();
                currentWords.addAll(overlap);
                currentWords.addAll(words);
            } else {
                currentWords.addAll(words);
            }
        }

        if (!currentWords.isEmpty()) {
            chunks.add(new PdfChunk(pageNumber, currentSection, String.join(" ", currentWords)));
        }
        return chunks;
    }

    private void addLongParagraph(List<PdfChunk> chunks, List<String> words, int pageNumber, String section) {
        int step = Math.max(1, targetWords - overlapWords);
        for (int start = 0; start < words.size(); start += step) {
            int end = Math.min(words.size(), start + targetWords);
            chunks.add(new PdfChunk(pageNumber, section, String.join(" ", words.subList(start, end))));
            if (end == words.size()) {
                break;
            }
        }
    }

    private List<String> tail(List<String> words) {
        if (overlapWords == 0 || words.isEmpty()) {
            return List.of();
        }
        int start = Math.max(0, words.size() - overlapWords);
        return new ArrayList<>(words.subList(start, words.size()));
    }

    private static List<String> words(String text) {
        return Arrays.stream(text.split("\\s+")).filter(word -> !word.isBlank()).toList();
    }

    private static boolean isHeading(String line) {
        return NUMBERED_HEADING.matcher(line).matches() && !line.endsWith(".")
                || (line.length() >= 5 && line.equals(line.toUpperCase()) && line.matches(".*[A-Z].*"));
    }
}
