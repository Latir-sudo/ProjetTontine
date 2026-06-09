package com.tontineApp.tontine_manager.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatutAdhesion {
    ACCEPTEE,
    REJETEE,
    ATTENTE;

    @JsonCreator
    public static StatutAdhesion from(String value){
        return StatutAdhesion.valueOf(value.toUpperCase());
    }

}
