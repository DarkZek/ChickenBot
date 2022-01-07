package com.darkzek.ChickenBot.Enums;

public enum GlobalEmote {
    CALL_ME ("\uD83E\uDD19");

    private final String value;

    GlobalEmote(String s) {
        value = s;
    }

    public boolean equalsName(String otherName) {
        return value.equals(otherName);
    }

    public String toString() {
        return this.value;
    }
}
