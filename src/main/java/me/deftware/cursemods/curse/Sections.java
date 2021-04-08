package me.deftware.cursemods.curse;

public enum Sections {

    Mods("6");

    private final String id;

    Sections(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }

}
