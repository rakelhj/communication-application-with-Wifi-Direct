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

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.android.wifidirect.DeviceDetailFragment.mClientPeersConnected;
import static com.example.android.wifidirect.DeviceDetailFragment.mGOPeersConnected;
import static com.example.android.wifidirect.DeviceDetailFragment.mSignalStrengthPeersConnected;
import static com.example.android.wifidirect.WiFiDirectActivity.TAG;

/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */
public class DeviceListFragment extends ListFragment implements PeerListListener {


    private static List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;
    private boolean isAlreadyConnectedPeer = false;
    private boolean doesAnyGroupExist = false;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE: {
                Log.d(TAG, "Peer status : Available");
                return "Available";
            }
            case WifiP2pDevice.INVITED: {
                Log.d(TAG, "Peer status : Invited");
                return "Invited";
            }
            case WifiP2pDevice.CONNECTED: {
                Log.d(TAG, "Peer status : Connected");
                return "Connected";
            }
            case WifiP2pDevice.FAILED: {
                Log.d(TAG, "Peer status : Failed");
                return "Failed";
            }
            case WifiP2pDevice.UNAVAILABLE: {
                Log.d(TAG, "Peer status : Unavailable");
                return "Unavailable";
            }
            default: {
                Log.d(TAG, "Peer status : Unavailable");
                return "Unknown";

            }
        }
    }

    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(device);
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {


        private List<WifiP2pDevice> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;

        }
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    // Listener for whenever there are peers available
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        boolean isItMeTheGO = DeviceDetailFragment.getAmIGroupOwner();


        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

        int peerListSize = peers.size();


        // There are devices found
        if (peerListSize != 0) {


            isAlreadyConnectedPeer = false;
            doesAnyGroupExist = false;


            for (int i = 0; i < peerListSize; i++) {

                // the status is 0, i.e. connected
                if (peers.get(i).status == 0) {
                    isAlreadyConnectedPeer = true;
                }

                if ((peers.get(i).isGroupOwner()) || (isItMeTheGO)) {
                    doesAnyGroupExist = true;
                }


            }

        }


        // No devices found - clear up hashmaps
        if (peerListSize == 0) {
            if ((isItMeTheGO) && (!mGOPeersConnected.isEmpty())) {
                mGOPeersConnected.clear();
                DeviceDetailFragment.showConnectedPeersList(mGOPeersConnected, "peerListSize: Cleaning Peers List - ");
            }
            if ((!isItMeTheGO) && (!mClientPeersConnected.isEmpty())) {
                mClientPeersConnected.clear();
                DeviceDetailFragment.showConnectedPeersList(mClientPeersConnected, "peerListSize: Cleaning Peers List - ");
            }
            return;
        } else {


            int peersThatIAmConnected = 0;
            for (int i = 0; i < peerListSize; i++) {
                if ((peers.get(i).status == 0)) {
                    peersThatIAmConnected++;
                }
            }

            // This device is not connected - clear hashmaps
            if (peersThatIAmConnected == 0) {
                if ((isItMeTheGO) && (!mGOPeersConnected.isEmpty())) {
                    mGOPeersConnected.clear();
                    mSignalStrengthPeersConnected.clear();
                    DeviceDetailFragment.showConnectedPeersList(mGOPeersConnected, "peersThatIAmConnected: Cleaning Peers List - ");
                }
                if ((!isItMeTheGO) && (!mClientPeersConnected.isEmpty())) {
                    mClientPeersConnected.clear();
                    mSignalStrengthPeersConnected.clear();
                    DeviceDetailFragment.showConnectedPeersList(mClientPeersConnected, "peersThatIAmConnected: Cleaning Peers List - ");
                }
            }


            // GO checks for the discovered peers list
            if ((doesAnyGroupExist) && (isItMeTheGO)) {

                ArrayList<String> peersDiscoveredAndConnected = new ArrayList<String>();
                ArrayList<String> keysOfTheRemovedPeersFromTheList = new ArrayList<String>();

                for (int i = 0; i < peerListSize; i++) {
                    if ((peers.get(i).status == 0)) {
                        peersDiscoveredAndConnected.add(peers.get(i).deviceAddress.toUpperCase());
                    }
                }

                for (int i = 0; i < peersDiscoveredAndConnected.size(); i++) {
                    Log.d(TAG, "peersDiscovered.get(" + i + ")" + peersDiscoveredAndConnected.get(i));
                }

                // remove disconnected peers from connected peers hashmap
                Iterator mGOUpdatedPeersConnectedIterator = DeviceDetailFragment.mGOPeersConnected.keySet().iterator();
                while (mGOUpdatedPeersConnectedIterator.hasNext()) {
                    String key = (String) mGOUpdatedPeersConnectedIterator.next();
                    Log.d(TAG, "Checking if the key: " + key + " is in the peersDiscovered list!");
                    if ((!peersDiscoveredAndConnected.isEmpty()) && (!peersDiscoveredAndConnected.contains(key)) && (!key.equalsIgnoreCase(DeviceDetailFragment.getGoMacAddress()))) {
                        mGOUpdatedPeersConnectedIterator.remove();
                        keysOfTheRemovedPeersFromTheList.add(key);
                    }

                }

                // remove disconnected peers from signal strength hashmap
                Iterator mSignalStrengthPeersConnectedIterator = mSignalStrengthPeersConnected.keySet().iterator();
                while (mSignalStrengthPeersConnectedIterator.hasNext()) {
                    String key = (String) mSignalStrengthPeersConnectedIterator.next();
                    if ((!peersDiscoveredAndConnected.isEmpty()) && (!peersDiscoveredAndConnected.contains(key)) && (!key.equalsIgnoreCase(DeviceDetailFragment.getGoMacAddress()))) {
                        mSignalStrengthPeersConnectedIterator.remove();
                    }
                }



                // GO updating the clients to remove disconnected peers from their hashmaps
                if (keysOfTheRemovedPeersFromTheList.size() != 0) {
                    for (int i = 0; i < keysOfTheRemovedPeersFromTheList.size(); i++) {

                        Iterator mRemoveFromGOUpdatedPeersConnectedIterator = mGOPeersConnected.keySet().iterator();
                        while (mRemoveFromGOUpdatedPeersConnectedIterator.hasNext()) {

                            String key = (String) mRemoveFromGOUpdatedPeersConnectedIterator.next();

                            Log.d(TAG, "Sending the IP addresses of the removed clients to the connected peer(s)");
                            String smsToTheConnectedPeer = "IP;" + "REMOVE;" + keysOfTheRemovedPeersFromTheList.get(i) + ";" + "1.1.1.1";
                            new DeviceDetailFragment().createAndStartFTSI(getActivity(), mGOPeersConnected.get(key), smsToTheConnectedPeer);
                        }

                    }

                }

            }



            if ((!isAlreadyConnectedPeer) || ((isAlreadyConnectedPeer) && (doesAnyGroupExist) && (isItMeTheGO))) {

                for (int i = 0; i < peerListSize; i++) {

                    if (!peers.get(i).deviceName.equalsIgnoreCase("DIRECT-d5-HP M277 LaserJet")) {

                        Log.d(TAG, "Yes, " + peerListSize + " device(s) found");


                        int peersConnectionStatus = peers.get(i).status;


                        // Check if we should resend the invitation ...
                        if ((peersConnectionStatus != 0) && (peersConnectionStatus != 1)) {


                            Log.d(TAG, "peers.get(" + i + ").status = " + peers.get(i).status + " !");


                            if (!doesAnyGroupExist) {

                                Log.d(TAG, "Group creation ... !");


                                WifiP2pConfig config = new WifiP2pConfig();
                                config.deviceAddress = peers.get(i).deviceAddress;
                                config.wps.setup = WpsInfo.PBC;
                                ((DeviceActionListener) getActivity()).connect(config);
                            }
                            //not the group owner
                            else if ((doesAnyGroupExist) && (peers.get(i).isGroupOwner()) && (!isItMeTheGO)) {

                                Log.d(TAG, "Client : Sending the request to the GO ... !");

                                WifiP2pConfig config = new WifiP2pConfig();
                                config.deviceAddress = peers.get(i).deviceAddress;
                                config.wps.setup = WpsInfo.PBC;
                                ((DeviceActionListener) getActivity()).connect(config);

                            }
                            //group owner sends the connection invitation
                            else if ((doesAnyGroupExist) && (isItMeTheGO)) {

                                Log.d(TAG, "Group Owner: Sending the request to the Client ... !");

                                WifiP2pConfig config = new WifiP2pConfig();
                                config.deviceAddress = peers.get(i).deviceAddress;
                                config.wps.setup = WpsInfo.PBC;
                                ((DeviceActionListener) getActivity()).connect(config);

                            } else
                                Log.d(TAG, "peers.get(" + i + ").status = " + peers.get(i).status + " !");


                        } else
                            Log.d(TAG, "DeviceDetailFragment.getIsGroupFormed() = " + DeviceDetailFragment.getIsGroupFormed() + ", peers.get(" + i + ").isGroupOwner() = " + peers.get(i).isGroupOwner() + " !");


                    }
                }

                return;
            }

        }


    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     *
     */
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }


}
