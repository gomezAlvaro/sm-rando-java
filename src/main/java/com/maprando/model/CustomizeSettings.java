package com.maprando.model;

public class CustomizeSettings {
    public boolean transitionLetters = false;
    public ItemDotChange itemDotChange = ItemDotChange.FADE;

    public enum ItemDotChange {
        FADE, DISAPPEAR
    }
}
