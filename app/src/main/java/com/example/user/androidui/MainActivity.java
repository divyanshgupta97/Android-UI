package com.example.user.androidui;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.androidui.Adapters.GridViewAdapter;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    /**
     * TAG for logging purposes.
     */
    private static final String TAG = "MainActivity";

    /**
     * Declaring UI Elements.
     */
    private GridView mGridView;
    private GridViewAdapter mGridViewAdapter;
    private GridView xAxis;
    private GridView yAxis;
    private TextView connectedDeviceTextView;
    private static final int NUM_ROWS = 20;
    private static final int NUM_COLS = 15;

    /**
     * String values to be sent to Bluetooth Device, when
     * f1 and f2 are clicked.
     */
    private String f1String;
    private String f2String;

    /**
     * Intent Request Codes.
     */
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DEVICE_CONNECT_INSECURE = 1;
    private static final int REQUEST_DEVICE_CONNECT_SECURE = 2;
    private static final int REQUEST_DEVICE_CHAT = 3;

    /**
     * Bluetooth Essentials.
     */
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Map Descriptor String for
     */
    private ArrayList<Character> mMapDescriptor;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Get the SharedPreferences
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /**
         * Get Preference Strings for f1 and f2.
         */
        f1String = sharedPreferences.getString(getString(R.string.string_f1_key), getString(R.string.defaultValue_f1));
        f2String = sharedPreferences.getString(getString(R.string.string_f2_key), getString(R.string.defaultValue_f2));

        /**
         * The the local Bluetooth Adapter
         */
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Initialize the BluetoothConnection Service Object.
         */
        mBluetoothConnection = new BluetoothConnectionService(this);

        mGridView = (GridView) findViewById(R.id.maze);
