package com.ailab.eval;

import java.util.List;
import java.util.Map;

public record ChatEvalCase(
        String name,
        String input,
        String userId,
        String expectedTool,
        Map<String, String> expectedArguments,
        List<String> expectedFacts,
        List<String> forbiddenFacts) {
}
