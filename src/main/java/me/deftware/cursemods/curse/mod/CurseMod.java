package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import me.deftware.cursemods.curse.CurseForgeAPI;
import me.deftware.cursemods.curse.Serializer;
import me.deftware.cursemods.gui.HttpTexture;
import me.deftware.cursemods.web.HttpRequests;
import me.deftware.cursemods.web.HttpResponse;
import net.minecraft.SharedConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Deftware
 */
@Getter
public class CurseMod {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("authors")
    private List<Author> authors;

    /**
     * Attachments such as images
     */
    @SerializedName("attachments")
    private List<Attachment> attachments;

    @SerializedName("websiteUrl")
    private String websiteUrl;

    @SerializedName("gamePopularityRank")
    private int gamePopularityRank;

    @SerializedName("gameId")
    private int gameId;

    @SerializedName("summary")
    private String summary;

    @SerializedName("defaultFileId")
    private int defaultFileId;

    @SerializedName("downloadCount")
    private double downloadCount;

    @SerializedName("latestFiles")
    private List<ModFile> latestFiles;

    @SerializedName("categories")
    private List<Category> categories;

    @SerializedName("status")
    private int status;

    @SerializedName("primaryCategoryId")
    private int primaryCategoryId;

    @SerializedName("slug")
    private String slug;

    @SerializedName("gameVersionLatestFiles")
    private List<GameVersionData> gameVersionLatestFiles;

    @SerializedName("isFeatured")
    private boolean isFeatured;

    @SerializedName("popularityScore")
    private double popularityScore;

    @SerializedName("primaryLanguage")
    private String primaryLanguage;

    @SerializedName("dateCreated")
    private String dateCreated;

    @SerializedName("dateReleased")
    private String dateReleased;

    @SerializedName("dateModified")
    private String dateModified;

    @SerializedName("isAvailable")
    private boolean isAvailable;

    @SerializedName("isExperiemental")
    private boolean isExperiemental;

    private boolean cachedFiles = false;
    private Document cachedDescription;
    private HttpTexture texture;

    /**
     * @return The mod icon texture
     */
    public HttpTexture getTexture() {
        if (this.texture == null) {
            Optional<Attachment> attachment = this.getDefaultAttachment();
            attachment.ifPresent(a -> this.texture = new HttpTexture(a.getThumbnailUrl(), this.getSlug()));
        }
        return this.texture;
    }

    /**
     * @return All the mod authors, joined by a comma
     */
    public String getAuthorsString() {
        return this.authors
                .stream().map(Author::getName)
                .collect(Collectors.joining(", "));
    }

    /**
     * @return The optimal file to install for the current game version and mod loader
     */
    public Optional<ModFile> getOptimalFile() {
        this.fetchFiles();
        for (ModFile file : this.latestFiles) {
            String gameVersion = String.join(", ", file.getGameVersion());
            if (gameVersion.contains(SharedConstants.getGameVersion().getName())) {
                if (file.getGameVersion().size() > 1  && !gameVersion.contains(CurseForgeAPI.RUNTIME))
                    continue;
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    /**
     * @return The default mod attachment, which should be the mod icon png
     */
    public Optional<Attachment> getDefaultAttachment() {
        return this.attachments.stream()
                .filter(Attachment::isDefault)
                .findFirst();
    }

    /**
     * @param filter A search term
     * @return Returns true if this mod contains a given serch term
     */
    public boolean search(String filter) {
        String authors = this.authors.stream()
                .map(Author::getName)
                .collect(Collectors.joining())
                .toLowerCase();
        return authors.contains(filter) || this.getName().toLowerCase().contains(filter);
    }

    /**
     * Fetches all available files to download
     */
    public void fetchFiles() {
        if (this.cachedFiles)
            return;
        try {
            HttpResponse response = HttpRequests.get(new URI(
                    CurseForgeAPI.CURSE_API + "/" + id + "/files"
            ).toURL(), null, HttpRequests.BROWSER_AGENT);
            if (response.getStatusCode() != 200)
                throw new Error("Invalid status code returned from API!");
            this.latestFiles =  Arrays.asList(Serializer.getGson().fromJson(response.getResponse(), ModFile[].class));
            // Sort
            this.latestFiles.sort((a, b) -> b.getFileDate().compareTo(a.getFileDate()));
            this.cachedFiles = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return Returns a parsed jsoup document of the mod description html
     */
    public Optional<Document> getDescription() {
        if (cachedDescription != null)
            return Optional.of(cachedDescription);
        try {
            HttpResponse response = HttpRequests.get(new URI(
                    CurseForgeAPI.CURSE_API + "/" + id + "/description"
            ).toURL(), null, HttpRequests.BROWSER_AGENT);
            if (response.getStatusCode() != 200)
                throw new Error("Invalid status code returned from API!");
            return Optional.of(
                    this.cachedDescription = Jsoup.parse(response.getResponse())
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * @param id A curse mod id
     * @return The CurseMod instance of a given mod id
     */
    public static Optional<CurseMod> resolve(int id) {
        if (CurseForgeAPI.INSTANCE.getCachedMods().containsKey(id))
            return Optional.of(
                    CurseForgeAPI.INSTANCE.getCachedMods().get(id)
            );
        try {
            HttpResponse response = HttpRequests.get(new URI(
                    CurseForgeAPI.CURSE_API + "/" + id
            ).toURL(), null, HttpRequests.BROWSER_AGENT);
            if (response.getStatusCode() != 200)
                throw new Error("Invalid status code returned from API!");
            CurseMod mod = Serializer.getGson().fromJson(response.getResponse(), CurseMod.class);
            CurseForgeAPI.INSTANCE.getCachedMods().put(id, mod);
            return Optional.of(mod);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

}
