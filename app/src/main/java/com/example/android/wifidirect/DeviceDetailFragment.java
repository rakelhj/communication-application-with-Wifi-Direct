/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Map;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.math.*;


import static com.example.android.wifidirect.WiFiDirectActivity.TAG;


/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private static WifiP2pInfo info;
    ProgressDialog progressDialog = null;


    private static String goIPAddress = null;
    private static String goMAcAddress = null;



    private static Activity mActivity = null;
    private static String myClientIPAddress = null;
    private static String myClientMacAddress = null;


    public static HashMap<String, String> mGOPeersConnected = new HashMap<String, String>();
    public static HashMap<String, String> mClientPeersConnected = new HashMap<String, String>();
    public static HashMap<String, String> mSignalStrengthPeersConnected = new HashMap<String, String>();


    private static boolean amIGroupOwner = false;

    private static String lowPriority = "2";
    private static String midPriority = "1";
    private static String highPriority = "0";


    private static String peerIPMessage = "IP";
    private static String peerItsMessage = "MESSAGE";
    private static String signalStrengthMessage = "SIGNAL_STRENGTH_MESSAGE";

    //private static WiFiDirectActivity wifiDirectactivity;
    private static String testServerSMS = "Ciao";
    private static String httpAddress = "129.241.200.206";
    private static String httpPort = "4424";

    private static Integer ValueOfHighestSignalStrength = null;
    private static String keyOfHighestSignalStrength = null;

    /*
    public static long messageSentAt = 0L;
    private static Integer numberOfMessages = 0;
    private static long messageDelayTime = 0L;
    private static long totalDelayTime = 0L;
    private static long averageDelayTime = 0L;
    private static long messageReceivedAt = 0L;
    */




    public static String getLowPriority() {
        return lowPriority;
    }

    public static String getMidPriority() {
        return midPriority;
    }

    public static String getHighPriority() {
        return highPriority;
    }

    public static String getPeerIPMessage() {
        return peerIPMessage;
    }

    public static String getPeerItsMessage() {
        return peerItsMessage;
    }

    public static String getSignalstrengthMessage() {
        return signalStrengthMessage;
    }

    public static boolean getAmIGroupOwner() {
        return amIGroupOwner;
    }

    public static String getGoMacAddress() {
        return goMAcAddress;
    }

    public static String getGoIPAddress() {
        return goIPAddress;
    }

    public static String getClientMacAddress() {
        return myClientMacAddress;
    }

    public static String getClientIPAddress() {
        return myClientIPAddress;
    }

    public static Activity getmActivity() {
        return mActivity;
    }


    public static void setAmIGroupOwner(boolean amIGroupOwner) {
        DeviceDetailFragment.amIGroupOwner = amIGroupOwner;
    }


    private static boolean isGroupFormed = false;

    public static boolean getIsGroupFormed() {
        return isGroupFormed;
    }

    public static void setIsGroupFormed(boolean groupFormed) {
        isGroupFormed = groupFormed;
    }

    public static String getServerSMS(){
        return testServerSMS;
    }

    public static String gethttpAddress(){
        return httpAddress;
    }

    public static String gethttpPort(){
        return httpPort;
    }

    public static void setHighestSignalStrength(Integer highestSignalStrength){
         ValueOfHighestSignalStrength = highestSignalStrength;
    }

    public static Integer getValueOfHighestSignalStrength(){
        return ValueOfHighestSignalStrength;
    }

    public static void setKeyHighestSignalStrength(String keyHighestSignalStrength){
        keyOfHighestSignalStrength = keyHighestSignalStrength;
    }

    public static String getKeyOfHighestSignalStrength(){
        return keyOfHighestSignalStrength;
    }

    /*
    public static long getMessageSentAt(){ return messageSentAt; }

    public static void setMessageSentAt(long messageSentAtTime){
        messageSentAt = messageSentAtTime; }*/


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        // onClickListener - event for the disconnect button
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        // onClickListener - event for the ITS message button
        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {


                        Integer highestSignalStrengthValue = -1;
                        String keyOfHighestSignalStrength = null;

                        long currentTimeValue = new Date().getTime();
                        Log.d(TAG, "currentTimeValue" + currentTimeValue );

                        //setMessageSentAt(currentTimeValue);


                        // GO code
                        if (getAmIGroupOwner()) {

                            //messageSentAt = System.currentTimeMillis();
                            //setMessageSentAt(messageSentAt);
                            //Log.d(TAG, "messageSentAt = " + messageSentAt);

                            ArrayList<String> currentConnectedPeersIPAddress = new ArrayList<String>();

                            Iterator mGOCurrentPeersConnectedIterator = mGOPeersConnected.keySet().iterator();
                            while (mGOCurrentPeersConnectedIterator.hasNext()) {
                                String key = (String) mGOCurrentPeersConnectedIterator.next();
                                if (!mGOPeersConnected.get(key).equalsIgnoreCase(goIPAddress))
                                    currentConnectedPeersIPAddress.add(mGOPeersConnected.get(key));
                            }

                            Iterator mSignalStrengthPeersConnectedIterator = mSignalStrengthPeersConnected.keySet().iterator();
                            while (mSignalStrengthPeersConnectedIterator.hasNext()) {
                                String key = (String) mSignalStrengthPeersConnectedIterator.next();
                                if (Integer.parseInt(mSignalStrengthPeersConnected.get(key)) >= highestSignalStrengthValue){
                                    highestSignalStrengthValue = Integer.parseInt(mSignalStrengthPeersConnected.get(key));
                                    keyOfHighestSignalStrength = key;
                                    setHighestSignalStrength(highestSignalStrengthValue);
                                    setKeyHighestSignalStrength (keyOfHighestSignalStrength);
                                }
                            }
                            Log.d(TAG, "keyOfhighestSignalStrength:" + keyOfHighestSignalStrength );

                             // use for broadcasting
                            /* for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {

                                    createAndStartFTSI(mActivity, currentConnectedPeersIPAddress.get(i), itsMessageToThePeer + ";Button Pushed");
                                   //createAndStartFTSI(mActivity, "192.168.49.1", itsMessageToThePeer + ";Button Pushed");
                            }*/


                            // use for unicast to the peer with highest signal strength
                            // if the signal strength is too low, send ITS-message to another client
                            if (SignalStrengthService.getSignalStrength() < 15 && getValueOfHighestSignalStrength()!= null && SignalStrengthService.getSignalStrength() < getValueOfHighestSignalStrength() && currentConnectedPeersIPAddress.size() > 0) {

                                    for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {

                                        String itsMessageToThePeer = peerItsMessage + ";" + lowPriority + ";" + goMAcAddress + ";" + goIPAddress;
                                        createAndStartFTSI(mActivity, mGOPeersConnected.get(getKeyOfHighestSignalStrength()), itsMessageToThePeer + ";Button Pushed");


                                }
                            }

                            // if the signalstrength is good, send straight to server
                            else {
                                //URL of the HTTP Server
                                String myHttpServerUrl = "http://" + gethttpAddress() + ":" + gethttpPort();

                                HttpServerConnection HttpServerConnection = new HttpServerConnection (myHttpServerUrl, getServerSMS());
                                HttpServerConnection.start();
                            }
                        }

                        // client code
                        if (!getAmIGroupOwner()) {

                            //messageSentAt = System.currentTimeMillis();
                            //setMessageSentAt(messageSentAt);
                            //Log.d(TAG, "messageSentAt = " + messageSentAt);

                            ArrayList<String> currentConnectedPeersIPAddress = new ArrayList<String>();

                            Iterator mClientCurrentPeersConnectedIterator = mClientPeersConnected.keySet().iterator();
                            while (mClientCurrentPeersConnectedIterator.hasNext()) {
                                String key = (String) mClientCurrentPeersConnectedIterator.next();
                                if (!mClientPeersConnected.get(key).equalsIgnoreCase(myClientMacAddress))
                                    currentConnectedPeersIPAddress.add(mClientPeersConnected.get(key));
                            }

                            Iterator mSignalStrengthPeersConnectedIterator = mSignalStrengthPeersConnected.keySet().iterator();
                            while (mSignalStrengthPeersConnectedIterator.hasNext()) {
                                String key = (String) mSignalStrengthPeersConnectedIterator.next();
                                if (Integer.parseInt(mSignalStrengthPeersConnected.get(key)) >= highestSignalStrengthValue){
                                    highestSignalStrengthValue = Integer.parseInt(mSignalStrengthPeersConnected.get(key));
                                    keyOfHighestSignalStrength = key;
                                }
                            }


                            for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {
                            }


                            if (currentConnectedPeersIPAddress.size() > 0) {
                                Log.d(TAG, "currentConnectedPeersIPAddress.size() = " + currentConnectedPeersIPAddress.size());
                                String itsMessageToThePeer = peerItsMessage + ";" + lowPriority + ";" + myClientMacAddress + ";" + myClientIPAddress;


                                // use for broadcasting
                               /* for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {

                                    createAndStartFTSI(mActivity, currentConnectedPeersIPAddress.get(i), itsMessageToThePeer + ";Button Pushed");
                                }*/

                               // use for unicast to the peer with highest signal strength
                                if (SignalStrengthService.getSignalStrength() < 15 && getValueOfHighestSignalStrength()!= null && SignalStrengthService.getSignalStrength() < getValueOfHighestSignalStrength() && currentConnectedPeersIPAddress.size() > 0) {
                                    createAndStartFTSI(mActivity, mGOPeersConnected.get(getKeyOfHighestSignalStrength()), itsMessageToThePeer + ";Button Pushed");

                                }

                                // if the signal is good, i.e. above 15, send straight to server
                                else {
                                    //URL of the HTTP Server
                                    String myHttpServerUrl = "http://" + gethttpAddress() + ":" + gethttpPort();

                                    HttpServerConnection HttpServerConnection = new HttpServerConnection (myHttpServerUrl, getServerSMS());
                                    HttpServerConnection.start();
                                }
                                }

                        }
                    }
                });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has triggered the message to be sent. Transfer it to receiver using
        // FileTransferService.


        Uri uri = data.getData();
        Log.d(TAG, "URI:  " + uri);
        Log.d(TAG, "URI string:  " + uri.toString());

    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {


        mActivity = getActivity();
        setIsGroupFormed(info.groupFormed);
        setAmIGroupOwner(info.isGroupOwner);


        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);


        // The Group owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress() + " Group formed - " + info.groupFormed + " ip address: " + getDottedDecimalIP(getLocalIPAddress()) + ": MAC Address: " + getWFDMacAddress());


        goIPAddress = info.groupOwnerAddress.getHostAddress();
        myClientIPAddress = getDottedDecimalIP(getLocalIPAddress()).toString();
        myClientMacAddress = getWFDMacAddress();




        if (info.groupFormed) {
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));

            new FileServerAsyncTask()
                    .execute();


            // put GO's IP address in the mGOPeersconnected hashmap
            if (info.isGroupOwner) {
                goMAcAddress = getWFDMacAddress();
                mGOPeersConnected.put(getWFDMacAddress().toUpperCase(), info.groupOwnerAddress.getHostAddress());

            }


            // put clients IP address in the mClientPeersConnected hashmap
            // And send the MAC:IP pair to GO
            if (!info.isGroupOwner) {
                mClientPeersConnected.put(getWFDMacAddress().toUpperCase(), myClientIPAddress);

                String smsToGroupOwner = "IP;" + "ADD;" + myClientMacAddress + ";" + myClientIPAddress;
                createAndStartFTSI(mActivity, goIPAddress, smsToGroupOwner);
            }



        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);

        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {


        private Context context;
        private TextView statusText;



        public FileServerAsyncTask() {


            Log.d(TAG, "RUNNING FileServerAsyncTask ...");


        }

        //
        @Override
        protected String doInBackground(Void... params) {

            ServerSocket serverSocket = null;
            Socket client = null;
            DataInputStream inputstream = null;
            try {
                serverSocket = new ServerSocket(8988);
                client = serverSocket.accept();
                inputstream = new DataInputStream(client.getInputStream());
                String str = inputstream.readUTF();
                serverSocket.close();

                return str;

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            } finally {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */



        // Handling the received message/result
        @Override
        protected void onPostExecute(String result) {

            String itsReceivedMessage = new String(result);
            Log.d(TAG, "itsReceivedMessage: " + itsReceivedMessage);

            String[] itsReceivedMessageSplit = itsReceivedMessage.split(";");

            Integer numberOfParameters = itsReceivedMessageSplit.length;





            //GO Code
            Log.d(TAG, "getAmIgroupowner: " + getAmIGroupOwner());
            if (getAmIGroupOwner()) {

                Log.d(TAG, "result = " + result);
                if (result != null) {

                    // The received message contains the senders IP address and the parameter "ADD"
                    if ((numberOfParameters == 5) && (itsReceivedMessageSplit[1].toUpperCase().equals(peerIPMessage)) && (itsReceivedMessageSplit[2].equalsIgnoreCase("ADD"))) {

                        String newPeerIPAddress = itsReceivedMessageSplit[4];
                        ArrayList<String> currentConnectedPeersIPAddress = new ArrayList<String>();

                        // send the GO's MAC:IP pair to the new peer
                        String smsToTheNewClient = "IP;" + "ADD;" + getWFDMacAddress().toUpperCase() + ";" + info.groupOwnerAddress.getHostAddress();
                        createAndStartFTSI(mActivity, newPeerIPAddress, smsToTheNewClient);


                        // Send the IP addresses of the already connected clients to the new peer
                        Iterator mGOCurrentPeersConnectedIterator = mGOPeersConnected.keySet().iterator();
                        while (mGOCurrentPeersConnectedIterator.hasNext()) {
                            String key = (String) mGOCurrentPeersConnectedIterator.next();
                            if (!mGOPeersConnected.get(key).equalsIgnoreCase(goIPAddress)) {
                                currentConnectedPeersIPAddress.add(mGOPeersConnected.get(key));

                                String smsToTheNewPeer = "IP;" + "ADD;" + key + ";" + mGOPeersConnected.get(key);
                                createAndStartFTSI(mActivity, newPeerIPAddress, smsToTheNewPeer);
                            }


                        }

                        for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {
                            Log.d(TAG, "Peer IP Address where to send the IP Address of the new Peer: currentConnectedPeersIPAddress.get(" + i + ")" + currentConnectedPeersIPAddress.get(i));
                        }


                        if (currentConnectedPeersIPAddress.size() > 0) {
                            Log.d(TAG, "currentConnectedPeersIPAddress.size() = " + currentConnectedPeersIPAddress.size());

                            // Send the IP address of the new peer to the already connected clients
                            for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {
                                String smsToTheClients = "IP;" + "ADD;" + itsReceivedMessageSplit[3] + ";" + itsReceivedMessageSplit[4];
                                createAndStartFTSI(mActivity, currentConnectedPeersIPAddress.get(i), smsToTheClients);
                            }


                        }

                        // Add the MAC:IP pair of the new peer to the GO's hashmap of connected peers
                        mGOPeersConnected.put(itsReceivedMessageSplit[3], itsReceivedMessageSplit[4]);

                        showConnectedPeersList(mGOPeersConnected, "Peers connected");

                        new FileServerAsyncTask().execute();

                    }

                    // The received message contains the ITS-Message
                    if ((numberOfParameters > 1) && (itsReceivedMessageSplit[1].toUpperCase().equals(peerItsMessage)) && (itsReceivedMessageSplit[2].equalsIgnoreCase(lowPriority))) {


                        /*
                        if(getMessageSentAt()!=0) {
                            //messageReceivedAt = System.currentTimeMillis();
                            messageReceivedAt = new Date().getTime();
                            messageSentAt = getMessageSentAt();
                            messageDelayTime = messageReceivedAt - messageSentAt;
                            numberOfMessages++;
                            totalDelayTime += messageDelayTime;
                            averageDelayTime = totalDelayTime / numberOfMessages;

                            Log.d(TAG, "messageDelayTime = " + (messageReceivedAt - messageSentAt));
                            Log.d(TAG, "numberOfMessages = " + numberOfMessages);
                            Log.d(TAG, "averageDelayTime = " + averageDelayTime);
                        }*/



                        String newPeerIPAddress = itsReceivedMessageSplit[4];


                        // Establish http-connection starting HttpServerConnection Thread
                        //URL of the HTTP Server
                        String myHttpServerUrl = "http://" + gethttpAddress() + ":" + gethttpPort();
                        HttpServerConnection HttpServerConnection = new HttpServerConnection (myHttpServerUrl, getServerSMS());
                        HttpServerConnection.start();


                    }
                    // The received message contains the signalStrength of the sender peer
                    if ((numberOfParameters > 1) && (itsReceivedMessageSplit[1].toUpperCase().equals(signalStrengthMessage)) && (itsReceivedMessageSplit[2].equalsIgnoreCase(lowPriority))) {

                        // add the signalstrength of the sender to the SignalStrength hashmap
                        mSignalStrengthPeersConnected.put(itsReceivedMessageSplit[3], (itsReceivedMessageSplit[5]));

                        // add the signalstrength of local peer to the SignalStrength hasmap
                        mSignalStrengthPeersConnected.put(goMAcAddress, SignalStrengthService.getSignalStrength().toString());


                        showConnectedPeersList(mSignalStrengthPeersConnected, "Signal Strength");

                    }

                        new FileServerAsyncTask().execute();


                if (result == null) {
                    Log.d(TAG, "result = " + result);

                    new FileServerAsyncTask().execute();
                }

            }}
            //Client Code
            if (!getAmIGroupOwner()) {


                if (result != null) {

                    // The received message contains the senders IP address
                    if ((numberOfParameters == 5) && (itsReceivedMessageSplit[1].toUpperCase().equals(peerIPMessage))) {
                        String newPeerIPAddress = itsReceivedMessageSplit[4];

                        // The message has the parameter "ADD" - add this MAC:IP pair to the hashmap of connected peers
                        if (itsReceivedMessageSplit[2].toUpperCase().equals("ADD")) {
                            mClientPeersConnected.put(itsReceivedMessageSplit[3], itsReceivedMessageSplit[4]);
                            //showConnectedPeersList(mClientPeersConnected, "ADD - Client Newcomer Peers List ");

                            // The message has the parameter "REMOVE" - add this MAC:IP pair to the hashmap of connected peers
                        } else if (itsReceivedMessageSplit[2].toUpperCase().equals("REMOVE")) {
                            mClientPeersConnected.remove(itsReceivedMessageSplit[3]);
                            mSignalStrengthPeersConnected.remove(itsReceivedMessageSplit[3]);
                            //showConnectedPeersList(mClientPeersConnected, "REMOVE - Client Newcomer Peers List ");
                        } else {
                            Log.d(TAG, "Attention: Unable to understand the message of the GO");
                        }
                    }

                    // The received message contains the ITS-Message
                    if ((numberOfParameters > 1) && (itsReceivedMessageSplit[1].toUpperCase().equals(peerItsMessage)) && (itsReceivedMessageSplit[2].equalsIgnoreCase(lowPriority))) {

                        Log.d(TAG, "ITS message is received ");

                        /*
                        if(messageSentAt!=0) {
                            //messageReceivedAt = System.currentTimeMillis();
                            messageReceivedAt = new Date().getTime();
                            messageSentAt = getMessageSentAt();
                            messageDelayTime = messageReceivedAt - messageSentAt;
                            numberOfMessages++;
                            totalDelayTime += messageDelayTime;
                            averageDelayTime = totalDelayTime / numberOfMessages;

                            Log.d(TAG, "messageDelayTime = " + (messageReceivedAt - messageSentAt));
                            Log.d(TAG, "numberOfMessages = " + numberOfMessages);
                            Log.d(TAG, "averageDelayTime = " + averageDelayTime);

                        }*/

                        String newPeerIPAddress = itsReceivedMessageSplit[4];


                        // Establish http-connection starting HttpServerConnection Thread


                        //URL of the HTTP Server
                        String myHttpServerUrl = "http://" + gethttpAddress() + ":" + gethttpPort();

                        HttpServerConnection HttpServerConnection = new HttpServerConnection (myHttpServerUrl, getServerSMS());
                        HttpServerConnection.start();
                    }

                    // The received message contains the signal strength
                    if ((numberOfParameters > 1) && (itsReceivedMessageSplit[1].toUpperCase().equals(signalStrengthMessage)) && (itsReceivedMessageSplit[2].equalsIgnoreCase(lowPriority))) {

                        mSignalStrengthPeersConnected.put(itsReceivedMessageSplit[3], itsReceivedMessageSplit[5]);
                        mSignalStrengthPeersConnected.put(getWFDMacAddress().toUpperCase(), SignalStrengthService.getSignalStrength().toString());



                        //showConnectedPeersList(mSignalStrengthPeersConnected, "Client: Peers Signal Strength List : ");
                        showConnectedPeersList(mSignalStrengthPeersConnected, "Signal Strength");

                    }


                    new FileServerAsyncTask().execute();

                }
                if (result == null) {
                    Log.d(TAG, "result = " + result);

                    new FileServerAsyncTask().execute();
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
        }

    }



    // Method to get the local IP address of device
    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    // Make the IP-address to correct form
    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }


    // method to start FileTransferService
    public static void createAndStartFTSI(Activity lActivity, String lPeerIpAddress, String lPeerSms) {


        String[] lPeerSmsSplit  = lPeerSms.split(";");
        Log.d(TAG, "lpeersms "+ lPeerSms);


        if ((lPeerSmsSplit[0].toUpperCase().equals(peerItsMessage))) {
            /*setMessageSentAt(System.currentTimeMillis());
            long currentTimeValue = new Date().getTime();
            setMessageSentAt(currentTimeValue);*/

        }
            String filepathIpadress2 = "/storage/emulated/0/Download/myIpAdress.txt";


        try {
            File f = new File(filepathIpadress2);
            InputStream fileIS = new FileInputStream(f);
            BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
            String readString = new String();
            while ((readString = buf.readLine()) != null) {
                Log.d("Content: ", readString);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent serviceIntentIpAdress = new Intent(lActivity, FileTransferService.class);
        serviceIntentIpAdress.setAction(FileTransferService.ACTION_SEND_MESSAGE);
        serviceIntentIpAdress.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                lPeerIpAddress);

        serviceIntentIpAdress.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);

        String checkedGO = null;
        if (getAmIGroupOwner())
            checkedGO = "Yes";
        else
            checkedGO = "No";
        serviceIntentIpAdress.putExtra(FileTransferService.ITS_MESSAGE_FROM_PEER_S, checkedGO + ";" + lPeerSms);
        serviceIntentIpAdress.putExtra(FileTransferService.PEER_S_ADDRESS, lPeerIpAddress);


        mActivity.startService(serviceIntentIpAdress);

    }


    // get local MAC address
    public static String getWFDMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ntwInterface : interfaces) {

                if (ntwInterface.getName().equalsIgnoreCase("p2p0")) {
                    byte[] byteMac = ntwInterface.getHardwareAddress();
                    if (byteMac == null) {
                        return null;
                    }
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i = 0; i < byteMac.length; i++) {
                        strBuilder.append(String.format("%02X:", byteMac[i]));
                    }

                    if (strBuilder.length() > 0) {
                        strBuilder.deleteCharAt(strBuilder.length() - 1);
                    }

                    return strBuilder.toString();
                }

            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }



    // Method to log hashmap
    public static void showConnectedPeersList(HashMap<String, String> lHashMap, String lInfo) {

        int numberOfPeers = 0;

        Iterator lHashMapIterator = lHashMap.keySet().iterator();
        while (lHashMapIterator.hasNext()) {
            numberOfPeers++;
            String key = (String) lHashMapIterator.next();
            System.out.println(lInfo + " hashmap: " + key + ";" + lHashMap.get(key));

        }

        System.out.println(lInfo + " - numberOfPeers =  " + numberOfPeers);


    }
}


