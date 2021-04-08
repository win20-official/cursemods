package me.deftware.cursemods.curse.mod;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import me.deftware.cursemods.curse.CurseForgeAPI;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author Deftware
 */
@Getter
public class ModFile {

    @SerializedName("id")
    private int id;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("fileDate")
    private String fileDate;

    @SerializedName("fileLength")
    private int fileLength;

    @SerializedName("releaseType")
    private int releaseType;

    @SerializedName("fileStatus")
    private int fileStatus;

    @SerializedName("downloadUrl")
    private String downloadUrl;

    @SerializedName("isAvailable")
    private boolean isAvailable;

    @SerializedName("dependencies")
    private List<Dependency> dependencies;

    @SerializedName("gameVersion")
    private List<String> gameVersion;

    @SerializedName("isServerPack")
    private boolean isServerPack;

    public boolean download(CurseMod mod) {
        File file = CurseForgeAPI.PATH_PROVIDER.getModFile(mod);
        try {
            if (!file.exists()) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.downloadUrl).openConnection(MinecraftClient.getInstance().getNetworkProxy());
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(false);
                httpURLConnection.connect();
                if (httpURLConnection.getResponseCode() / 100 != 2)
                    throw new IOException("Failed to download texture");
                FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), file);
                httpURLConnection.disconnect();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
