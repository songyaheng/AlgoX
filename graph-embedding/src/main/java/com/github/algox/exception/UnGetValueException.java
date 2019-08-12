package com.github.algox.exception;

public class UnGetValueException extends RuntimeException{
    public UnGetValueException() {
        super("can not get value");
    }
}
