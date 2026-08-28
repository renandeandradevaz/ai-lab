package com.ailab.operations;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import com.ailab.operations.delivery.DeliveryNotFoundException;
import com.ailab.operations.delivery.DeliveryEntity;
import com.ailab.operations.delivery.DeliveryService;
import com.ailab.operations.order.OrderEntity;
import com.ailab.operations.order.OrderNotFoundException;
import com.ailab.operations.order.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderTools {

    private static final Pattern ORDER_ID = Pattern.compile("ORD-[0-9]{4,}");
    private static final Pattern CUSTOMER_ID = Pattern.compile("CUST-[0-9]{4,}");

    private final OrderService orderService;
    private final DeliveryService deliveryService;

    public OrderTools(OrderService orderService, DeliveryService deliveryService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
    }

    @Tool(name = "getOrderStatus", description = "Get the current status and basic details of an order by its order ID.")
    @Transactional(readOnly = true)
    public OrderStatusResult getOrderStatus(
            @ToolParam(description = "Order ID, for example ORD-1001") String orderId) {
        if (!isOrderId(orderId)) {
            return OrderStatusResult.invalid("Order ID must match the format ORD-1001.");
        }

        try {
            OrderEntity order = orderService.getById(orderId);
            DeliveryEntity delivery = null;
            try {
                delivery = deliveryService.getByOrderId(orderId);
            } catch (DeliveryNotFoundException ignored) {
                // Cancelled orders may not have an active delivery.
            }
            return new OrderStatusResult(
                    true,
                    order.getId(),
                    order.getStatus().name(),
                    order.getCustomer().getId(),
                    order.getCreatedAt().toString(),
                    order.getTotalAmount().toPlainString(),
                    delivery == null ? null : delivery.getStatus().name(),
                    delivery == null ? null : delivery.getEstimatedDeliveryDate(),
                    null);
        } catch (OrderNotFoundException exception) {
            return OrderStatusResult.notFound("No order was found with ID " + orderId + ".");
        }
    }

    @Tool(name = "getDeliveryStatus", description = "Get delivery status and estimated delivery date for an order.")
    @Transactional(readOnly = true)
    public DeliveryStatusResult getDeliveryStatus(
            @ToolParam(description = "Order ID, for example ORD-1001") String orderId) {
        if (!isOrderId(orderId)) {
            return DeliveryStatusResult.invalid("Order ID must match the format ORD-1001.");
        }

        try {
            DeliveryEntity delivery = deliveryService.getByOrderId(orderId);
            return new DeliveryStatusResult(
                    true,
                    delivery.getId(),
                    orderId,
                    delivery.getStatus().name(),
                    delivery.getEstimatedDeliveryDate(),
                    delivery.getDeliveredAt() == null ? null : delivery.getDeliveredAt().toString(),
                    null);
        } catch (DeliveryNotFoundException exception) {
            return DeliveryStatusResult.notFound("No delivery was found for order " + orderId + ".");
        }
    }

    @Tool(name = "listCustomerOrders", description = "List the orders belonging to a customer by customer ID.")
    @Transactional(readOnly = true)
    public CustomerOrdersResult listCustomerOrders(
            @ToolParam(description = "Customer ID, for example CUST-1001") String customerId) {
        if (!isCustomerId(customerId)) {
            return new CustomerOrdersResult(false, customerId, List.of(), "Customer ID must match the format CUST-1001.");
        }

        List<OrderSummary> orders = orderService.getByCustomerId(customerId).stream()
                .map(order -> new OrderSummary(order.getId(), order.getStatus().name(), order.getCreatedAt().toString()))
                .toList();
        return new CustomerOrdersResult(true, customerId, orders, null);
    }

    private boolean isOrderId(String value) {
        return value != null && ORDER_ID.matcher(value).matches();
    }

    private boolean isCustomerId(String value) {
        return value != null && CUSTOMER_ID.matcher(value).matches();
    }

    public record OrderStatusResult(
            boolean found,
            String orderId,
            String status,
            String customerId,
            String createdAt,
            String totalAmount,
            String deliveryStatus,
            LocalDate estimatedDeliveryDate,
            String error) {

        static OrderStatusResult invalid(String error) {
            return new OrderStatusResult(false, null, null, null, null, null, null, null, error);
        }

        static OrderStatusResult notFound(String error) {
            return invalid(error);
        }
    }

    public record DeliveryStatusResult(
            boolean found,
            String deliveryId,
            String orderId,
            String status,
            LocalDate estimatedDeliveryDate,
            String deliveredAt,
            String error) {

        static DeliveryStatusResult invalid(String error) {
            return new DeliveryStatusResult(false, null, null, null, null, null, error);
        }

        static DeliveryStatusResult notFound(String error) {
            return invalid(error);
        }
    }

    public record CustomerOrdersResult(
            boolean found,
            String customerId,
            List<OrderSummary> orders,
            String error) {
    }

    public record OrderSummary(String orderId, String status, String createdAt) {
    }
}
