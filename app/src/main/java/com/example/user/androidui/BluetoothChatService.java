package com.example.user.androidui;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothChatService extends AppCompatActivity {
    private static final String TAG = "BluetoothChatService";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothConnectionService mBluetoothConnection;
    private BluetoothDevice mBTDevice;

    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    private ArrayAdapter<String> mConversationArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_chat);

        mConversationView = (ListView) findViewById(R.id.in);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (Button) findViewById(R.id.button_send);

        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView.setAdapter(mConversationArrayAdapter);

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

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        mBTDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mBluetoothConnection = new BluetoothConnectionService(this);
        mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String readMessage = intent.getStringExtra("theMessage");
            mConversationArrayAdapter.add(mBTDevice.getName() + ": " + readMessage);
        }
    };
}
