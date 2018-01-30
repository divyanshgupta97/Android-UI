package com.example.user.androidui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by user on 30/1/2018.
 */

public class GridAxisAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mItems;

    public GridAxisAdapter(Context context, int numCoordinates, boolean reverse){
        mContext = context;
        mItems = new int[numCoordinates];

        if(reverse)
            for(int i=0; i < numCoordinates; ++i)
                mItems[i] = (numCoordinates - 1) - i;
        else
            for(int i=0; i < numCoordinates; ++i)
                mItems[i] = i;
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public Object getItem(int index) {
        return mItems[index];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.cell_layout,viewGroup, false);
        }
        TextView numTextView = (TextView) view.findViewById(R.id.tv_cell);
        numTextView.setText(Integer.toString(mItems[index]));
//        view.setBackgroundResource(R.drawable.cell_item_border);
        return view;

    }
}
