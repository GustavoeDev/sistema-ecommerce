package br.com.gustavoedev.orders_service.modules.orders.enums;

public enum OrderStatus {
    WAITING_PAYMENT,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}