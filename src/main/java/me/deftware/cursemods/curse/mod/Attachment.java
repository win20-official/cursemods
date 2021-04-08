package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * @author Deftware
 */
@Getter
public class Attachment {

    @SerializedName("id")
    private int id;

    @SerializedName("projectId")
    private int projectId;

    @SerializedName("description")
    private String description;

    @SerializedName("isDefault")
    private boolean isDefault;

    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    @SerializedName("title")
    private String title;

    @SerializedName("url")
    private String url;

    @SerializedName("status")
    private int status;

}
