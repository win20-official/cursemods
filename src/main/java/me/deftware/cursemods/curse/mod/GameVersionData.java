package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Optional;

/**
 * @author Deftware
 */
@Getter
public class GameVersionData {

    @SerializedName("gameVersion")
    private String gameVersion;

    @SerializedName("projectFileId")
    private int projectFileId;

    @SerializedName("projectFileName")
    private String projectFileName;

    @SerializedName("fileType")
    private int fileType;

    public Optional<ModFile> resolve(CurseMod mod) {
        return mod.getLatestFiles().stream()
                .filter(f -> f.getId() == this.projectFileId)
                .findFirst();
    }

}
