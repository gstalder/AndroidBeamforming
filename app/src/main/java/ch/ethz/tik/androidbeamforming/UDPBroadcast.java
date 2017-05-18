package ch.ethz.tik.androidbeamforming;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Jonas Stehli on 4/29/2017.
 */

public class UDPBroadcast {


    private DatagramSocket socket;
    private Thread listenThread;

    private boolean hasReceived = false;
    private int port = 0;

    public UDPBroadcast(int port, InetAddress address) {
        this.port = port;

        try {
            socket = new DatagramSocket(port, address);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    // constructor for listening only!!
    public UDPBroadcast(int port) {
        this.port = port;

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void send (String message, InetAddress address) {

        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = null;
        try {
            sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    address, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final DatagramPacket finalSendPacket = sendPacket;
        Thread sendThread = new Thread(new Runnable() {
            public void run() {

                try {
                    socket.send(finalSendPacket);
                    Log.d("UDP Broadcast", "start packet sent");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, "Send Thread");
        sendThread.start();

    }

    public void listenFor (final String message, final TextView textView) {

        final byte[] listenData = message.getBytes();
        final DatagramPacket listenPacket = new DatagramPacket(listenData, listenData.length);

        listenThread = new Thread(new Runnable() {
            public void run() {

                while(hasReceived == false) {
                    try {
                        socket.receive(listenPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] receivedData = listenPacket.getData();
                    if(receivedData.equals(listenData)) hasReceived = true;
                    textView.setText("UDP received: " + receivedData.toString());
                }
            }
        }, "Listening Thread");
        listenThread.start();
    }

    InetAddress getBroadcastAddress(Context context) throws IOException {

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        Toast.makeText(context, "" + InetAddress.getByAddress(quads), Toast.LENGTH_LONG).show();
        return InetAddress.getByAddress(quads);
    }

    public boolean checkReceived() {
        if(hasReceived == true) {
            hasReceived = false;
            return true;
        }
        else return false;
    }

}