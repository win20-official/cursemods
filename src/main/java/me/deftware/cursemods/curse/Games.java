package me.deftware.cursemods.curse;

/**
 * @author Deftware
 */
public enum Games {

    Minecraft("432");

    private final String id;

    Games(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }

}
