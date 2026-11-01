package com.skytekx.elementarystaffs;

/**
 * The eight elements of the arrows and grenades. The arrow and grenade ids use {@link #id()},
 * the essence that crafts them uses {@link #essence()}, as in 2.0.0.
 */
public enum Element {
    DARK("dark", "darkness"),
    ELECTRIC("electric", "electric"),
    FIRE("fire", "fire"),
    ICE("ice", "ice"),
    LIFE("life", "life"),
    POISON("poison", "poison"),
    WATER("water", "water"),
    WIND("wind", "wind");

    private final String id;
    private final String essence;

    Element(String id, String essence) {
        this.id = id;
        this.essence = essence;
    }

    public String id() {
        return id;
    }

    public String essence() {
        return essence;
    }
}
