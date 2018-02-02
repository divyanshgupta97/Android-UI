package com.example.user.androidui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GridView mGridView;
    private GridView xAxis;
    private GridView yAxis;
    private static final int NUM_ROWS = 20;
    private static final int NUM_COLS = 15;

    private static final int REQUEST_ENABLED = 0;
    private static final int REQUEST_DISCOVERABLE = 0;

    private Button enableBluetoothBtn, disableBluetoothBtn, discoverableBlueToothBtn, listDevicesBtn;
    private ListView deviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.maze);
        xAxis = (GridView) findViewById(R.id.x_axis);
        yAxis = (GridView) findViewById(R.id.y_axis);

        GridViewAdapter gridViewAdapter = new GridViewAdapter(MainActivity.this, NUM_ROWS * NUM_COLS);
        GridAxisAdapter xAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_COLS, false);
        GridAxisAdapter yAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_ROWS, true);

        mGridView.setNumColumns(NUM_COLS);
        xAxis.setNumColumns(NUM_COLS);
        yAxis.setNumColumns(1);

        mGridView.setAdapter(gridViewAdapter);
        xAxis.setAdapter(xAxisAdapter);
        yAxis.setAdapter(yAxisAdapter);

//        enableBluetoothBtn = (Button) findViewById(R.id.btn_bluetooth_enable);
//        disableBluetoothBtn = (Button) findViewById(R.id.btn_bluetooth_disable);
//        discoverableBlueToothBtn = (Button) findViewById(R.id.btn_bluetooth_discoverable);
//        listDevicesBtn = (Button) findViewById(R.id.btn_bluetooth_list);
//        deviceListView = (ListView) findViewById(R.id.device_list);
//
//        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if(bluetoothAdapter == null){
//            Toast.makeText(this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//
//
//        enableBluetoothBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(intent, REQUEST_ENABLED);
//            }
//        });
//
//        disableBluetoothBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                bluetoothAdapter.disable();
//            }
//        });
//
//        discoverableBlueToothBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(!bluetoothAdapter.isDiscovering()){
//                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                    startActivityForResult(intent, REQUEST_DISCOVERABLE);
//                }
//            }
//        });
//
//        listDevicesBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//                ArrayList<String> devices = new ArrayList<String>();
//
//                for(BluetoothDevice device: pairedDevices){
//                    devices.add(device.getName());
//                }
//
//                ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.simple_list_item, devices);
//
//                deviceListView.setAdapter(arrayAdapter);
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if(itemId == R.id.action_bluetooth){
            Intent bluetoothActivityIntent = new Intent(this, BluetoothConnectionTestActivity.class);
            startActivity(bluetoothActivityIntent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
