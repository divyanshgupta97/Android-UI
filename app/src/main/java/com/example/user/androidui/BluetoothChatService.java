package com.example.user.androidui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothChatService extends AppCompatActivity {
    /**
     * TAG for logging purposes.
     */
    private static final String TAG = "BluetoothChatService";

    /**
     * UUID for initiating a connection.
     */
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Bluetooth Essentials.
     */
    private BluetoothConnectionService mBluetoothConnection;
    private BluetoothDevice mBTDevice;

    /**
     * Chat UI Elements.
     */
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * ArrayAdapter for conversation threads.
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_chat);

        /**
         * Initialize UI Elements.
         */
        mConversationView = (ListView) findViewById(R.id.in);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (Button) findViewById(R.id.button_send);

        /**
         * Initialize conversation adapter.
         */
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView.setAdapter(mConversationArrayAdapter);

        /**
         * Set onClickListener for the send button to send string
         * to the Bluetooth Connected Thread for writing on output
         * stream.
         */
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String writeMessage = mOutEditText.getText().toString();
                byte[] bytes = writeMessage.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                mOutEditText.setText("");
                mConversationArrayAdapter.add("Me:  " + writeMessage);
            }
        });

        /**
         * Register Broadcast receiver to listen to incoming strings from the Bluetooth
         * Connected Thread's InputStream.
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        /**
         * Get the paired Bluetooth Device from the calling Intent.
         */
        mBTDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        /**
         * Initialize the BluetoothConnection Object to start listening for
         * incoming Bluetooth Sockets.
         */
        mBluetoothConnection = new BluetoothConnectionService(this);

        /**
         * Start a Bluetooth connection from this device to the paired device,
         * via Bluetooth Connect Thread.
         */
        mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * Unregister the Broadcast Receiver.
         */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == android.R.id.home){
            Log.d(TAG, "Before stopping the fucking connection");
            mBluetoothConnection.stopAllThreads();
            Intent sendPairedDeviceIntent = new Intent();
            sendPairedDeviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mBTDevice);
            setResult(Activity.RESULT_OK, sendPairedDeviceIntent);
            finish();
        }
            return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Before stopping the connection");
        mBluetoothConnection.stopAllThreads();
        Intent sendPairedDeviceIntent = new Intent();
        sendPairedDeviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mBTDevice);
        setResult(Activity.RESULT_OK, sendPairedDeviceIntent);
        finish();
    }

    /**
     * Broadcast Receiver to listen for strings from the Bluetooth Connected Thread's
     * InputStream.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String readMessage = intent.getStringExtra("theMessage");
            mConversationArrayAdapter.add(mBTDevice.getName() + ": " + readMessage);
        }
    };
}
