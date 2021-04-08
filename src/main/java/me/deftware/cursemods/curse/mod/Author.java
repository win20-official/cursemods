package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * @author Deftware
 */
@Getter
public class Author {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("projectId")
    private int projectId;

    @SerializedName("id")
    private int id;

    @SerializedName("projectTitleId")
    private int projectTitleId;

    @SerializedName("projectTitleTitle")
    private String projectTitleTitle;

    @SerializedName("userId")
    private int userId;

    @SerializedName("twitchId")
    private int twitchId;

}
