package ch.ethz.tik.androidbeamforming;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by Jonas Stehli on 5/25/2017.
 */

public class SystemClockSync {

    UDPBroadcast udpBroadcast;
    DatagramSocket socket;
    int port;

    long currentTime;
    long hostTime = 0;
    long clientOffset = 0;

    private static String TAG = SystemClockSync.class.getSimpleName();

    //threads
    Thread listenThread;

    public SystemClockSync(UDPBroadcast udpBroadcast) {
        this.udpBroadcast = udpBroadcast;
        this.port = MainActivity.UDP_BROADCAST_PORT;
        Log.d(TAG, "SSS Constructor: created!!!!");
    }

    public void startHostSync(final List<InetAddress> clientAddressList) {
        socket = udpBroadcast.getSocket();

        Thread sendThread = new Thread(new Runnable() {
            public void run() {
                // wait a moment to make sure clients are ready
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (int i = 1; i <= 10; i++) {

                    for (int k  = 0; k < clientAddressList.size(); k++) {
                        byte[] sendData = longToBytes(System.currentTimeMillis());
                        try {
                            DatagramPacket sendPacket = new DatagramPacket(
                                    sendData, sendData.length,
                                    clientAddressList.get(k), port);
                            socket.send(sendPacket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }, "Send Thread");
        sendThread.start();

    }

    public void startClientSync() {

        socket = udpBroadcast.getSocket();
        final List<Long> offsetList = new ArrayList<>();

        final byte[] listenData = new byte[8];
        final DatagramPacket listenPacket = new DatagramPacket(listenData, listenData.length);
        Log.d(TAG, "zeile 80");
        listenThread = new Thread(new Runnable() {
            public void run() {

                while(hostTime != -1) {
                    try {
                        socket.receive(listenPacket);
                        currentTime = System.currentTimeMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] receivedData = listenPacket.getData();
                    Log.d(TAG, "data received");
                    hostTime = bytesToLong(receivedData);
                    long offset = currentTime - hostTime;
                    offsetList.add(offset);
                }
            }
        }, "Listening Thread");
        listenThread.start();

    }

    public long getClientOffset(){
        return clientOffset;
    }



    @NonNull
    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    private long calculateAverage(List <Long> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Long mark : marks) {
            sum += mark;
        }

        return Math.round(sum / marks.size());
    }

}