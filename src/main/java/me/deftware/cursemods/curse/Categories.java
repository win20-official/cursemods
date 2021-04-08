package me.deftware.cursemods.curse;

public enum Categories {

    Fabric("4780");

    private final String id;

    Categories(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }

}

