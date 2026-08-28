package com.ailab.operations;

import static org.assertj.core.api.Assertions.assertThat;

import com.ailab.operations.OrderTools;
import com.ailab.operations.delivery.DeliveryRepository;
import com.ailab.operations.delivery.DeliveryStatus;
import com.ailab.operations.order.OrderRepository;
import com.ailab.operations.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OperationalDataTests {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderTools orderTools;

    @Test
    void loadsSeededOrderFromPostgres() {
        var order = orderRepository.findById("ORD-1003");

        assertThat(order).isPresent();
        assertThat(order.orElseThrow().getStatus()).isEqualTo(OrderStatus.DELAYED);
        assertThat(order.orElseThrow().getCustomer().getId()).isEqualTo("CUST-1002");
    }

    @Test
    void findsOrdersForCustomerInReverseCreationOrder() {
        var orders = orderRepository.findByCustomer_IdOrderByCreatedAtDesc("CUST-1001");

        assertThat(orders).extracting("id").containsExactly("ORD-1002", "ORD-1001");
    }

    @Test
    void findsDeliveryByOrderId() {
        var delivery = deliveryRepository.findByOrderId("ORD-1003");

        assertThat(delivery).isPresent();
        assertThat(delivery.orElseThrow().getStatus()).isEqualTo(DeliveryStatus.DELAYED);
    }

    @Test
    void exposesOrderDataThroughReadTool() {
        var result = orderTools.getOrderStatus("ORD-1003");

        assertThat(result.found()).isTrue();
        assertThat(result.status()).isEqualTo("DELAYED");
        assertThat(result.deliveryStatus()).isEqualTo("DELAYED");
        assertThat(result.estimatedDeliveryDate()).hasToString("2026-08-25");
        assertThat(result.error()).isNull();
    }

    @Test
    void rejectsInvalidOrderIdThroughReadTool() {
        var result = orderTools.getOrderStatus("not-an-order");

        assertThat(result.found()).isFalse();
        assertThat(result.error()).contains("ORD-1001");
    }
}
