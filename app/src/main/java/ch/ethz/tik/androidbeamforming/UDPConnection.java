package ch.ethz.tik.androidbeamforming;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Jonas Stehli on 4/29/2017.
 */

public class UDPConnection {

    DatagramSocket socket;
    InetAddress localAddress;
    InetAddress broadcastAddress;
    int port = 0;

    public void broadcast (String message) {

        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);

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

    }

    public void send (String message, InetAddress host) {

    }

}
