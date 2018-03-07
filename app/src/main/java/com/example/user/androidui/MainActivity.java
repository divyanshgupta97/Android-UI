package com.example.user.androidui;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.androidui.Adapters.GridAxisAdapter;
import com.example.user.androidui.Adapters.GridViewAdapter;


import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, SensorEventListener {

    private static final String TAG = "MainActivity";

    private static final int NUM_ROWS = 20;
    private static final int NUM_COLS = 15;

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DEVICE_CONNECT_INSECURE = 1;
    public static final int REQUEST_COORDINATES = 2;

    private Handler mHandler;

    private GridView mGridView;
    private GridView mXAxis;
    private GridView mYAxis;

    private GridViewAdapter mGridViewAdapter;
    private GridAxisAdapter mXAxisAdapter;
    private GridAxisAdapter mYAxisAdapter;

    private Button gridUpdateBtn;

    private String mF1String;
    private String mF2String;
    private String mWayPointXCoord;
    private String mWayPointYCoord;
    private String mStartCoordinateXCoord;
    private String mStartCoordinateYCoord;

    private TextView mConnDeviceTV;
    private TextView mRobotStatusTV;
    private TextView mWayPointXCoordTV;
    private TextView mWayPointYCoordTV;
    private TextView mStartCoordinateXCoordTV;
    private TextView mStartCoordinateYCoordTV;

    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private SensorManager sensorManager;
    private Sensor sensor;


    private Toast mToast;
    private Boolean isAuto = true;
    private TextView connectTV;
    private TextView autoTV;

    private ArrayList<Character> mMapDescriptor;
    private static final String MAP_DESCRIPTOR_STRING = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000434000000000000444000000000000444000000000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.maze);
        mXAxis = (GridView) findViewById(R.id.x_axis);
        mYAxis = (GridView) findViewById(R.id.y_axis);

        mGridView.setNumColumns(NUM_COLS);
        mXAxis.setNumColumns(NUM_COLS);
        mYAxis.setNumColumns(1);

        mMapDescriptor = Utils.getMapDescriptor(MAP_DESCRIPTOR_STRING);

        mGridViewAdapter = new GridViewAdapter(MainActivity.this, NUM_ROWS, NUM_COLS, mMapDescriptor);
        mXAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_COLS);
        mYAxisAdapter = new GridAxisAdapter(MainActivity.this, NUM_ROWS);

        mGridView.setAdapter(mGridViewAdapter);
        mXAxis.setAdapter(mXAxisAdapter);
        mYAxis.setAdapter(mYAxisAdapter);

        mConnDeviceTV = (TextView) findViewById(R.id.connected_device);
        mRobotStatusTV = (TextView) findViewById(R.id.robot_status);
        mWayPointXCoordTV = (TextView) findViewById(R.id.waypoint_x);
        mWayPointYCoordTV = (TextView) findViewById(R.id.waypoint_y);
        mStartCoordinateXCoordTV = (TextView) findViewById(R.id.start_coordinate_x);
        mStartCoordinateYCoordTV = (TextView) findViewById(R.id.start_coordinate_y);

        gridUpdateBtn = (Button) findViewById(R.id.maze_update);

        connectTV = (TextView) findViewById(R.id.tv_connect_btn);
        autoTV = (TextView) findViewById(R.id.tv_auto_btn);

        gridUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGridViewAdapter.refreshMap(mMapDescriptor);
            }
        });

//        TODO: Auto/Manual
//        gridUpdateToggleBtn = (ToggleButton) findViewById(R.id.update_maze_toggle_btn);
//        gridUpdateToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                if(isChecked){
//                    gridUpdateBtn.setVisibility(View.VISIBLE);
//                }
//                else {
//                    gridUpdateBtn.setVisibility(View.INVISIBLE);
//                }
//            }
//        });

        setupPreferenceStrings();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mIncomingMessageReceiver, new IntentFilter("incomingMessage"));

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
//
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private BroadcastReceiver mIncomingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String incomingMessage = intent.getStringExtra("theMessage");
            incomingMessage = incomingMessage.substring(4, incomingMessage.length());

            JSONObject messageJSON = Utils.getJSONObject(incomingMessage);
            String mazeString = Utils.getJSONString(messageJSON, "maze");
            mMapDescriptor = Utils.getMapDescriptor(mazeString);
            mGridViewAdapter.refreshMap(mMapDescriptor);
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = bluetoothDevice;
                    updateConnTV();
                }

                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }

                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    if (bluetoothDevice.equals(mBTDevice)) {
                        mBTDevice = null;
                        updateConnTV();
                    }
                }
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mHandler = new Handler();

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];

        Log.d(TAG, "onSensorChanged: X: " + x + ", onSensorChanged: Y: " + y);

