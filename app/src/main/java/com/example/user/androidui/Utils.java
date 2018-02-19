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

    public static int[] getJSONArray(JSONObject jsonObject, String jsonKey){
        JSONArray jsonArray = null;
        int[] intArray = null;
        try{
            jsonArray = jsonObject.getJSONArray(jsonKey);
        } catch (JSONException e) {
           Log.e(TAG, "error: " + e.getMessage());
        }
        intArray = new int[jsonArray.length()];
        for(int i = 0; i < intArray.length; ++i)
            try{
            intArray[i] = jsonArray.getInt(i);
            } catch (JSONException e) {
                Log.e(TAG, "error: " + e.getMessage());
            }
        return intArray;
    }

    public static ArrayList<Character> getMapDescriptor(String mapDescriptorString){
        ArrayList<Character> mapDescriptor = new ArrayList<Character>();
        Log.d(TAG, "hexString length: " + mapDescriptorString.length());
        String binString = hexToBin(mapDescriptorString);
        Log.d(TAG, "binString length: " + binString.length());

        for(int i = 0; i < binString.length(); ++i)
            mapDescriptor.add(binString.charAt(i));


        return mapDescriptor;
    }

    public static ArrayList<Character> robotPositionChanged(ArrayList<Character> mapDescriptor, int[] robotPosition, int numCols){
        ArrayList<int[]> robotCoordinates = getRobotCoordinates(robotPosition, numCols);

        for(int i = 0 ; i < mapDescriptor.size(); ++i)
            if(mapDescriptor.get(i) == GridViewAdapter.ROBOT_BODY || mapDescriptor.get(i) == GridViewAdapter.ROBOT_HEAD)
                mapDescriptor.set(i, GridViewAdapter.FREE);

        for(int[] arrayIterator : robotCoordinates){
            if(arrayIterator[1] == 0)
                mapDescriptor.set(arrayIterator[0], GridViewAdapter.ROBOT_BODY);
            else
                mapDescriptor.set(arrayIterator[0], GridViewAdapter.ROBOT_HEAD);
        }

        return mapDescriptor;
    }

    private static String hexToBin(String hexString){
        String binString = "";
        for(int i = 0; i < hexString.length(); ++i){
            switch(hexString.charAt(i)){
                case '0':
                    binString += "0000";
                    break;
                case '1':
                    binString += "0001";
                    break;
                case '2':
                    binString += "0010";
                    break;
                case '3':
                    binString += "0011";
                    break;
                case '4':
                    binString += "0100";
                    break;
                case '5':
                    binString += "0101";
                    break;
                case '6':
                    binString += "0110";
                    break;
                case '7':
                    binString += "0111";
                    break;
                case '8':
                    binString += "1000";
                    break;
                case '9':
                    binString += "1001";
                    break;
                case 'a':
                    binString += "1010";
                    break;
                case 'b':
                    binString += "1011";
                    break;
                case 'c':
                    binString += "1100";
                    break;
                case 'd':
                    binString += "1101";
                    break;
                case 'e':
                    binString += "1110";
                    break;
                case 'f':
                    binString += "1111";
                    break;
            }
        }
        return binString;
    }

    private static ArrayList<int[]> getRobotCoordinates(int[] robotPosition, int numCols){
        int xCoord = robotPosition[0];
        int yCoord = robotPosition[1];
        int robotDirection = robotPosition[2];

        ArrayList<int[]> robotCoordinates = new ArrayList<int[]>();

        for(int i = yCoord; i < yCoord + 3; ++i)
            for(int j = xCoord; j < xCoord + 3; ++j){
                int cellIndex = getCellIndex(j, i, numCols);
                int[] intArray = new int[2];
                intArray[0] = cellIndex;
                if(j == xCoord + 1 && i == yCoord && robotDirection == 0)
                    intArray[1] = 1;
                else
                    if(j == xCoord && i == yCoord + 1 && robotDirection == 270)
                        intArray[1] = 1;
                    else
                        if(j == xCoord + 2 && i == yCoord + 1 && robotDirection == 90)
                            intArray[1] = 1;
                    else
                        if(j == xCoord + 1 && i == yCoord + 2 && robotDirection == 180)
                            intArray[1] = 1;
                        else
                            intArray[1] = 0;
                robotCoordinates.add(intArray);
            }
        return robotCoordinates;
    }
    private static int getCellIndex(int xCoord, int yCoord, int numCols){
        return (yCoord * numCols) + xCoord;
    }
}
