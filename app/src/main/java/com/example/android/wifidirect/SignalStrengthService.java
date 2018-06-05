package com.example.android.wifidirect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import static com.example.android.wifidirect.DeviceDetailFragment.getAmIGroupOwner;
import static com.example.android.wifidirect.DeviceDetailFragment.getClientIPAddress;
import static com.example.android.wifidirect.DeviceDetailFragment.getClientMacAddress;
import static com.example.android.wifidirect.DeviceDetailFragment.getGoIPAddress;
import static com.example.android.wifidirect.DeviceDetailFragment.getGoMacAddress;
import static com.example.android.wifidirect.DeviceDetailFragment.getLowPriority;
import static com.example.android.wifidirect.DeviceDetailFragment.getSignalstrengthMessage;
import static com.example.android.wifidirect.DeviceDetailFragment.getmActivity;
import static com.example.android.wifidirect.DeviceDetailFragment.mClientPeersConnected;
import static com.example.android.wifidirect.DeviceDetailFragment.mGOPeersConnected;
import static com.example.android.wifidirect.WiFiDirectActivity.TAG;


public class SignalStrengthService extends Service {

    TelephonyManager tm;
    PhoneStateListener psl;
    //Integer signalStrength = -1;
    private static Integer signalStrength = -1;
    private static String newCase = null;
    private static String oldCase = null;
    private static String currentCase = null;



    public void setSignalStrength(Integer iStrength) {
        this.signalStrength = iStrength;
    }

    //public Integer getSignalStrength() {return this.signalStrength;}

    public static Integer getSignalStrength() {
        return signalStrength;
    }


