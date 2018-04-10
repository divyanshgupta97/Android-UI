package com.example.user.androidui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayDescriptorStringActivity extends Activity {

    private static final String TAG = "DisplayDescriptorStringActivity";

    private TextView mDescriptorStringOneTV;
    private TextView mDescriptorStringTwoTV;

    private String mDescriptorStringOne;
    private String mDescriptorStringTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_descriptor_strings_layout);
        setTitle("Select Coordinates");

        mDescriptorStringOneTV = (TextView) findViewById(R.id.descriptor_one_tv);
        mDescriptorStringTwoTV = (TextView) findViewById(R.id.descriptor_two_tv);

        mDescriptorStringOne = getIntent().getStringExtra("descriptorStringOne");
        mDescriptorStringTwo = getIntent().getStringExtra("descriptorStringTwo");

        mDescriptorStringOneTV.setText(mDescriptorStringOne);
        mDescriptorStringTwoTV.setText(mDescriptorStringTwo);
    }
}
