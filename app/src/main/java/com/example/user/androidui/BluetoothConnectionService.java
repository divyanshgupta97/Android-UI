package com.example.user.androidui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    /**
     * TAG for logging purposes.
     */
    private static final String TAG = "BluetoothConnection";

    /**
     * Application Name
     */
    private static final String appName = "Mars Rover";

    /**
     * UUID to setup Bluetooth Server Socket to listen
     * for incoming Bluetooth Sockets.
     */
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /**
     * Bluetooth Essentials.
     */
    private final BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private UUID mBTDeviceUUID;

    /**
     * Context of the calling Activity.
     */
    private final Context mContext;

    /**
     * Bluetooth Communication Threads.
     */
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    /**
     * Progress Dialog for user-feedback.
     */
    private ProgressDialog mProgressDialog;


    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        /**
         * Local Server Socket for listening to
         * incoming Bluetooth Sockets.
         */
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            /**
             * Instantiate a new server socket for listening to
             * incoming Bluetooth Sockets.
             */
            try{
                Log.d(TAG, "Inside Accept Thread");
                tmp = mBTAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                /**
                 * This is a blocking call and will only return on a
                 * successful connection or exception.
                 */
                Log.d(TAG, "run: RFCOM server socket start.....");

                /**
                 * Accepted an incoming Bluetooth Socket.
                 */
                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            /**
             * Start the Connected Thread to begin I/O with remote
             * Bluetooth Device.
             */
            if(socket != null){
                connected(socket);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                /**
                 * Close the Bluetooth Server Socket to not
                 * listen for any new devices.
                 */
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    /**
     * This thread runs while trying to make an outgoing connection
     * with a remote device. Given a Bluetooth Device object, it retrieves the
     * Bluetooth socket from the device. This method runs straight through,
     * it either succeeds or fails.
     */
    private class ConnectThread extends Thread {
        /**
         * Local Bluetooth Socket object.
         */
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mBTDevice = device;
            mBTDeviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            /**
             * Retrieve Bluetooth Socket from the given remote
             * Bluetooth Device.
             */
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mBTDevice.createRfcommSocketToServiceRecord(mBTDeviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            /**
             * Stop discovering new Bluetooth Devices as we have a
             * device to connect with.
             */
            mBTAdapter.cancelDiscovery();

            /**
             * Start a connection with the retrieved
             * Bluetooth Socket.
             */

            try {
                /**
                 * This is a blocking call. This returns on
                 * either success or exception.
                 */
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                /**
                 * Cannot socket to the Bluetooth Socket,
                 * so we close it.
                 */
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            /**
             * Start the Connected Thread to begin I/O with remote
             * Bluetooth Device.
             */
            connected(mmSocket);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode.
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        /**
         * Cancel any Connect Thread instance trying to make
         * an outgoing connection.
         */
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        /**
         * Instantiate an AcceptThread to start listening
         * for Bluetooth Sockets.
         */
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Given a Bluetooth Device and a UUID, we use ConnectThread
     * to retreive the Bluetooth Socket, connect to it and then
     * use the Connected Thread to begin I/O.
     **/

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        /**
         * Progress Dialog for user-feedback.
         */
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     * Connected Thread is responsible for maintaining the Bluetooth Connection,
     * sending and receiving data via I/O Streams.
     */
    private class ConnectedThread extends Thread {
        /**
         * The connected Bluetooth Socket from the
         * remote paired device.
         */
        private BluetoothSocket mmSocket;

        /**
         * I/O Streams for receiving and sending data.
         */
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            /**
             * Dismiss the Progress Dialog.
             */
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            /**
             * Buffer store for the stream.
             */
            byte[] buffer = new byte[1024];

            /**
             * Bytes returned from the InputStream.
             */
            int bytes;

            /**
             * Keep listening to the InputStream until
             * an exception occurs.
             */
            while (true) {
                /**
                 * Read bytes from the InputStream.
                 */
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    /**
                     * Broadcast the received data to the Broadcast Receivers that
                     * are listening to it.
                     */
                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.e(TAG, "read: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        /**
         * @param bytes
         * This is called from the calling activity to send data to
         * remote Bluetooth Device via the output stream.
         */
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /**
         * Close the I/O connection with the remote device
         * and close the connected Bluetooth Socket.
         */
        public void cancel() {
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                    Log.d(TAG, "InputStream closed");
                } catch (IOException e) {
                    Log.d(TAG, "Unable to close InputStream");
                }
                mmInStream = null;
            }

            if(mmOutStream != null){
                try{
                    mmOutStream.close();
                    Log.d(TAG, "OutputStream closed");
                } catch (IOException e) {
                    Log.d(TAG, "Unable to close OutputStream");
                }
                mmOutStream = null;
            }

            if(mmSocket != null){
                try{
                    mmSocket.close();
                    Log.d(TAG, "Connected Thread  Bluetooth Socket closed");
                } catch (IOException e) {
                    Log.d(TAG, "Unable to close Bluetooth Socket for Connected Thread");
                }
                mmSocket = null;
            }
        }
    }

    /**
     * @param mmSocket
     * Give a connected Bluetooth Socket to begin
     * I/O via the Connected Thread.
     */
    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * @param out The bytes to write
     * Write to the Connected Thread in an unsynchronized manner.
     */
    public void write(byte[] out) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

    /**
     * Stop all threads. Bluetooth Communication is
     * no longer required.
     */
    public void stopAllThreads() {
        Log.d(TAG, "Kill all Bluetooth Threads");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
    }

    /**
     * Stop the Connect Thread and the Connected Thread,
     * but keep the Accept Thread alive for listening to
     * incoming to devices.
     */
    public void stopThreadsButListen(){
        Log.d(TAG, "Kill only Connect and Connected Threads");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }
}