    private final IBinder mBinder = new SignalStrengthServicerBinder();


    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getBaseContext(), "The SignalStrength Service is running ...", Toast.LENGTH_SHORT).show();


        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        psl = new PhoneStateListener() {
            public void onSignalStrengthsChanged(android.telephony.SignalStrength sigStr) {
                if (sigStr != null) {
                    signalStrength = sigStr.getGsmSignalStrength();
                    setSignalStrength(signalStrength);


                    //signalStrengthBigChange(getSignalStrength());

                    //Toast.makeText(getBaseContext(), "Signal Strength changed to " + getSignalStrength(), Toast.LENGTH_SHORT).show();


                    //System.out.println("sigStr.getGsmBitErrorRate(): " + sigStr.getGsmBitErrorRate());
                    //System.out.println("sigStr.getLevel(): " + sigStr.getLevel());
                    //System.out.println("sigStr.getEvdoSnr(): " + sigStr.getEvdoSnr());


                    //System.out.println("tm.getPhoneType(): " + tm.getPhoneType());
                    //System.out.println("tm.getNetworkType(): " + tm.getNetworkType());
                    //System.out.println("tm.getCellLocation(): " + tm.getCellLocation());
                    //System.out.println("tm.getSimOperator(): " + tm.getSimOperator());
                    //System.out.println("tm.getAllCellInfo(): " + tm.getAllCellInfo());





                    if (mGOPeersConnected.size() > 1 || mClientPeersConnected.size() > 1){
                        oldCase = getCurrentCase();
                        setSignalStrengthCase(signalStrength);
                        newCase = getCurrentCase();
                    }
                    else {
                        newCase = null;
                        oldCase = null;
                    }

                    // A case change for the signal strength has occurred
                    if((significantSignalStrengthChange(oldCase, newCase) && mGOPeersConnected.size() >= 1) || (significantSignalStrengthChange(oldCase, newCase) && mClientPeersConnected.size() >= 1)) {


                        Integer signalStrength = SignalStrengthService.getSignalStrength();
                        Log.d(TAG, "SSS: Signalstrength" + signalStrength);

                        if (getAmIGroupOwner()) {
                            ArrayList<String> currentConnectedPeersIPAddress = new ArrayList<String>();

                            Iterator mGOCurrentPeersConnectedIterator = mGOPeersConnected.keySet().iterator();
                            while (mGOCurrentPeersConnectedIterator.hasNext()) {
                                String key = (String) mGOCurrentPeersConnectedIterator.next();
                                if (!mGOPeersConnected.get(key).equalsIgnoreCase(getGoIPAddress()))
                                    currentConnectedPeersIPAddress.add(mGOPeersConnected.get(key));

                            }

                            for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {
                                Log.d(TAG, "SSS: Peer IP Address where to send the IP Address of the new Peer: currentConnectedPeersIPAddress.get(" + i + ")" + currentConnectedPeersIPAddress.get(i));
                            }


                            // Send GO's signal strength to the rest of the group
                            if (currentConnectedPeersIPAddress.size() > 0) {

                                for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {

                                    String itsMessageToThePeer = getSignalstrengthMessage() + ";" + getLowPriority() + ";" + getGoMacAddress() + ";" + getGoIPAddress()+ ";" + getSignalStrength();
                                    new DeviceDetailFragment().createAndStartFTSI(getmActivity(), currentConnectedPeersIPAddress.get(i), itsMessageToThePeer + ";Message from SignalStrength Service");


                                }
                            }

                        }
                        if (!getAmIGroupOwner()) {
                            ArrayList<String> currentConnectedPeersIPAddress = new ArrayList<String>();

                            Iterator mClientCurrentPeersConnectedIterator = mClientPeersConnected.keySet().iterator();
                            while (mClientCurrentPeersConnectedIterator.hasNext()) {
                                String key = (String) mClientCurrentPeersConnectedIterator.next();
                                if (!mClientPeersConnected.get(key).equalsIgnoreCase(getClientIPAddress()))
                                    currentConnectedPeersIPAddress.add(mClientPeersConnected.get(key));

                            }

                            for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {
                                Log.d(TAG, "SSS: Peer IP Address where to send the IP Address of the new Peer: currentConnectedPeersIPAddress.get(" + i + ")" + currentConnectedPeersIPAddress.get(i));
                            }


                            // Send client's signal strength to the rest of the group
                            if (currentConnectedPeersIPAddress.size() > 0) {
                                Log.d(TAG, "SSS: currentConnectedPeersIPAddress.size() = " + currentConnectedPeersIPAddress.size());

                                for (int i = 0; i < currentConnectedPeersIPAddress.size(); i++) {

                                    Log.d(TAG, "SSS: Peer IP Address where to send the IP Address of the new Peer: currentConnectedPeersIPAddress.get(" + i + ")" + currentConnectedPeersIPAddress.get(i));

                                    String itsMessageToThePeer = getSignalstrengthMessage() + ";" + getLowPriority() + ";" + getClientMacAddress() + ";" + getClientIPAddress() + ";" + getSignalStrength();
                                    new DeviceDetailFragment().createAndStartFTSI(getmActivity(), currentConnectedPeersIPAddress.get(i), itsMessageToThePeer + ";Message from SignalStrength Service");
                                }
                            }

                        }
                    }


                    /*try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/


                }
            }
        };


        tm.listen(psl,
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                        PhoneStateListener.LISTEN_CALL_STATE |
                        PhoneStateListener.LISTEN_CELL_LOCATION |
                        PhoneStateListener.LISTEN_DATA_ACTIVITY |
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                        PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                        PhoneStateListener.LISTEN_SERVICE_STATE |
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
        );

        return START_STICKY;

    }


    @Override
    public void onDestroy() {
        //tm = null;
        //psl = null;
        System.out.println("Stopping the PhoneStateListener!!!");
        setSignalStrength(-1);
        tm.listen(psl, PhoneStateListener.LISTEN_NONE);

        tm = null;
        psl = null;

        Toast.makeText(getBaseContext(), "The SignalStrength Service is stopped ...", Toast.LENGTH_SHORT).show();

        //this.stopSelf();
        //super.onDestroy();
    }


    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    public class SignalStrengthServicerBinder extends Binder {
        public SignalStrengthService getService() {
            return SignalStrengthService.this;
        }
    }




    // set boolean for if signal Strength Case has changed
    private static Boolean significantSignalStrengthChange (String oldCase, String newCase) {
        Log.d(TAG, "signalstregth :" + signalStrength);

        if(oldCase != null && newCase != null) {
            if (oldCase.equals(newCase)) {
                return false;
            }
            return true;
        }
        else
            return true;

    }

    // Set signal strength case
    private static void setSignalStrengthCase(Integer signalStrengthCase){
        if (signalStrength >= 0 && signalStrength < 10 ) {
            currentCase = "0";
        }

        if (signalStrength >= 10 && signalStrength < 20 ) {
            currentCase = "1";
        }

        if (signalStrength >= 20 && signalStrength <= 31 ) {
            currentCase = "2";
        }
    }

    // Get current signal strength case
    private static String getCurrentCase(){
        return currentCase;
    }

}

