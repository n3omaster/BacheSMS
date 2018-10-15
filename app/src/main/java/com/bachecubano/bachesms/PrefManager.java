package com.bachecubano.bachesms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class PrefManager {

    private final SharedPreferences prefs;

    //Nauta Data
    public static String nautaUser;
    public static String nautaPass;

    public PrefManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getPrefInstance() {
        return prefs;
    }

    /**
     * Verify if the user has modified the Nauta settings
     *
     * @return bool
     */
    public boolean isNautaConfigured() {
        nautaUser = prefs.getString("pref_key_nauta_user", "pepe@nauta.cu");
        nautaPass = prefs.getString("pref_key_nauta_pass", "12345678");
        return !(nautaUser.equals("pepe@nauta.cu") || nautaPass.equals("12345678"));
    }
}
