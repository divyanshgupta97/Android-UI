package com.example.user.androidui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.icu.util.Output;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by divyanshgupta on 31/1/18.
 */

public class BluetoothConnectionService {
    private final String TAG = getClass().getSimpleName();

    private static final UUID UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final String NAME_INSEUCRE = "Mars Rover Insecure";

    private final BluetoothAdapter mBluetoothAdapter;
    private final Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mRaspberryPi;
    private UUID mRaspberryUUID;
    private ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a  server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */

    public class AcceptThread extends Thread {

        /**
         * The local server socket for listening to incoming connections.
         */
        private final BluetoothServerSocket mServerSocket;


        public AcceptThread(){
            BluetoothServerSocket tempSocket = null;
            /**
             * Create a new listening server socket.
             */
            try{
                tempSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSEUCRE, UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up server using " + UUID_INSECURE);
            } catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mServerSocket = tempSocket;
        }

        public void run(){
            Log.d(TAG, "AcceptThread Running");

            BluetoothSocket bluetoothSocket = null;

            /**
             * This is a blocking call and will only return on
             * successful connection or an exception.
             */
            Log.d(TAG, "run: RFCOM server socket start.....");

            try {
                bluetoothSocket = mServerSocket.accept();
                Log.d(TAG, "RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if(bluetoothSocket != null){
                connected(bluetoothSocket, mRaspberryPi);
            }

            Log.d(TAG, "End mAcceptThread");
        }

        public void cancel(){
            Log.d(TAG, "cancel: Cancelling mAcceptThread");
            try{
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Cannot close the AcceptThread Socket: " + e.getMessage());
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; it either succeeds or fails.
     */

    public class ConnectThread extends Thread {

        private BluetoothSocket mBluetoothSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: Started");

            mRaspberryPi = device;
            mRaspberryUUID = uuid;
        }

        public void run(){
            BluetoothSocket tempSocket = null;
            Log.d(TAG, "Run mConnectThread");

            /**
             * Get a bluetooth socket for a connection
             * with the given bluetooth device
             */
            try{
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + UUID_INSECURE);
                tempSocket = mRaspberryPi.createInsecureRfcommSocketToServiceRecord(mRaspberryUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket: " + e.getMessage());
            }

            mBluetoothSocket = tempSocket;

            /**
             * Now a connection attempt (either successful or unsuccessful)
             * has been made and so we stop discovering/listening for connections.
             */
            mBluetoothAdapter.cancelDiscovery();

            /**
             * This is a blocking call and will only return
             * on a successful connection or an exception
             */
            try {
                mBluetoothSocket.connect();
                Log.d(TAG, "mConnectThread: Connected to RaspberryPi");
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Unable to establish a connection with RaspberryPi: " + e.getMessage());
                try {
                    mBluetoothSocket.close();
                    Log.d(TAG, "ConnectThread: Socket Closed");
                } catch (IOException e1) {
                    Log.e(TAG, "ConnectThread: Unable to close socket: " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + UUID_INSECURE);
            }

            connected(mBluetoothSocket, mRaspberryPi);
        }

        public void cancel(){
            try{
                Log.d(TAG, "ConnectThread: Closing Client Socket");
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"ConnectThread: Unable to close socket: " + e.getMessage());
            }
        }
    }

    /**
     * Start the connection service. Specifically, start the AcceptThread to
     * begin a session in the listening (server) mode.
     */

    public synchronized void start(){
        Log.d(TAG, "start");
        /**
         * Cancel any thread attempting to make a connection
         */

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        /**
         * If the AcceptThread is null. Then we start a new one
         */

        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and waits for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other device.
     */

    public void startClient(BluetoothDevice bluetoothDevice, UUID uuid){
        Log.d(TAG, "startClient; Started");

        /**
         * initprogress Dialog
         */
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth",
                "Please Wait...", true);

        mConnectThread = new ConnectThread(bluetoothDevice, uuid);
        mConnectThread.start();
    }



    public class ConnectedThread extends Thread {
        private final BluetoothSocket mBluetoothSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket){
            Log.d(TAG, "ConnectedThread Starting");

            mBluetoothSocket = bluetoothSocket;
            InputStream tempInputStream = null;
            OutputStream tempOutputStream = null;

            /**
             * We dismiss the Progress Dialog Box when the connection has been
             * established.
             */
            mProgressDialog.dismiss();

            try{
                tempInputStream = mBluetoothSocket.getInputStream();
                tempOutputStream = mBluetoothSocket.getOutputStream();
            } catch(IOException e){
                Log.e(TAG, "ConnectedThread: Unable to get the I/O Streams: " + e.getMessage());
            }

            mInputStream = tempInputStream;
            mOutputStream = tempOutputStream;
        }

        public void run(){
            /**
             * Buffer Store for the stream
             */
            byte[] inputBuffer = new byte[1024];

            /**
             * Bytes returned from the read
             */
            int bytes;

            /**
             * Listen to the input stream indefinitely.
             */
            while(true){
                try {
                    bytes = mInputStream.read(inputBuffer);
                    String incomingMessage = new String(inputBuffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "ConnectedThread: Unable to read from the input buffer");
                    /**
                     * If there is a problem with the connection, then we
                     * break it.
                     */
                    break;
                }
            }
        }

        /**
         * Called from Main Activity to send message to remote device.
         */
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "ConnectedThread: Writing to output stream: " + text);
            try{
                mOutputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Unable to write to output stream: " + e.getMessage());
            }
        }

        /**
         * Called from the Main Activity to kill the connection.
         */
        public void cancel(){
            try{
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Unable to close the socket: " + e.getMessage());
            }
        }
    }

    public void connected(BluetoothSocket bluetoothSocket, BluetoothDevice device){
        Log.d(TAG, "connected: Starting");

        /**
         * Start the thread to manage the connection and perform data transmissions
         */
        mConnectedThread = new ConnectedThread(bluetoothSocket);
        mConnectedThread.start();
    }

    /**
     * Write to ConnectThread in an unsynchronized manner.
     */

    public void write(byte[] out){

        Log.d(TAG, "write: write called");
        /**
         * Perform the unsynchronized write
         */
        mConnectedThread.write(out);

    }
}
