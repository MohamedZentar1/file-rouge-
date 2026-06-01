package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.content.SharedPreferences;

public final class ProfilePrefs {

    private static final String FILE = "signalroute_profile";
    private final SharedPreferences sp;

    public ProfilePrefs(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public String getName() {
        return sp.getString("name", "Utilisateur SignalRoute");
    }

    public String getPoste() {
        return sp.getString("poste", "Citoyen");
    }

    public String getPhotoPath() {
        return sp.getString("photo", null);
    }

    public boolean isLoggedIn() {
        return sp.getBoolean("logged", true);
    }

    public boolean isNotifEnabled() {
        return sp.getBoolean("notif", true);
    }

    public boolean isAnonymous() {
        return sp.getBoolean("anon", false);
    }

    public boolean isMapVisible() {
        return sp.getBoolean("mapvis", true);
    }

    public void saveProfile(String name, String poste) {
        sp.edit().putString("name", name).putString("poste", poste).apply();
    }

    public void setPhotoPath(String path) {
        sp.edit().putString("photo", path).apply();
    }

    public void setLoggedIn(boolean value) {
        sp.edit().putBoolean("logged", value).apply();
    }

    public void setNotifEnabled(boolean value) {
        sp.edit().putBoolean("notif", value).apply();
    }

    public void setAnonymous(boolean value) {
        sp.edit().putBoolean("anon", value).apply();
    }

    public void setMapVisible(boolean value) {
        sp.edit().putBoolean("mapvis", value).apply();
    }
}
