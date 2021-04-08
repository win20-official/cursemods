package me.deftware.cursemods.curse;

import lombok.Getter;
import lombok.SneakyThrows;
import me.deftware.cursemods.api.ModLoaderProvider;
import me.deftware.cursemods.api.ModPathProvider;
import me.deftware.cursemods.curse.mod.CurseMod;
import me.deftware.cursemods.web.HttpRequests;
import me.deftware.cursemods.web.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Deftware
 */
public class CurseForgeAPI implements Runnable {

    public static final CurseForgeAPI INSTANCE = new CurseForgeAPI();

    private final Logger logger = LogManager.getLogger("CurseForgeAPI");

    /**
     * CurseForge API url
     */
    public static final String CURSE_API = "https://addons-ecs.forgesvc.net/api/v2/addon";

    public static String RUNTIME = "Fabric";
    public static final ModPathProvider PATH_PROVIDER = new ModPathProvider() { };
    public static final ModLoaderProvider MOD_LOADER_PROVIDER = new ModLoaderProvider() { };


    /**
     * ID to mod map
     */
    @Getter
    private final Map<Integer, CurseMod> cachedMods = new HashMap<>();

    /**
     * The game to get mods in
     */
    @Getter
    private final Games game = Games.Minecraft;

    @Getter
    private boolean ready = false;

    @Getter
    private final List<Runnable> readyCallback = new ArrayList<>();

    @Override
    public void run() {
        this.ready = false;
        cachedMods.clear();
        this.loadDefault("1.16.4");
        this.loadDefault("1.16.5");
        logger.info("Cached {} mods", size());
        this.ready = true;
        readyCallback.forEach(Runnable::run);
        this.readyCallback.clear();
    }

    public void loadDefault(String gameVersion) {
        this.cache(this.search(Sections.Mods,
                "gameVersion", gameVersion,
                "pageSize", "500"
        ));
    }

    @SneakyThrows
    public void loadSearchQuery(String searchQuery) {
        this.cache(this.search(Sections.Mods,
                "searchFilter", URLEncoder.encode(searchQuery, "UTF-8")
        ));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void cache(Optional<List<CurseMod>> mods) {
        mods.ifPresent(curseMods -> curseMods.forEach(m -> {
            if (cachedMods.containsKey(m.getId()))
                logger.debug("Found duplicate mod {}", m.getSlug());
            if (MOD_LOADER_PROVIDER.isVersionSupported(m) && MOD_LOADER_PROVIDER.isLoaderSupported(m))
                cachedMods.putIfAbsent(m.getId(), m);
        }));
    }

    /**
     * @return The amount of cached mods
     */
    public int size() {
        return cachedMods.size();
    }

    public Stream<CurseMod> stream() {
        return cachedMods.values().stream();
    }

    /**
     * Example of query args:
     *      - gameVersion
     *      - searchFilter
     *      - index
     *      - pageSize
     *      - sort
     */
    public Optional<List<CurseMod>> search(Sections section, String... queryArgs) {
        try {
            StringBuilder builder = new StringBuilder();
            if (queryArgs.length > 0) {
                if (queryArgs.length % 2 != 0)
                    throw new Exception("Invalid query length, should be an even number!");
                for (int i = 1; i <= queryArgs.length; i++)
                    if (i % 2 == 0)
                        builder.append(
                                String.format("&%s=%s", queryArgs[i - 2], queryArgs[i - 1])
                        );
            }
            String query = builder.toString();
            HttpResponse response = HttpRequests.get(new URI(
                    String.format("%s/search?gameId=%s&sectionId=%s%s", CURSE_API, game, section, query)
            ).toURL(), null, HttpRequests.BROWSER_AGENT);
            if (response.getStatusCode() != 200)
                throw new Error("Invalid status code returned from API!");
            return Optional.of(
                    Arrays.asList(Serializer.getGson().fromJson(response.getResponse(), CurseMod[].class))
            );
        } catch (Exception ex) {
            logger.error(ex);
        }
        return Optional.empty();
    }

}
