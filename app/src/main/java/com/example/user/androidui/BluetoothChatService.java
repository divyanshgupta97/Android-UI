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

public class BluetoothChatService extends AppCompatActivity {
    private static final String TAG = "BluetoothChatService";

    private BluetoothDevice mBTDevice;

    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    private ArrayAdapter<String> mConversationArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_chat);

        mBTDevice = ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.getBTDevice();

        mConversationView = (ListView) findViewById(R.id.in);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (Button) findViewById(R.id.button_send);

        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView.setAdapter(mConversationArrayAdapter);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(mIncomingMessageReceiver, new IntentFilter("incomingMessage"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIncomingMessageReceiver);
    }

    private BroadcastReceiver mIncomingMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String incomingMessage = intent.getStringExtra("theMessage");
            mConversationArrayAdapter.add(mBTDevice.getName() + ": " + incomingMessage);
        }
    };

    public void sendMessage(View view){
        String writeMessage = mOutEditText.getText().toString();
        byte[] bytes = writeMessage.getBytes(Charset.defaultCharset());
        ((BluetoothDelegate)this.getApplicationContext()).appBluetoothConnectionService.write(bytes);
        mOutEditText.setText("");
        mConversationArrayAdapter.add("Me:  " + writeMessage);
    }
}
