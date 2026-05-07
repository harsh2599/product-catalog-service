package com.example.exception;

public class EmptyReviewsException extends RuntimeException {

    public EmptyReviewsException() {
        super("At least one review is required to create a product");
    }
}