//        if(mToast != null)
//            mToast.cancel();

        if (Math.abs(x) > Math.abs(y)) {
            if (x < -4) {
//                mToast = Toast.makeText(this, "Right", Toast.LENGTH_SHORT);
//                mToast.show();
                Log.d(TAG, "Right");
//                writeRight();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeRight();
                    }
                }, 5000);
            }
            if (x > 4) {
//                mToast = Toast.makeText(this, "Left", Toast.LENGTH_SHORT);
//                mToast.show();
                Log.d(TAG, "Left");
//                writeLeft();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeLeft();
                    }
                }, 5000);
            }
        } else {
            if (y < -2) {
//                mToast = Toast.makeText(this, "Forward", Toast.LENGTH_SHORT);
//                mToast.show();
                Log.d(TAG, "Forward");
//                writeForward();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeForward();
                    }
                }, 5000);
            }
            if (y > 4) {
//                mToast = Toast.makeText(this, "Reverse", Toast.LENGTH_SHORT);
//                mToast.show();
                Log.d(TAG, "Reverse");
//                writeReverse();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeReverse();
                    }
                }, 5000);
            }
        }
        if (x > (-4) && x < (4) && y > (-2) && y < (4)) {
//            mToast = Toast.makeText(this, "Stable", Toast.LENGTH_SHORT);
//            mToast.show();
            Log.d(TAG, "Stable");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBTAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, sensor, 1000000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister Sensor listener
//        sensorManager.unregisterListener(this);
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

        switch (itemId) {
            case R.id.action_chat: {
                if (mBTDevice == null) {
                    Toast.makeText(this, "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
                } else {
                    Intent bluetoothChatIntent = new Intent(this, BluetoothChatService.class);
                    startActivity(bluetoothChatIntent);
                }
                return true;
            }

            case R.id.action_reconfigure: {
                Intent preferenceIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferenceIntent);
                return true;
            }

            case R.id.action_send_f1: {
                sendF1();
                return true;
            }

            case R.id.action_send_f2: {
                sendF2();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(menuItem);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent receivedIntent) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Could not enable Bluetooth.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bluetooth enabled.", Toast.LENGTH_SHORT).show();
                }

                break;
            }

            case REQUEST_DEVICE_CONNECT_INSECURE: {
                if (resultCode == Activity.RESULT_OK) {

                    initializeNewBTDevice(receivedIntent);
                }
                break;
            }

            case REQUEST_COORDINATES: {
                if (resultCode == Activity.RESULT_OK) {
                    int xCoord = receivedIntent.getIntExtra("X", 0);
                    int yCoord = receivedIntent.getIntExtra("Y", 0);
                    String type = receivedIntent.getStringExtra("TYPE");

                    Log.d(TAG, "X: " + xCoord + ", Y: " + yCoord + ", Type: " + type);

                    if (type.equals("wayPoint")) {
                        mWayPointXCoord = Integer.toString(xCoord);
                        mWayPointYCoord = Integer.toString(yCoord);
                        ;
                        sendWayPoint(xCoord, yCoord);
                        updateWayPointTV();
                    } else {
                        mStartCoordinateXCoord = Integer.toString(xCoord);
                        mStartCoordinateYCoord = Integer.toString(yCoord);
                        sendStartCoordinates(xCoord, yCoord);
                        updateStartCoordinatesTV();
                    }
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.string_f1_key))) {
            mF1String = sharedPreferences.getString(getString(R.string.string_f1_key), getString(R.string.defaultValue_f1));
        }
        if (key.equals(getString(R.string.string_f2_key))) {
            mF2String = sharedPreferences.getString(getString(R.string.string_f2_key), getString(R.string.defaultValue_f2));
        }
    }

    private void setupPreferenceStrings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mF1String = sharedPreferences.getString(getString(R.string.string_f1_key), getString(R.string.defaultValue_f1));
        mF2String = sharedPreferences.getString(getString(R.string.string_f2_key), getString(R.string.defaultValue_f2));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void initializeNewBTDevice(Intent intent) {
        mBTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        startBTConnection(mBTDevice);
        updateConnTV();
    }

    public void stopBTConnection() {
        if (mBTDevice == null) {
            Toast.makeText(this, "No Bluetooth Device Paired", Toast.LENGTH_SHORT);
        } else {
            ((BluetoothDelegate) this.getApplicationContext()).appBluetoothConnectionService.disconnectConn();
        }
    }

    private void writeOnOutputStream(String message) {
        if (mBTDevice == null) {
            Toast.makeText(this, "No Bluetooth Device Connected", Toast.LENGTH_SHORT).show();
            return;
        } else {
            byte[] bytes = message.toString().getBytes(Charset.defaultCharset());
            ((BluetoothDelegate) this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
        }
    }

    private void writeForward() {
        writeOnOutputStream("f");
    }

    private void writeLeft() {
        writeOnOutputStream("tl");
    }

    public void writeRight() {
        writeOnOutputStream("tr");
    }

    public void writeReverse() {
        writeOnOutputStream("r");
    }

    public void sendForward() {
        writeForward();
    }

    public void sendLeft() {
        writeLeft();
    }

    public void sendRight() {
        writeRight();
    }

    public void sendReverse() {
        writeReverse();
    }

    public void sendF1() {
        writeOnOutputStream(mF1String);
    }

    public void sendF2() {
        writeOnOutputStream(mF2String);
    }

    public void startExploration() {
        writeOnOutputStream("startExploration");
    }

    public void startShortestPath() {
        writeOnOutputStream("startShortestPath");
    }

    private void sendStartCoordinates(int x, int y) {
        writeOnOutputStream("startCoordinates(" + Integer.toString(x) + ", " + Integer.toString(y) + ")");
    }

    private void sendWayPoint(int x, int y) {
        writeOnOutputStream("wayPoint(" + Integer.toString(x) + ", " + Integer.toString(y) + ")");
    }

    private void updateConnTV() {
        if (mBTDevice != null) {
            mConnDeviceTV.setText("Connected Device: " + mBTDevice.getName().toString());
            mConnDeviceTV.setVisibility(View.VISIBLE);
        } else {
            mConnDeviceTV.setText("Connected Device:");
            mConnDeviceTV.setVisibility(View.INVISIBLE);
        }
    }

    private void updateWayPointTV() {
        mWayPointXCoordTV.setText("X: " + mWayPointXCoord);
        mWayPointYCoordTV.setText("Y: " + mWayPointYCoord);
    }

    private void updateStartCoordinatesTV() {
        mStartCoordinateXCoordTV.setText("X: " + mStartCoordinateXCoord);
        mStartCoordinateYCoordTV.setText("Y: " + mStartCoordinateYCoord);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mToast != null)
                    mToast.cancel();
//                Toast.makeText(getApplicationContext(), "x = " + x + " y = " + y, Toast.LENGTH_SHORT).show();
                if (x > 110 && x < 160 && y > 1000 && y < 1050) {
                    sendForward();
//                    mToast = Toast.makeText(getApplicationContext(), "Forward", Toast.LENGTH_SHORT);
//                    mToast.show();
                }
                if (x > 110 && x < 160 && y > 1100 && y < 1160) {
                    sendReverse();
//                    mToast = Toast.makeText(getApplicationContext(), "Reverse", Toast.LENGTH_SHORT);
//                    mToast.show();
                }
                if (x > 55 && x < 100 && y > 1050 && y < 1100) {
                    sendLeft();
//                    mToast = Toast.makeText(getApplicationContext(), "Left", Toast.LENGTH_SHORT);
//                    mToast.show();
                }
                if (x > 160 && x < 220 && y > 1050 && y < 1100) {
                    sendRight();
//                    mToast = Toast.makeText(getApplicationContext(), "Right", Toast.LENGTH_SHORT);
//                    mToast.show();
                }
                if (x > 300 && x < 375 && y > 1120 && y < 1170) {
                    //TODO: Connect Bluetooth
                    if (mBTDevice == null) {
                        Intent bluetoothConnectIntent = new Intent(MainActivity.this, BluetoothPairingService.class);
                        startActivityForResult(bluetoothConnectIntent, REQUEST_DEVICE_CONNECT_INSECURE);
                        connectTV.setText(R.string.disconnect);
                    } else {
                        stopBTConnection();
                        connectTV.setText(R.string.connect);
                    }
                    return true;
                }
                if (x > 420 && x < 490 && y > 1120 && y < 1170) {
                    if (isAuto) {
                        isAuto = false;
                        gridUpdateBtn.setEnabled(false);
                        autoTV.setText(R.string.manual);

                    } else {
                        isAuto = true;
                        gridUpdateBtn.setEnabled(true);
                        autoTV.setText(R.string.auto);
                    }
                }
                if (x > 560 && x < 645 && y > 1050 && y < 1120) {
                    startShortestPath();
//                    mToast = Toast.makeText(getApplicationContext(), "Shortest", Toast.LENGTH_SHORT);
//                    mToast.show();
                }
                if (x > 670 && x < 760 && y > 1000 && y < 1080) {
                    startExploration();
//                    mToast = Toast.makeText(getApplicationContext(), "Explore", Toast.LENGTH_SHORT);
//                    mToast.show();
                }
                break;
        }
        return true;
    }

    public void startBTConnection(BluetoothDevice bluetoothDevice){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService
                .startClient(bluetoothDevice,MY_UUID_INSECURE, this);
    }
}
