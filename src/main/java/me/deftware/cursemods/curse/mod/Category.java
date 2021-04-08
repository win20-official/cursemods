package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * @author Deftware
 */
@Getter
public class Category {

    @SerializedName("categoryId")
    private int categoryId;

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("parentId")
    private int parentId;

    @SerializedName("rootId")
    private int rootId;

    @SerializedName("projectId")
    private int projectId;

    @SerializedName("avatarId")
    private int avatarId;

    @SerializedName("gameId")
    private int gameId;

}
