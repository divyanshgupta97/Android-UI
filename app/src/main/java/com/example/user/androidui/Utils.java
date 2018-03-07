package com.example.user.androidui;

import android.util.Log;

import com.example.user.androidui.Adapters.GridViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Utils {

    private static final String TAG = "Utils";

    public static JSONObject getJSONObject(String jsonString){
        JSONObject jsonObject = null;
        try{
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e){
            Log.d(TAG, "error: " + e.getMessage());
        }
        return jsonObject;
    }

    public static String getJSONString(JSONObject jsonObject, String jsonKey){
        String jsonString = "";
        try{
            jsonString = jsonObject.getString(jsonKey);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + e.getMessage());
        }
        return jsonString;
    }

    public static ArrayList<Character> getMapDescriptor(String mapDescriptorString){
        ArrayList<Character> mapDescriptor = new ArrayList<Character>();
        for(int i = 0; i < mapDescriptorString.length(); ++i)
            mapDescriptor.add(mapDescriptorString.charAt(i));

        return mapDescriptor;
    }
}
