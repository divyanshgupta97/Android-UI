package com.example.user.androidui;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.androidui.Adapters.GridAxisAdapter;
import com.example.user.androidui.Adapters.GridViewAdapter;


import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MainActivity";

    private GridView mGridView;
    private GridViewAdapter mGridViewAdapter;

    private static final int NUM_ROWS = 20;
    private static final int NUM_COLS = 15;

    private GridView xAxis;
    private GridAxisAdapter xAxisAdapter;

    private GridView yAxis;
    private GridAxisAdapter yAxisAdapter;

    private TextView connDeviceTV;
    private TextView robotStatusTV;

    private String f1String;
    private String f2String;

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DEVICE_CONNECT_INSECURE = 1;

    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ArrayList<Character> mMapDescriptor;

    private static final String MAP_DESCRIPTOR_STRING = "000000000000000000000000000000000000000000000000000000000000000000000000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.maze);
        xAxis = (GridView) findViewById(R.id.x_axis);
        yAxis = (GridView) findViewById(R.id.y_axis);

        mGridView.setNumColumns(NUM_COLS);
        xAxis.setNumColumns(NUM_COLS);
        yAxis.setNumColumns(1);

        mMapDescriptor = Utils.getMapDescriptor(MAP_DESCRIPTOR_STRING);

        mGridViewAdapter = new GridViewAdapter(MainActivity.this, NUM_ROWS, NUM_COLS, mMapDescriptor);
        xAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_COLS);
        yAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_ROWS);

        mGridView.setAdapter(mGridViewAdapter);
        xAxis.setAdapter(xAxisAdapter);
        yAxis.setAdapter(yAxisAdapter);

        connDeviceTV = (TextView) findViewById(R.id.connected_device);
        robotStatusTV = (TextView) findViewById(R.id.robot_status);

        setupPreferenceStrings();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mIncomingMessageReceiver, new IntentFilter("incomingMessage"));

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!mBTAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIncomingMessageReceiver);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(mBroadcastReceiver);

        Log.d(TAG, "All receivers successfully unregistered");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        switch(itemId){
            case R.id.action_bluetooth:{
                Intent bluetoothConnectIntent = new Intent(this, BluetoothPairingService.class);
                startActivityForResult(bluetoothConnectIntent, REQUEST_DEVICE_CONNECT_INSECURE);
                return true;
            }

            case R.id.action_chat:{
                if(mBTDevice == null){
                    Toast.makeText(this, "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
                } else {
                    Intent bluetoothChatIntent = new Intent(this, BluetoothChatService.class);
                    startActivity(bluetoothChatIntent);
                }
                return true;
            }

            case R.id.action_reconfigure:{
                Intent preferenceIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferenceIntent);
                return true;
            }

            default:{
                return super.onOptionsItemSelected(menuItem);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent receivedIntent) {
        switch (requestCode){
            case REQUEST_ENABLE_BT: {
                if(resultCode != Activity.RESULT_OK){
                    Toast.makeText(this, "Could not enable Bluetooth.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Bluetooth enabled.", Toast.LENGTH_SHORT).show();
                }

                break;
            }

            case REQUEST_DEVICE_CONNECT_INSECURE: {
                if(resultCode == Activity.RESULT_OK){
                    initializeNewBTDevice(receivedIntent);
                }
                break;
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.string_f1_key))){
            f1String = sharedPreferences.getString(getString(R.string.string_f1_key), getString(R.string.defaultValue_f1));
        }
        if(key.equals(getString(R.string.string_f2_key))){
            f2String = sharedPreferences.getString(getString(R.string.string_f2_key), getString(R.string.defaultValue_f2));
        }
    }

    private void setupPreferenceStrings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        f1String = sharedPreferences.getString(getString(R.string.string_f1_key), getString(R.string.defaultValue_f1));
        f2String = sharedPreferences.getString(getString(R.string.string_f2_key), getString(R.string.defaultValue_f2));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void initializeNewBTDevice(Intent intent) {
        mBTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        updateConnTV();
    }

    public void startBTConnection(View view){
        if(mBTDevice == null){
            Toast.makeText(this, "No paired device available.", Toast.LENGTH_SHORT).show();
        } else{
            Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
            ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService
                    .startClient(mBTDevice,MY_UUID_INSECURE, this);
        }
    }

    public void stopBTConnection(View view){
        if(mBTDevice == null){
            Toast.makeText(this, "No bluetooth device paired", Toast.LENGTH_SHORT);
        } else {
            ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.disconnectConn();
        }
    }

    public void makeDiscoverable(View view){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private BroadcastReceiver mIncomingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String incomingMessage = intent.getStringExtra("theMessage");
            if(incomingMessage.contains("status")){
                robotStatusTV.setText(incomingMessage);
            }
            if(incomingMessage.contains("grid")){
                JSONObject gridJSON = Utils.getJSONObject(incomingMessage);
                String gridString = Utils.getJSONString(gridJSON, "grid");
                mMapDescriptor = Utils.getMapDescriptor(gridString);
                mGridViewAdapter.refreshMap(mMapDescriptor);
            }
            if(incomingMessage.contains("robotPosition")){
                JSONObject robotJSON = Utils.getJSONObject(incomingMessage);
                int[] robotPosition = Utils.getJSONArray(robotJSON, "robotPosition");
                Log.d(TAG, "robotPosition: " + robotPosition[0] + ", " + robotPosition[1] + ", " + robotPosition[2]);
                mMapDescriptor = Utils.robotPositionChanged(mMapDescriptor, robotPosition, NUM_COLS);
                mGridViewAdapter.refreshMap(mMapDescriptor);
            }
            Log.d(TAG, "Message from remote device: " + incomingMessage);
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = bluetoothDevice;
                    updateConnTV();
                }

                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }

                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    if(bluetoothDevice.equals(mBTDevice)){
                        mBTDevice = null;
                        updateConnTV();
                    }
                }
            }
        }
    };

    public void sendForward(View view){
        String instruction = "f";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    public void sendLeft(View view){
        String instruction = "tl";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    public void sendRight(View view){
        String instruction = "tr";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    public void sendReverse(View view){
        String instruction = "r";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    public void sendF1(View view){
        byte[] bytes = f1String.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    public void sendF2(View view){
        byte[] bytes = f2String.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    public void askForGrid(View view){
        String instruction = "sendArena";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
    }

    private void updateConnTV(){
        if(mBTDevice != null){
            connDeviceTV.setText(mBTDevice.getName().toString());
            connDeviceTV.setVisibility(View.VISIBLE);
        }
        else{
            connDeviceTV.setText("");
            connDeviceTV.setVisibility(View.INVISIBLE);
        }
    }
}
