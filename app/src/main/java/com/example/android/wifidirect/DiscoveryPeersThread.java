package com.example.android.wifidirect;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;


public class DiscoveryPeersThread extends Thread {


    WiFiDirectActivity mParent;
    DeviceListFragment mdfl;


    private WifiP2pManager lManager;
    private WifiP2pManager.Channel lChannel;

    public DiscoveryPeersThread(WiFiDirectActivity parent) {
        super();
        this.mParent = parent;

    }


    public DiscoveryPeersThread(WiFiDirectActivity parent, DeviceListFragment dlf) {
        super();
        this.mParent = parent;
        this.mdfl = dlf;

    }


    public void run() {

        Log.d(WiFiDirectActivity.TAG, "DiscoveryPeersThread is running...");

        boolean i = true;

        while (i) {

            lManager = mParent.getManager();
            lChannel = mParent.getChannel();


            lManager.discoverPeers(lChannel, new WifiP2pManager.ActionListener() {


                @Override
                public void onSuccess() {
                    Toast.makeText(mParent, "Discovery Initiated",
                            Toast.LENGTH_SHORT).show();

                    mParent.setManager(lManager);
                    mParent.setChannel(lChannel);

                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(mParent, "Discovery Failed : " + reasonCode,
                            Toast.LENGTH_SHORT).show();

                    mParent.setManager(lManager);
                    mParent.setChannel(lChannel);
                }
            });


            Log.d(WiFiDirectActivity.TAG, "device.name" + mdfl.getDevice().deviceName);
            Log.d(WiFiDirectActivity.TAG, "device.address" + mdfl.getDevice().deviceAddress);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}

