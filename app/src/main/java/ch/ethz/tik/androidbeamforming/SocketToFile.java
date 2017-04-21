package ch.ethz.tik.androidbeamforming;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jonas Stehli on 4/16/2017.
 * Creates a Server Socket that receives Audio Data and writes it to a File
 */

public class SocketToFile {

    private Thread writeThread;
    private ServerSocket serverSocket;
    private Socket client;
    private String path;
    private InputStream is;
    private int readBytes;
    private File outputFile;
    private FileOutputStream fos;
    private String filename;

    private boolean isRunning = false;

    private static final int BUFFERSIZE = 1024;


    public SocketToFile (Socket socket, String filename) {
        this.client = socket;
        this.filename = filename;
    }

    public void Start() {

        if (isRunning) return;
        isRunning = true;

        path = Environment.getExternalStorageDirectory() + "/" + filename;
        outputFile = new File(path);

        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }




        writeThread = new Thread(new Runnable() {
            //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); //MAY HELP???
            public void run() {
                writeDataToFile();
            }
        }, "Socket to File Thread");
        writeThread.start();
    }

    public String Stop() {
        isRunning = false;
        return path;
    }

    private void writeDataToFile() {

        try {
            is = client.getInputStream();
            fos = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isRunning) {

            byte[] buffer = new byte[BUFFERSIZE];

            try {
                readBytes = is.read(buffer, 0, BUFFERSIZE);
                fos.write(buffer, 0, readBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
