package ch.ethz.tik.androidbeamforming;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Jonas Stehli on 4/29/2017.
 */

public class UDPBroadcast {

    private DatagramSocket socket;
    private Thread listenThread;

    private boolean hasReceived = false;
    private int port = 0;

    public UDPBroadcast(int port) {
        this.port = port;

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void send (String message) {

        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = null;
        try {
            sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        final DatagramPacket finalSendPacket = sendPacket;
        Thread sendThread = new Thread(new Runnable() {
            public void run() {

                try {
                    socket.send(finalSendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, "Send Thread");
        sendThread.start();

        try {
            socket.setBroadcast(false);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void listenFor (final String message) {

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
                    if(receivedData == listenData) hasReceived = true;
                    ClientActivity.clientStatus.setText("UDP arrived " + hasReceived);
                }
            }
        }, "Connection Accepting Thread");
        listenThread.start();

    }

    public boolean checkReceived() {
        if(hasReceived == true) {
            hasReceived = false;
            return true;
        }
        else return false;
    }

}