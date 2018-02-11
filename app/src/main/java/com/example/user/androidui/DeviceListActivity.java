package com.example.user.androidui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.androidui.Adapters.DeviceListAdapter;

import java.util.ArrayList;
import java.util.Set;


public class DeviceListActivity extends Activity {

    private static final String TAG = "DeviceListActivity";

    public static final String EXTRA_DEVICES_ADDRESS = "device_address";

    private BluetoothAdapter mBTAdapter;

    private ArrayList<BluetoothDevice> mBTDiscoveredDevices;

    private ArrayList<BluetoothDevice> mBTPairedDevices;

    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Setup the window.
         */

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_layout);

        /**
         * Set result cancelled in case the user backs out.
         */
        setResult(Activity.RESULT_CANCELED);

        /**
         * Initialize the button to perform device discovery
         */
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
            }
        });

        /**
         * Initialize the ArrayAdapters. One for already paired devices
         * and one for newly discovered devices.
         */

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        /**
         * Find and setup ListView for paired devices.
         */

        ListView pairedDevicesListView = (ListView) findViewById(R.id.paired_devices);
        pairedDevicesListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedDevicesListView.setOnItemClickListener(mDeviceClickListener);

        /**
         * Find and setup ListView for newly discovered devices.
         */

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        /**
         * Register a Broadcast Receiver when a device is discovered.
         */

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mBroadcastReceiver, intentFilter);

        /**
         * Register a Broadcast Receiver when device discovery has finished.
         */

        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);

        intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
        /**
         * Get the local BluetoothAdapter
         */

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Get currently paired devices.
         */

        mBTDiscoveredDevices = new ArrayList<BluetoothDevice>();
        mBTPairedDevices = new ArrayList<BluetoothDevice>(mBTAdapter.getBondedDevices());

        /**
         * If there are paired devices, then add them to the ArrayAdapter.
         */

        if(mBTPairedDevices.size() > 0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

            for(BluetoothDevice device : mBTPairedDevices)
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        } else {
            String noPairedDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noPairedDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /**
         * Cancel discovery.
         */
        if(mBTAdapter != null)
            mBTAdapter.cancelDiscovery();

        /**
         * Unregister the Broadcast Receiver.
         */

        this.unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Start device discovery with BluetoothAdapter
     */

    private void doDiscovery(){
        Log.d(TAG, "doDiscovery()");

        /**
         * Indicate scanning in the title.
         */

        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        /**
         * Turn on subtitle for new devices.
         */

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        /**
         * If already discovering, then stop.
         */

        if(mBTAdapter.isDiscovering())
            mBTAdapter.cancelDiscovery();

        /**
         * Start discovery for new devices.
         */

        mBTAdapter.startDiscovery();
    }

    /**
     * onClick Listeners for all devices in ListViews.
     */

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
            /**
             * Cancel discovery, as the connection is about to begin.
             */
            if(mBTAdapter.isDiscovering())
                mBTAdapter.cancelDiscovery();

            BluetoothDevice bluetoothDevice = mBTDiscoveredDevices.get(index);
            String deviceName = bluetoothDevice.getName();
            String deviceMACAddress = bluetoothDevice.getAddress();

            Log.d(TAG, deviceName + " clicked!!");

            /**
             * Create an Intent and include the MAC Address.
             */
            bluetoothDevice.createBond();
        }
    };

    /**
     * The Broadcast Receiver that listens for discovered devices and changes the title when
     * discovery is finished.
     */

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mBroadcastReceiver: onReceive()");
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(mBTPairedDevices.contains((BluetoothDevice) bluetoothDevice) || mBTDiscoveredDevices.contains((BluetoothDevice) bluetoothDevice))
                    return;

                mBTDiscoveredDevices.add(bluetoothDevice);
                Log.d(TAG, "onReceive: " + bluetoothDevice.getName() + ": " + bluetoothDevice.getAddress());
                mNewDevicesArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
//
//                if(bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED){
//                    mNewDevicesArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
//                    Log.d(TAG, "onReceive: " + bluetoothDevice.getName() + ": " + bluetoothDevice.getAddress());
//                }
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);

                if(mNewDevicesArrayAdapter.getCount() == 0){
                    String noNewDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noNewDevices);
                }
            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    Log.d(TAG, "Inside the fucking receiver");
                    sendConnectedDevice(bluetoothDevice);
                }
                //case2: creating a bone
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    private void sendConnectedDevice(BluetoothDevice bluetoothDevice){
        Log.d(TAG, "Inside sendConnectedDevice");
        Intent connectIntent = new Intent();
        connectIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);
        setResult(Activity.RESULT_OK, connectIntent);
        Log.d(TAG, "Before finishing the fucking activity");
        finish();
    }

}
