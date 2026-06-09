package com.tontineApp.tontine_manager.exception;

public class UnAuthorizedException extends RuntimeException{

    public UnAuthorizedException(String message) {
        super(message);
    }
}
