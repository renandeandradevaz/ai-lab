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
        String content = chatClient.prompt()
                .system("""
                        You are an AI Operations Copilot for a fictional e-commerce company.
                        Help customers understand their orders using the available tools.
                        Use a tool whenever the user asks about an order or delivery.
                        Never invent order data, status, dates, or prices.
                        When an order result includes delivery information, report both the order status and delivery status or estimated date.
                        Treat deliveryStatus as a status, never as a date; estimatedDeliveryDate is the date field.
                        If an order ID is missing, ask the customer for it.
                        Explain when a requested order or delivery cannot be found.
                        Only read order information in this version; do not claim to cancel, refund, or modify orders.
                        """)
                .user(message)
                .tools(orderTools)
                .call()
                .content();
        return content == null ? "" : content;
    }

    @Override
    public Flux<String> stream(String message) {
        return chatClient.prompt()
                .system("""
                        You are an AI Operations Copilot for a fictional e-commerce company.
                        Use the available read-only order tools for order and delivery questions.
                        Never invent data and ask for an order ID when it is missing.
                        """)
                .user(message)
                .tools(orderTools)
                .stream()
                .content();
    }
}
