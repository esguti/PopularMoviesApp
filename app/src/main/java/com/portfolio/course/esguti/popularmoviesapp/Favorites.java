package com.portfolio.course.esguti.popularmoviesapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by esguti on 04.02.16.
 *
 * This class is based on: http://stackoverflow.com/questions/9054193/how-to-use-sharedpreferences-to-save-more-than-one-values
 */
public abstract class Favorites {

    public static boolean addFavoriteItem(Activity activity,String favoriteItem){
        String key = activity.getString(R.string.save_favorite);
        String favoritesString = getStringFromPreferences(activity, null, key);

        if ( favoritesString != null && favoriteItem != null && !favoritesString.contains(favoriteItem) ) {
            // not found match => insert
            favoritesString = favoritesString + favoriteItem + ",";
            return putStringInPreferences(activity,favoritesString,key);
        }else return false;
    }

    //check if exists the key we want to insert
    public static boolean existFavorite(Activity activity, String favoriteItem){
        String key = activity.getString(R.string.save_favorite);
        String favoritesString = getStringFromPreferences(activity,null,key);

        return favoritesString.contains("," + favoriteItem + ",");
    }

    //delete favorite
    public static void delFavoriteItem(Activity activity, String favoriteItem){
        String key = activity.getString(R.string.save_favorite);
        String favoritesString = getStringFromPreferences(activity, null, key);

        if( favoritesString != null && favoriteItem != null ) {
            favoritesString = favoritesString.replace(favoriteItem + ",", "");
            putStringInPreferences(activity, favoritesString, key);
        }
    }


    public static String[] getFavoriteList(Activity activity){
        // get the favorites pref key
        String key = activity.getString(R.string.save_favorite);

        String favoriteList = getStringFromPreferences(activity,null,key);
        return convertStringToArray(favoriteList);
    }

    private static boolean putStringInPreferences(Activity activity,String nick,String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        // SharedPreferences sharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, nick);
        editor.commit();
        return true;
    }
    private static String getStringFromPreferences(Activity activity,String defaultValue,String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        // SharedPreferences sharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
        String temp = sharedPreferences.getString(key, defaultValue);
        //if does not exists => create
        if( temp == null && putStringInPreferences(activity, ",", key) )
            temp = sharedPreferences.getString(key, defaultValue);
        return temp;
    }

    private static String[] convertStringToArray(String str){
        String[] out;

        if(str != null){
            out = str.split(",",0);
            List<String> list = new ArrayList<String>(Arrays.asList(out));
            list.removeAll(Collections.singleton(""));
            out = list.toArray(new String[list.size()]);
        }else
            out = new String[0];

        return out;
    }
}
