package com.example.user.androidui;

import java.util.ArrayList;

public class MapDescriptor {

    public static ArrayList<Character> getMapDescriptor(String mapDescriptorString){
        ArrayList<Character> mapDescriptor = new ArrayList<Character>();

        char[] mapDescriptorArray = mapDescriptorString.replace(" ", "").toCharArray();

        for(char descriptor : mapDescriptorArray){
            mapDescriptor.add(descriptor);
        }

        return mapDescriptor;
    }
}
