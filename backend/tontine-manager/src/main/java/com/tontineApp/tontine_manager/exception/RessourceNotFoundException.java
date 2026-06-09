package com.tontineApp.tontine_manager.exception;


import com.tontineApp.tontine_manager.model.Role;

public class RessourceNotFoundException extends RuntimeException    {

    public RessourceNotFoundException(String message) {
        super(message);
    }
}
