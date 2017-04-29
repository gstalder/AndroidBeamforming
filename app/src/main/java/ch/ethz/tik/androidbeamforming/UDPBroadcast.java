package ch.ethz.tik.androidbeamforming;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Jonas Stehli on 4/29/2017.
 */

public class UDPBroadcast {

    DatagramSocket socket;
    int port = 0;

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
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, port);

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

}