//        xAxis = (GridView) findViewById(R.id.x_axis);
//        yAxis = (GridView) findViewById(R.id.y_axis);

        connectedDeviceTextView = (TextView) findViewById(R.id.connected_device);

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
         * Register Broadcast Receiver to listen for strings Bluetooth Connected Thread's
         * InputStream.
         */
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mIncomingMessageReceiver, new IntentFilter("incomingMessage"));

        /**
         * Register listener for changes in string prefereces.
         */
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /**
         * If Bluetooth is not enabled, prompt user for enabling Bluetooth.
         */
        if(!mBTAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * Unregister the Broadcast Receiver for incoming string messages.
         */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIncomingMessageReceiver);
        /**
         * Unregister the preference change listener
         */
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        Log.d(TAG, "All receivers successfully unregistered");
    }

    /**
     * Broadcast Receiver to listen for strings from Bluetooth Connected Thread's
     * InputStream.
     */
    private BroadcastReceiver mIncomingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.d(TAG, "Message from remote device: " + text);
        }
    };


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
                /**
                 * Start the BluetoothPairingService for getting back a
                 * paired Bluetooth Device.
                 */
                Intent bluetoothConnectIntent = new Intent(this, BluetoothPairingService.class);
                startActivityForResult(bluetoothConnectIntent, REQUEST_DEVICE_CONNECT_INSECURE);
                return true;

            case R.id.action_chat:
                /**
                 * If no paired Bluetooth Device found,
                 * then we cannot connect and begin the chat.
                 */
                if(mBTDevice == null)
                    Toast.makeText(this, "No Bluetooth Device connected", Toast.LENGTH_SHORT).show();
                /**
                 * We have a paired Bluetooth Device, so we can begin chat.
                 */
                else{
                    Intent bluetoothChatIntent = new Intent(this, BluetoothChatService.class);
                    bluetoothChatIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mBTDevice);
                    startActivityForResult(bluetoothChatIntent, REQUEST_DEVICE_CHAT);
                }
                return true;

            case R.id.action_reconfigure:
                /**
                 * Prepare Intent for the PreferencesActivity.
                 */
                Intent preferenceIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferenceIntent);

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
                 * Prompted user for enabling Bluetooth.
                 */
                if(resultCode != Activity.RESULT_OK)
                    Toast.makeText(this, "Could not enable Bluetooth", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Bluetooth enabled.", Toast.LENGTH_SHORT).show();
                break;

            case REQUEST_DEVICE_CONNECT_INSECURE:
                /**
                 * BluetoothPairingService returns with a paired device.
                 */
                Log.d(TAG, "Inside REQUEST_DEVICE_CONNECT_INSECURE");
                Log.d(TAG, "Before resultCode == Activity.RESULT_OK");
                if(resultCode == Activity.RESULT_OK){
                    Log.d(TAG, "Inside resultCode == Activity.RESULT_OK");
                    initializeBluetoothDevice(receivedIntent);
                }
                else{
                    Log.d(TAG, "inside else statement");
                    mBTDevice = null;
                }
                Log.d(TAG, "after resultCode == Activity.RESULT_OK");
                updateConnectedTextView();
                break;

            case REQUEST_DEVICE_CHAT:
                /**
                 * BluetoothChatService returns the Bluetooth Device we sent it.
                 */
                Log.d(TAG, "Inside onActivityResult: REQUEST_DEVICE_CHAT");
                if(resultCode == Activity.RESULT_OK){
                    Log.d(TAG, "Inside onActivityResult: REQUEST_DEVICE_CHAT: resultCode == Activity.RESULT_OK");
                    initializeBluetoothDevice(receivedIntent);
                }
                updateConnectedTextView();
                break;
        }
    }

    /**
     * @param sharedPreferences
     * @param key
     * Preference Change Listener to listen for change in string preferences.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.string_f1_key))){
            f1String = sharedPreferences.getString(getString(R.string.string_f1_key), getString(R.string.defaultValue_f1));
        }

        if(key.equals(getString(R.string.string_f2_key))){
            f2String = sharedPreferences.getString(getString(R.string.string_f2_key), getString(R.string.defaultValue_f2));
        }
    }

    /**
     * @param intent
     * Initialize mBTDevice with the paired device received from BluetoothPairingService.
     */
    private void initializeBluetoothDevice(Intent intent) {
        mBTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    }

    public void updateMap(View view){
        ArrayList<Character> newMapDescriptor = getMapDescriptor(NEW_MAP_DESCRIPTOR_STRING);
        mGridViewAdapter.refreshMap(newMapDescriptor);
    }

    /**
     * @param mapDescriptorString
     * @return
     * Takes in a Map Descriptor String and returns an
     * ArrayList of Characters.
     */
    private ArrayList<Character> getMapDescriptor(String mapDescriptorString){
        ArrayList<Character> mapDescriptor = new ArrayList<Character>();

        char[] mapDescriptorArray = mapDescriptorString.replace(" ", "").toCharArray();

        for(char descriptor : mapDescriptorArray){
            mapDescriptor.add(descriptor);
        }

        return mapDescriptor;
    }

    /**
     * @param device
     * @param uuid
     * Given that we have a paired device, we start
     * I/O with the remote Bluetooth Device.
     */
    private void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device,uuid);
    }

    /**
     * If we have received a paired Bluetooth Device,
     * then set the TextView to show the name of the
     * device. Else, we reset the TextView.
     */
    private void updateConnectedTextView(){
        if(mBTDevice != null){
            connectedDeviceTextView.setText(mBTDevice.getName().toString());
            connectedDeviceTextView.setVisibility(View.VISIBLE);
        }
        else{
            connectedDeviceTextView.setText("");
            connectedDeviceTextView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * @param view
     * Send instruction to move robot forward.
     */
    public void sendForward(View view){
        String instruction = "f";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    /**
     * @param view
     * Send instruction to make robot turn left.
     */
    public void sendLeft(View view){
        String instruction = "tl";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    /**
     * @param view
     * Send instruction to make robot turn right.
     */
    public void sendRight(View view){
        String instruction = "tr";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    /**
     * @param view
     * Send instruction to make the robot reverse.
     */
    public void sendReverse(View view){
        String instruction = "r";
        byte[] bytes = instruction.toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    /**
     * @param view
     * Start Bluetooth Connection after obtaining
     * a paired device.
     */
    public void startConnection(View view){
        if(mBTDevice == null)
            Toast.makeText(this, "No Paired device available", Toast.LENGTH_SHORT).show();
        else
            startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    /**
     * @param view
     * Stop ongoing Bluetooth connection, but keep
     * listening for incoming Bluetooth Sockets.
     */
    public void stopConnection(View view){
        mBluetoothConnection.stopThreadsButListen();
    }

    public void makeDiscoverable(View view){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /**
     * @param view
     * Send F1 String to the remote device.
     */
    public void sendF1(View view){
        byte[] bytes = f1String.toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    /**
     * @param view
     * Send F2 String to the remote device.
     */
    public void sendF2(View view){
        byte[] bytes = f2String.toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }
}
