// com/tontineApp/tontine_manager/enumeration/ModePaiement.java
package com.tontineApp.tontine_manager.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ModePaiement {
    ORANGE_MONEY,
    WAVE,
    FREE_MONEY;

    @JsonCreator
    public static ModePaiement from(String value) {
        if (value == null) return null;
        return ModePaiement.valueOf(value.toUpperCase());
    }
}