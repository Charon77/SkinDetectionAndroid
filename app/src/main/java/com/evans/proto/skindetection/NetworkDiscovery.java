package com.evans.proto.skindetection;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Evans on 30-Jul-17.
 */

public class NetworkDiscovery extends AsyncTask<Void, Void, String> {
    final int discoveryPort = 57700;

    AsyncTaskResult handler;

    public NetworkDiscovery(AsyncTaskResult handler)
    {
        this.handler = handler;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            //Sets up socket
            DatagramSocket datagramSocket = new DatagramSocket();

            datagramSocket.setBroadcast(true);
            InetAddress address = InetAddress.getByName("255.255.255.255");
            //datagramSocket.connect(address, discoveryPort);

            datagramSocket.setSoTimeout(3000);


            byte[] discoveryBuff = new byte []{'C', 'L'};
            DatagramPacket writePacket = new DatagramPacket(discoveryBuff, discoveryBuff.length, address, discoveryPort);


            DatagramPacket receivePacket = new DatagramPacket(new byte[200], 200);
            boolean retry = true;
            while (retry) {
                datagramSocket.send(writePacket);
                try {
                    retry = false;
                    datagramSocket.receive(receivePacket);

                    Log.d("received from", receivePacket.getAddress().getHostAddress());
                    return receivePacket.getAddress().getHostAddress();

                } catch (SocketTimeoutException e)
                {
                    retry = true;
                }
                Thread.sleep(1000);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        handler.onServerFound(s);
    }

}
