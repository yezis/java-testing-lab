package com.example.testinglab.notification.application;

import com.example.testinglab.notification.domain.MessageSender;

public class OrderNotificationService {

    private final MessageSender messageSender;

    public OrderNotificationService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void notifyOrderCreated(String orderId) {
        messageSender.send("Order created: " + orderId);
    }
}
