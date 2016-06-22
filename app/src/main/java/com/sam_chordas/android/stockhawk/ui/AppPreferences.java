package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by erikllerena on 6/20/16.
 */
public class AppPreferences {

    public static final String KEY_PREFS_SMS_BODY = "sms_body";
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName(); //  Name of the file -.xml
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;

    public AppPreferences(Context context) {
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public String getSmsBody(String MovieId) {
        return _sharedPrefs.getString(MovieId, "");
    }

    public int getIngBody() {
        return _sharedPrefs.getInt("selction", -1);
    }
    public void saveIntVal(int val) {
        _prefsEditor.putInt("selction", val);
        _prefsEditor.apply();
    }

    public void saveSmsBody(String MovieId, String val) {
        _prefsEditor.putString(MovieId, val);
        _prefsEditor.apply();
    }

    public void removePref(String MovieId) {
        _prefsEditor.remove(MovieId);
        _prefsEditor.apply();
    }

    public Map<String, ?> getAll() {
        return _sharedPrefs.getAll();
    }

}
