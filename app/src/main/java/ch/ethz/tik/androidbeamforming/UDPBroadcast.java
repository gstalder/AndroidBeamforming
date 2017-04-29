package ch.ethz.tik.androidbeamforming;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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
        DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, port);

        try {
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
