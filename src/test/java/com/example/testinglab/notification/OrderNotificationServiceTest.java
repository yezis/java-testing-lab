package com.example.testinglab.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderNotificationServiceTest {

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private OrderNotificationService orderNotificationService;

    @Test
    public void shouldSendMessageWhenOrderCreated() {
        String orderId = "o-1001";
        orderNotificationService.notifyOrderCreated(orderId);

        // 验证messageSender是否有被调用过，因为void方法没有返回值，需要验证它的依赖有没有被正确调用
        verify(messageSender).send("Order created: " + orderId);
    }


    @Test
    public void shouldThrowExceptionWhenSendMessageFailed() {
        String orderId = "o-1001";
        doThrow(new IllegalStateException("send message failed")).when(messageSender).send("Order created: " + orderId);

        assertThatThrownBy(() -> orderNotificationService.notifyOrderCreated(orderId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("send message failed");

        verify(messageSender).send("Order created: " + orderId);
    }

}
