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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;


public class BluetoothPairingService extends Activity {

    /**
     * TAG for logging purposes.
     */
    private static final String TAG = "BluetoothPairingService";

    /**
     * Bluetooth Essentials.
     */
    private BluetoothAdapter mBTAdapter;

    /**
     * ArrayList for newly discovered devices.
     */
    private ArrayList<BluetoothDevice> mBTDiscoveredDevices;

    /**
     * ArrayAdapter for newly discovered devices.
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    /**
     * ArrayList for already paired/bonded devices.
     */
    private ArrayList<BluetoothDevice> mBTPairedDevices;

    /**
     * ArrayAdapter for already paired/bonded devices.
     */
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Setup the window.
         */

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bluetooth_pairing_layout);

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
         * Initialize the button to unpair already paired/bonded devices.
         */
        Button unpairButton = (Button) findViewById(R.id.unpair_devices);
        unpairButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                unpairDevices();
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
         * Register Broadcast Receiver when a device is discovered.
         */
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mBroadcastReceiver, intentFilter);

        /**
         * Register Broadcast Receiver when device discovery has finished.
         */
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);

        /**
         * Register Broadcast receiver when the bonding state of this device
         * changes.
         */
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(mBroadcastReceiver, intentFilter);

        /**
         * Get the local BluetoothAdapter
         */
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Initialize ArrayList for newly discovered devices and
         * retrieve all already paired devices (if any).
         */
        mBTDiscoveredDevices = new ArrayList<BluetoothDevice>();
        getPairedDevices();
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
     * Get already paired/bonded devices.
     */
    private void getPairedDevices(){
        mBTPairedDevices = new ArrayList<BluetoothDevice>(mBTAdapter.getBondedDevices());
        Log.d(TAG, "mBTPairedDevices: " + mBTPairedDevices.size());
        mPairedDevicesArrayAdapter.clear();
        /**
         * If there are paired devices, then add them to the ArrayAdapter.
         */
        if(mBTPairedDevices.size() > 0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

            for(BluetoothDevice device : mBTPairedDevices)
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        } else {
            findViewById(R.id.title_paired_devices).setVisibility(View.INVISIBLE);
            String noPairedDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noPairedDevices);
        }
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
         * If already discovering, then stopAllThreads.
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

            /**
             * Get the device MAC address, which is the last 17 chars in the TextView.
             */
            String deviceInfo = ((TextView) view).getText().toString();
            String deviceMACAddress = deviceInfo.substring(deviceInfo.length() - 17);

            Log.d(TAG, deviceInfo + " clicked");

            /**
             * Get Bluetooth Device from this MAC Address.
             */
            BluetoothDevice selectedDevice = mBTAdapter.getRemoteDevice(deviceMACAddress);

            /**
             * If the selected device is already paired, then we don't
             * need to bond again and we just return the paired device.
             */
            if(mBTPairedDevices.contains(selectedDevice)){
                /**
                 * Selected device has been paired. Send paired device to
                 * calling activity.
                 */
                sendPairedDevice(selectedDevice);
            } else{
                /**
                 * If the selected device is not already paired/bonded,
                 * then we need to pair/bond first.
                 */
                selectedDevice.createBond();
            }
        }
    };

    /**
     * The Broadcast Receiver that listens for discovered devices and bonding state changes.
     */

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mBroadcastReceiver: onReceive()");
            String action = intent.getAction();

            /**
             * New device discovered.
             */
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                /**
                 * If the new device is already in the list of paired devices or
                 * discovered devices, then we don't put in the discovered devices
                 * list.
                 */
                if(mBTPairedDevices.contains((BluetoothDevice) bluetoothDevice) || mBTDiscoveredDevices.contains((BluetoothDevice) bluetoothDevice))
                    return;

                /**
                 * Otherwise we add the newly discovered device in discovered devices list.
                 */

                mBTDiscoveredDevices.add(bluetoothDevice);
                Log.d(TAG, "onReceive: " + bluetoothDevice.getName() + ": " + bluetoothDevice.getAddress());
                mNewDevicesArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

            } else
            /**
             * Device discovery has finished.
             */
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    setProgressBarIndeterminateVisibility(false);
                    setTitle(R.string.select_device);

                    if(mNewDevicesArrayAdapter.getCount() == 0){
                        String noNewDevices = getResources().getText(R.string.none_found).toString();
                        mNewDevicesArrayAdapter.add(noNewDevices);
                    }
                } else
                /**
                 * Bonding status of this device has changed.
                 */
                    if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        /**
                         * Pairing/Bonding with selected device is complete.
                         */
                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                            Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                            /**
                             * Selected device has been paired. Send paired device to
                             * calling activity.
                             */
                            sendPairedDevice(bluetoothDevice);
                        }

                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                            Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                        }

                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                            Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                        }
                        getPairedDevices();
                    }
        }
    };

    /**
     * @param bluetoothDevice
     * Send paired device to calling activity.
     */
    private void sendPairedDevice(BluetoothDevice bluetoothDevice){
        Intent sendPairedDeviceIntent = new Intent();
        sendPairedDeviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);

        setResult(Activity.RESULT_OK, sendPairedDeviceIntent);
        finish();
    }

    /**
     * Unpair all paired devices.
     */
    private void unpairDevices(){
        Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
        for(BluetoothDevice pairedDevice : pairedDevices)
            unpairDevice(pairedDevice);
        Log.d(TAG, "Just before getPairedDevices() inside unpairDevices()");
    }

    /**
     * @param bluetoothDevice
     * Unpair a Bluetooth Device.
     */
    private void unpairDevice(BluetoothDevice bluetoothDevice){
        try{
            Method m = bluetoothDevice.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(bluetoothDevice, (Object[]) null);
        } catch (Exception e){
            Log.d(TAG, "Could not unpair: " + e.getMessage());
        }
    }
}
