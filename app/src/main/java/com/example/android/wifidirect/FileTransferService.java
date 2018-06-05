// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.example.android.wifidirect.WiFiDirectActivity.TAG;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_MESSAGE = "Hei";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String PEER_S_ADDRESS = "cl_host";
    public static final String ITS_MESSAGE_FROM_PEER_S = "no";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";


    public FileTransferService() {
        super("FileTransferService");
    }


    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {


        Log.d(TAG, "Running onHandleIntent of FileTransferService! ");

        Context context = getApplicationContext();

        if (intent.getAction().equals(ACTION_SEND_MESSAGE)) {
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket goSocket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            String mItsSMS = intent.getExtras().getString(ITS_MESSAGE_FROM_PEER_S);
            String peerIPAddress = intent.getExtras().getString(PEER_S_ADDRESS);
            DataOutputStream goStream = null;



            try {

                //Sending message to the peer
                goSocket.connect((new InetSocketAddress(peerIPAddress, port)), SOCKET_TIMEOUT);
                goStream = new DataOutputStream(goSocket.getOutputStream());
                goStream.writeUTF(mItsSMS);


                //}
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (goStream != null) {
                    try {
                        goStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (goStream != null) {
                    if (goSocket.isConnected()) {
                        try {
                            goSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }
}
