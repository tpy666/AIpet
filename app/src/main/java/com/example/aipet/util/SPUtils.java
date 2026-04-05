package com.example.aipet.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SPUtils {
    private static final String SP_FILE_NAME = Constants.SP_APP_PREFS;
    private static SharedPreferences sharedPreferences;
    private static Gson gson = new Gson();

    private SPUtils() {
        // private constructor 防止实例化
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getApplicationContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getSharedPreferences(context).getString(key, defaultValue);
    }

    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(key);
        editor.apply();
    }

    public static boolean contains(Context context, String key) {
        return getSharedPreferences(context).contains(key);
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    public static <T> void putObject(Context context, String key, T object) {
        String json = gson.toJson(object);
        putString(context, key, json);
    }

    public static <T> T getObject(Context context, String key, Class<T> clazz) {
        String json = getString(context, key, null);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void putList(Context context, String key, List<T> list) {
        String json = gson.toJson(list);
        putString(context, key, json);
    }

    public static <T> List<T> getList(Context context, String key, Class<T> clazz) {
        String json = getString(context, key, null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static <T> void addItemToList(Context context, String key, T item, Class<T> clazz) {
        List<T> list = getList(context, key, clazz);
        list.add(item);
        putList(context, key, list);
    }

    public static <T> void removeItemFromList(Context context, String key, T item, Class<T> clazz) {
        List<T> list = getList(context, key, clazz);
        if (list.remove(item)) {
            putList(context, key, list);
        }
    }
}
