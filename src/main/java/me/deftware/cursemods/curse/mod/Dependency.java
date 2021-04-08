package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Optional;

/**
 * @author Deftware
 */
@Getter
public class Dependency {

    @SerializedName("id")
    private int id;

    @SerializedName("addonId")
    private int addonId;

    @SerializedName("type")
    private int type;

    @SerializedName("fileId")
    private int fileId;

    public Optional<CurseMod> resolve() {
        return CurseMod.resolve(this.addonId);
    }

}