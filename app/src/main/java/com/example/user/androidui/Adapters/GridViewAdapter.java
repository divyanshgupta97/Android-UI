package com.example.user.androidui.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.androidui.R;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    // Member variables
    private Toast mToast;
    private Context mContext;
    private int mNumRows;
    private int mNumCols;
    private ArrayList<Character> mMapDescriptor;

    // Map constants
    private static final char UNDISCOVERED = '0';
    private static final char DISCOVERED  ='1';
    private static final char OBSTACLE = '2';
    private static final char ROBOT_HEAD = '3';
    private static final char ROBOT_BODY = '4';

    public GridViewAdapter(Context context, int numRows, int numCols, ArrayList<Character> mapDescriptor){
        mContext = context;
        mNumRows = numRows;
        mNumCols = numCols;
        mMapDescriptor = new ArrayList<Character>(mapDescriptor);
    }

    @Override
    public int getCount() {
        return mNumRows * mNumCols;
    }

    @Override
    public Object getItem(int index) {
        return mMapDescriptor.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View currentView, ViewGroup parentViewGroup) {

        if(currentView == null){
            currentView = LayoutInflater.from(mContext).inflate(R.layout.cell_layout,parentViewGroup, false);
        }

        char cellStatus = (char) getItem(index);
        setColor(currentView, cellStatus);

        return currentView;
    }

    private void setColor(View view, char cellStatus){

        switch(cellStatus){
            case DISCOVERED:
                view.setBackgroundResource(R.drawable.cell_item_1);
                break;
            case OBSTACLE:
                view.setBackgroundResource(R.drawable.cell_item_2);
                break;
            case ROBOT_HEAD:
                view.setBackgroundResource(R.drawable.cell_item_3);
                break;
            case ROBOT_BODY:
                view.setBackgroundResource(R.drawable.cell_item_4);
                break;
            default:
                view.setBackgroundResource(R.drawable.cell_item_0);
        }
    }

    public void refreshMap(ArrayList<Character> mapDescriptor){
        mMapDescriptor.clear();
        mMapDescriptor.addAll(mapDescriptor);
        notifyDataSetChanged();
    }
}
