package com.example.user.androidui;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.example.user.androidui.Adapters.GridViewAdapter;

import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private GridView mGridView;
    private GridViewAdapter mGridViewAdapter;
    private ArrayList<Character> mMapDescriptor;
    private GridView xAxis;
    private GridView yAxis;
    private static final int NUM_ROWS = 20;
    private static final int NUM_COLS = 15;

    /**
     * Intent request codes.
     */
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DEVICE_CONNECT_INSECURE = 1;
    private static final int REQUEST_DEVICE_CONNECT_SECURE = 2;


    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private BluetoothConnectionService mBluetoothConnection;

    private static final String MAP_DESCRIPTOR_STRING = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
                                                        "1 1 1 2 2 2 1 1 1 2 2 2 2 2 1 " +
                                                        "0 0 0 0 0 0 2 2 2 2 1 1 1 1 2 " +
                                                        "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                        "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                        "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
                                                        "1 1 1 2 2 2 1 1 1 2 2 2 2 2 1 " +
                                                        "0 0 0 0 0 0 2 2 2 2 1 1 1 1 2 " +
                                                        "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                        "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                        "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                        "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                        "4 4 4 1 1 1 1 2 2 2 2 1 1 1 1 " +
                                                        "4 4 3 1 1 1 1 1 1 2 2 2 2 2 2 " +
                                                        "4 4 4 1 1 1 1 1 2 2 2 2 0 0 0 " +
                                                        "0 0 0 0 0 0 2 2 2 2 1 1 1 1 2 " +
                                                        "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                        "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                        "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                        "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2";


    private static final String NEW_MAP_DESCRIPTOR_STRING = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
                                                            "1 1 1 2 2 2 1 1 1 2 2 2 2 2 1 " +
                                                            "0 0 0 0 0 0 2 2 2 2 1 1 1 1 2 " +
                                                            "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                            "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                            "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
                                                            "1 1 1 2 2 2 1 1 1 2 2 2 2 2 1 " +
                                                            "0 0 0 0 0 0 2 2 2 2 1 1 1 1 2 " +
                                                            "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                            "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                            "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                            "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                            "0 0 0 1 1 1 1 2 2 2 2 1 1 1 1 " +
                                                            "0 0 0 1 1 1 1 1 1 2 2 2 2 2 2 " +
                                                            "0 0 0 1 1 1 1 1 2 2 2 2 0 0 0 " +
                                                            "0 0 0 0 0 0 2 2 2 2 1 1 1 1 2 " +
                                                            "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2 " +
                                                            "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                            "1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 " +
                                                            "1 1 1 1 1 1 2 2 2 2 1 1 1 1 2";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        mGridView = (GridView) findViewById(R.id.maze);
//        xAxis = (GridView) findViewById(R.id.x_axis);
//        yAxis = (GridView) findViewById(R.id.y_axis);

        mMapDescriptor = getMapDescriptor(MAP_DESCRIPTOR_STRING);
        mGridViewAdapter = new GridViewAdapter(MainActivity.this, NUM_ROWS, NUM_COLS, mMapDescriptor);
//        GridAxisAdapter xAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_COLS, false);
//        GridAxisAdapter yAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_ROWS, true);
//
        mGridView.setNumColumns(NUM_COLS);
//        xAxis.setNumColumns(NUM_COLS);
//        yAxis.setNumColumns(1);
//
        mGridView.setAdapter(mGridViewAdapter);
//        xAxis.setAdapter(xAxisAdapter);
//        yAxis.setAdapter(yAxisAdapter);

        /**
         * Register receivers.
         */
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mIncomingMessageReceiver, new IntentFilter("incomingMessage"));
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIncomingMessageReceiver);
        Log.d(TAG, "All receivers successfully unregistered");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!mBTAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
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
            case R.id.action_bluetooth:
                Intent bluetoothConnectIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(bluetoothConnectIntent, REQUEST_DEVICE_CONNECT_INSECURE);
                return true;

            case R.id.action_chat:
                if(mBTDevice == null)
                    Toast.makeText(this, "No Bluetooth Device connected", Toast.LENGTH_SHORT).show();
                else{
                    Intent bluetoothChatIntent = new Intent(this, BluetoothChatService.class);
                    bluetoothChatIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mBTDevice);
                    startActivity(bluetoothChatIntent);
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent receivedIntent) {
        Log.d(TAG, "Inside onActivityResult");
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                /**
                 * Prompt user for enabling Bluetooth
                 */
                if(resultCode != Activity.RESULT_OK)
                    Toast.makeText(this, "Could not enable Bluetooth", Toast.LENGTH_SHORT).show();
                break;

            case REQUEST_DEVICE_CONNECT_INSECURE:
                /**
                 * DeviceListActivity returns with a device to connect
                 */
                Log.d(TAG, "Inside REQUEST_DEVICE_CONNECT_INSECURE");
                if(resultCode == Activity.RESULT_OK){
                    Log.d(TAG,"Connect Intent successfully received");
                    connectDevice(receivedIntent);
                }
                break;
        }
    }

    private void connectDevice(Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mBTDevice = bluetoothDevice;
//        mBluetoothConnection = new BluetoothConnectionService(this);
//        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    public void updateMap(View view){
        ArrayList<Character> newMapDescriptor = getMapDescriptor(NEW_MAP_DESCRIPTOR_STRING);
        mGridViewAdapter.refreshMap(newMapDescriptor);
    }

    private ArrayList<Character> getMapDescriptor(String mapDescriptorString){
        ArrayList<Character> mapDescriptor = new ArrayList<Character>();

        char[] mapDescriptorArray = mapDescriptorString.replace(" ", "").toCharArray();

        for(char descriptor : mapDescriptorArray){
            mapDescriptor.add(descriptor);
        }

        return mapDescriptor;
    }

//    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
//                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                //3 cases:
//                //case1: bonded already
//                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
//                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
//                    //inside BroadcastReceiver4
//                    mBTDevice = mDevice;
//                    mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
//                    startBTConnection(mBTDevice, MY_UUID_INSECURE);
//                }
//                //case2: creating a bone
//                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
//                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
//                }
//                //case3: breaking a bond
//                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
//                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
//                }
//            }
//        }
//    };

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device,uuid);
    }

    private BroadcastReceiver mIncomingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.d(TAG, "Message from remote device: " + text);
        }
    };

}
