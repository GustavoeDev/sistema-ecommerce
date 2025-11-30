package br.com.gustavoedev.orders_service.exceptions;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String message) {
        super(message);
    }

    public CategoryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}