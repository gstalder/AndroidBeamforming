package ch.ethz.tik.androidbeamforming;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * Created by Jonas Stehli on 4/16/2017.
 * Captures Audio Data from MicCapture & writes to socket.
 */

public class MicCaptureToSocket {

    private MicCapture recorder;
    private Thread writeThread;
    private Socket socket;
    private String host;
    private int port;
    private OutputStream os;

    private boolean isRunning = false;


    public MicCaptureToSocket (String host, int port) {
        this.host = host;
        this.port = port;
        socket = new Socket();
    }

    public void start() {

        recorder = new MicCapture();
        if (isRunning) return;
        isRunning = true;

        recorder.startRecording();


        writeThread = new Thread(new Runnable() {
            //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); //MAY HELP???
            public void run() {
                try {
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), 500); // second argument: timeout value!
                } catch (IOException e) {
                    e.printStackTrace();
                }



                writeAudioDataToSocket();
            }
        }, "AudioToSocket Thread");
        writeThread.start();

    }

    public void stop() {
        isRunning = false;
        recorder.stopRecording();
    }

    private void writeAudioDataToSocket() {

        try {
            os = socket.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        while (isRunning) {

            short[] data = recorder.getData();
            try {
                // writes the data to file from buffer
                os.write(short2byte(data));
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

}
