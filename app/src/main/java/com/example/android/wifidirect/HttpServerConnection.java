package com.example.android.wifidirect;

/**
 * Created by rakel on 21.05.2018.
 */


import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;

import static com.example.android.wifidirect.WiFiDirectActivity.TAG;


/**
 * Created by ergyspuka on 28/04/2017.
 */

public class HttpServerConnection extends Thread {


    private static Activity mParent = null;
    private String mServerAddress;
    String sentSMS;
    String receivedSMS;
    //private Context mContext;



    NetworkInfo info;



    public HttpServerConnection(String serverAddress, String messageSentToServer) {
        super();
       // this.mParent = ();
        this.mServerAddress = serverAddress;
        this.sentSMS = messageSentToServer;
    }



    public void run(){

        /*info = Connectivity.getNetworkInfo(mParent.getApplicationContext());
        if (info != null && info.isConnected()) {
            //System.out.println("info.getType(): " + info.getType());
            System.out.println("info.getTypeName(): " + info.getTypeName());
            //System.out.println("info.getSubtype(): " + info.getSubtype());
            System.out.println("info.getSubtypeName(): " + info.getSubtypeName());
            //System.out.println("isConnectedWifi(): " + isConnectedWifi(udpContext));
            //System.out.println("isConnectedMobile(): " + isConnectedMobile(udpContext));
            //System.out.println("isConnectedFast(): " + isConnectedFast(udpContext));
        } else {
            System.out.println("There is no connection available in this android mobile phone or android phone emulator");
        }*/


        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {




            System.out.println("mServerAddress: " + mServerAddress);
            in = openHttpConnection(mServerAddress);

            if (in != null) {
                isr = new InputStreamReader(in);
                br = new BufferedReader(isr);


                while ((receivedSMS = br.readLine()) != null) {


                    System.out.println("sentSMS: " + sentSMS);
                    System.out.println("receivedSMS: " + receivedSMS);

                }

                in.close();



                if(sentSMS.equals(receivedSMS)) {

                    System.out.println("Successfully connected with the server HTTP: " + mServerAddress);

                }
                else{

                    System.out.println("Unsuccessful attempt to connect with the server HTTP: " + mServerAddress);
                }



            } else {
                System.out.println("Unable to open a connection with the server: " + mServerAddress);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            System.out.println("Finally: Setting the variable that is used from the HttpClientCaller to get out of the while cycle!");
        }


    }




    // Open http connection and get inputstream from device
    private InputStream openHttpConnection(String urlStr) {

        InputStream in = null;
        int resCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }

            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");

            httpConn.connect();

            resCode = httpConn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: The connection is refused!");
            //e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException: The connection is refused!");
            //e.printStackTrace();
        }
        return in;
    }

}
