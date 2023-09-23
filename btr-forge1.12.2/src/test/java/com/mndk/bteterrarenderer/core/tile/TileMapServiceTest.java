package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.loader.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;

// TODO: Add mixin for this test
public class TileMapServiceTest {
    private static final CategoryMap<TileMapService<?>> CATEGORY_MAP_DATA;

    public void givenYamlConfig_testJacksonReadability() {
        Assert.assertNotNull(CATEGORY_MAP_DATA.getCategory("Global"));
    }

    public void givenYamlConfig_testOsmUrl() throws OutOfProjectionBoundsException {
        FlatTileMapService osm = (FlatTileMapService) CATEGORY_MAP_DATA.getItem("Global", "osm");
        Assert.assertNotNull(osm);

        double longitude = 126.97683816936377, latitude = 37.57593302824052;
        int[] tileCoord = osm.getFlatTileProjection().geoCoordToTileCoord(longitude, latitude, 1);
        Assert.assertTrue(osm.getUrlFromTileCoordinate(tileCoord[0], tileCoord[1], 1).matches(
                "https://[abc]\\.tile\\.openstreetmap\\.org/19/447067/203014\\.png"
        ));
    }

    public void givenYamlConfig_testCategory() {
        CategoryMap.Wrapper<TileMapService<?>> osm = CATEGORY_MAP_DATA.getItemWrapper("Global", "osm");

        Assert.assertEquals("Global", osm.getParentCategory().getName());
        Assert.assertEquals("default", osm.getSource());
    }

    static {
        try {
            BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererConstants.class);
            ConfigLoaders.loadAll(false);
            CATEGORY_MAP_DATA = TileMapServiceYamlLoader.INSTANCE.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
