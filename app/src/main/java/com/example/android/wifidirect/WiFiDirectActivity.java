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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

import static com.example.android.wifidirect.Connectivity.isConnectedFast;
import static com.example.android.wifidirect.Connectivity.isConnectedMobile;
import static com.example.android.wifidirect.Connectivity.isConnectedWifi;


/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener, DeviceActionListener {



    public static final String TAG = "wifidirectdemo";
    public static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    SignalStrengthService mSignalStrengthService;
    CurrentPositionService mCurrentPositionService;
    private Intent mSignalStrengthServiceIntent;
    private Intent mCurrentPositionServiceIntent;
    private boolean bound = false;
    private boolean isBoundSignalStrength = false;
    private boolean isBoundLocalization = false;

    public ServiceConnection signalStrengthServiceConnection;
    public ServiceConnection currentPositionServiceConnection;


    private WifiP2pManager manager;


    public WifiP2pManager getManager() {
        return manager;
    }

    public void setManager(WifiP2pManager manager) {
        this.manager = manager;
    }


    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();


    private Channel channel;


    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    private BroadcastReceiver receiver = null;
    private String h;




    DiscoveryPeersThread discoveryPeersThread;






    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        if (ActivityCompat.checkSelfPermission(WiFiDirectActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WiFiDirectActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }


        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }


/*
        // To be tested!
        String providerCheckGPS = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!providerCheckGPS.contains("gps")){ //if gps is disabled
            Log.d(TAG, "The GPS is disabled!" + "Enabling GPS ...");
            turnGPSOn();
            Log.d(TAG, "The GPS is enabled!");
        }
        else
            Log.d(TAG, "The GPS is already enabled!");
*/

        // Get network info
        NetworkInfo info = Connectivity.getNetworkInfo(this.getApplicationContext());
        if (info != null && info.isConnected()) {
            System.out.println("info.getType(): " + info.getType());
            System.out.println("info.getTypeName(): " + info.getTypeName());
            System.out.println("info.getSubtype(): " + info.getSubtype());
            System.out.println("info.getSubtypeName(): " + info.getSubtypeName());
            System.out.println("isConnectedWifi(): " + isConnectedWifi(this.getApplicationContext()));
            System.out.println("isConnectedMobile(): " + isConnectedMobile(this.getApplicationContext()));
            System.out.println("isConnectedFast(): " + isConnectedFast(this.getApplicationContext()));
        } else {
            Log.e(TAG, "No NetworkInfo is available...");
        }


        mSignalStrengthServiceIntent = new Intent(getBaseContext(), SignalStrengthService.class);
        mCurrentPositionServiceIntent = new Intent(getBaseContext(), CurrentPositionService.class);


        /** Callbacks for service binding, passed to bindService() */
        signalStrengthServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                SignalStrengthService.SignalStrengthServicerBinder binder = (SignalStrengthService.SignalStrengthServicerBinder) service;
                mSignalStrengthService = binder.getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mSignalStrengthService = null;
                bound = false;
            }
        };


        /** Callbacks for service binding, passed to bindService() */
        currentPositionServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                CurrentPositionService.CurrentPositionServiceBinder binder = (CurrentPositionService.CurrentPositionServiceBinder) service;
                mCurrentPositionService = binder.getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mCurrentPositionService = null;
                bound = false;
            }
        };



        //Starting the Signal Strength Service
        startService(mSignalStrengthServiceIntent);
        isBoundSignalStrength = getApplicationContext().bindService(mSignalStrengthServiceIntent, signalStrengthServiceConnection, Context.BIND_AUTO_CREATE);
        //Starting the Localization Service
        startService(mCurrentPositionServiceIntent);
        isBoundLocalization = getApplicationContext().bindService(mCurrentPositionServiceIntent, currentPositionServiceConnection, Context.BIND_AUTO_CREATE);




        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {



                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }


                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();


                discoveryPeersThread = new DiscoveryPeersThread(WiFiDirectActivity.this, fragment);
                discoveryPeersThread.start();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }



/*
    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent gpsIntent = new Intent();
            gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            gpsIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            gpsIntent.setData(Uri.parse("3"));
            sendBroadcast(gpsIntent);
        }
    }

    private void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent gpsIntent = new Intent();
            gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            gpsIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            gpsIntent.setData(Uri.parse("3"));
            sendBroadcast(gpsIntent);
        }
    }

*/

}
