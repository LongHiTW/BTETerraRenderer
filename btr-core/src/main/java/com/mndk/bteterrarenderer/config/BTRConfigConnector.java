package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.TileImageCacheManager;
import com.mndk.bteterrarenderer.tile.TileMapService;

public interface BTRConfigConnector {
    BTRConfigConnector INSTANCE = ImplFinder.search();

    void save();
    boolean isDoRender();               void setDoRender(boolean doRender);
    String getMapServiceCategory();     void setMapServiceCategory(String mapServiceCategory);
    String getMapServiceId();           void setMapServiceId(String mapServiceId);

    RenderSettingsConnector getRenderSettings();
    interface RenderSettingsConnector {
        double getXAlign();             void setXAlign(double xAlign);
        double getZAlign();             void setZAlign(double zAlign);
        boolean isLockNorth();          void setLockNorth(boolean lockNorth);
        double getYAxis();              void setYAxis(double yAxis);
        double getOpacity();            void setOpacity(double opacity);
        int getRadius();                void setRadius(int radius);
        double getYDiffLimit();         void setYDiffLimit(double yDiffLimit);
        int getRelativeZoomValue();     void setRelativeZoomValue(int relativeZoomValue);

        default void setRelativeZoom(int newZoom) {
            this.setRelativeZoomValue(newZoom);
            TileImageCacheManager.getInstance().deleteAllRenderQueues();
        }
    }

    UISettingsConnector getUiSettings();
    interface UISettingsConnector {
        SidebarSide getSidebarSide();   void setSidebarSide(SidebarSide side);
        int getSidebarWidth();          void setSidebarWidth(int sidebarWidth);
        double getSidebarOpacity();     void setSidebarOpacity(double sidebarOpacity);
    }

    default void toggleRender() {
        setDoRender(!isDoRender());
    }

    static TileMapService getTileMapService() {
        return Storage.TMS_ON_DISPLAY;
    }

    static void setTileMapService(String categoryName, String mapId) {
        Storage.TMS_ON_DISPLAY = TMSYamlLoader.INSTANCE.result.getItem(categoryName, mapId);
        INSTANCE.setMapServiceCategory(categoryName);
        INSTANCE.setMapServiceId(mapId);

        TileImageCacheManager.getInstance().deleteAllRenderQueues();
    }

    static boolean isRelativeZoomAvailable(int relativeZoom) {
        TileMapService tms = Storage.TMS_ON_DISPLAY;
        return tms != null && tms.isRelativeZoomAvailable(relativeZoom);
    }

    static void refreshTileMapService() {
        Storage.TMS_ON_DISPLAY = getCurrentTileMapService();
    }

    static TileMapService getCurrentTileMapService() {
        return TMSYamlLoader.INSTANCE.result.getItem(INSTANCE.getMapServiceCategory(), INSTANCE.getMapServiceId());
    }

    class Storage {
        public static TileMapService TMS_ON_DISPLAY;
    }
}
