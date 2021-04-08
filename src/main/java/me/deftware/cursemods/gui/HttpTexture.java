package me.deftware.cursemods.gui;

import lombok.Getter;
import me.deftware.cursemods.curse.CurseForgeAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * @author Deftware
 */
@Getter
public class HttpTexture extends AbstractTexture {

    private final Logger logger = LogManager.getLogger("HttpTexture");

    private final String url, slug;
    private final Identifier identifier;
    private File cacheFile;

    private boolean ready = false, textureAvailable = false;

    public HttpTexture(String url, String slug) {
        this.url = url;
        this.slug = slug;
        this.identifier = new Identifier("cursemods", "texture/" + slug);
    }

    private void onTextureAvailable() throws IOException {
        NativeImage image = NativeImage.read(new FileInputStream(this.cacheFile));
        TextureUtil.allocate(this.getGlId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
        this.textureAvailable = true;
    }

    private void download() {
        try {
            logger.debug("Downloading http texture");
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.url).openConnection(MinecraftClient.getInstance().getNetworkProxy());
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() / 100 != 2)
                throw new IOException("Failed to download texture");
            FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), this.cacheFile);
            httpURLConnection.disconnect();
            this.ready = true;
        } catch (Exception ex) {
            logger.error("Failed to download texture", ex);
        }
    }

    @Override
    public void bindTexture() {
        try {
            if (ready) {
                if (!textureAvailable)
                    this.onTextureAvailable();
                super.bindTexture();
            } else {
                this.load(MinecraftClient.getInstance().getResourceManager());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        if (this.cacheFile != null)
            return;
        this.cacheFile = CurseForgeAPI.PATH_PROVIDER.getAssetsDir().resolve("cursemods").resolve(slug + ".png").toFile();
        if (!this.cacheFile.exists())
            CompletableFuture.runAsync(this::download);
        else
            this.ready = true;
    }

}
