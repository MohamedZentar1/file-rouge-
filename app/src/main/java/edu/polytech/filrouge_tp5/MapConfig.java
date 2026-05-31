package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.content.SharedPreferences;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;

import java.io.File;

public final class MapConfig {

    private MapConfig() {
    }

    public static void configure(Context context) {
        Context app = context.getApplicationContext();
        IConfigurationProvider cfg = Configuration.getInstance();

        SharedPreferences prefs = app.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE);
        cfg.load(app, prefs);
        cfg.setUserAgentValue(app.getPackageName());

        File base = new File(app.getCacheDir(), "osmdroid");
        File tiles = new File(base, "tiles");
        base.mkdirs();
        tiles.mkdirs();
        cfg.setOsmdroidBasePath(base);
        cfg.setOsmdroidTileCache(tiles);
    }

    public static void applyTileSource(MapView map) {
        map.setTileSource(new XYTileSource(
                "Mapnik", 0, 19, 256, ".png",
                new String[]{
                        "https://a.tile.openstreetmap.org/",
                        "https://b.tile.openstreetmap.org/",
                        "https://c.tile.openstreetmap.org/"
                }));
    }
}
