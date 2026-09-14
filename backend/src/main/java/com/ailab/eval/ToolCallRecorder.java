package com.ailab.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Records synchronous tool calls so end-to-end evals can inspect the agent trace. */
@Component
public class ToolCallRecorder {

    private final ThreadLocal<List<ToolCall>> calls = ThreadLocal.withInitial(ArrayList::new);

    public void clear() {
        calls.get().clear();
    }

    public void record(String toolName, Map<String, String> arguments) {
        calls.get().add(new ToolCall(toolName, Map.copyOf(arguments)));
    }

    public List<ToolCall> calls() {
        return List.copyOf(calls.get());
    }

    public void remove() {
        calls.remove();
    }

    public record ToolCall(String name, Map<String, String> arguments) {
    }
}
