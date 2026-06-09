package com.tontineApp.tontine_manager.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErreurReponse {
    private String message;
    private int status;

}
