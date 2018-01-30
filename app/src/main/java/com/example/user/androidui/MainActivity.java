package com.example.user.androidui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    private GridView mGridView;
    private GridView xAxis;
    private GridView yAxis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.maze);
        xAxis = (GridView) findViewById(R.id.x_axis);
        yAxis = (GridView) findViewById(R.id.y_axis);

        GridViewAdapter gridViewAdapter = new GridViewAdapter(MainActivity.this, 300);
        GridAxisAdapter xAxisAdapter = new GridAxisAdapter(MainActivity.this, 15, false);
        GridAxisAdapter yAxisAdapter = new GridAxisAdapter(MainActivity.this, 20, true);

        mGridView.setNumColumns(15);
        xAxis.setNumColumns(15);
        yAxis.setNumColumns(1);

        mGridView.setAdapter(gridViewAdapter);
        xAxis.setAdapter(xAxisAdapter);
        yAxis.setAdapter(yAxisAdapter);
    }
}
