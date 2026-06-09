package com.tontineApp.tontine_manager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<ErreurReponse>handleRessourceNotFoundException(RessourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErreurReponse(e.getMessage(),404));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurReponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErreurReponse(e.getMessage(),400));

    }

    @ExceptionHandler(UnAuthorizedException.class)
    public  ResponseEntity<ErreurReponse> handleNoAuthorizationFoundException(UnAuthorizedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErreurReponse(e.getMessage(),403));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurReponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErreurReponse("Erreur serveur interne",500)) ;
    }

}
