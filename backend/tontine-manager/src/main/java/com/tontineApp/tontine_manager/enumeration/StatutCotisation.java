package com.tontineApp.tontine_manager.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatutCotisation {
    COMPLET,
    EN_ATTENTE,
    PARTIEL;
    @JsonCreator
    public static StatutAdhesion from(String value){
        return StatutAdhesion.valueOf(value.toUpperCase());
    }

}
