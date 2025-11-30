package br.com.gustavoedev.orders_service.exceptions;

public class ProductInactiveException extends RuntimeException {
    public ProductInactiveException(String message) {
        super(message);
    }
}