package ch.ethz.tik.androidbeamforming;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Jonas Stehli in 2017.
 * This class initiates a recorder.
 * Audio Settings by default: 16 bit, 44.1 kHz
 */

public class MicCapture {

    private AudioRecord recorder = null;

    private static final int SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLERATE = 44100;
    private static final int CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFERSIZE = AudioRecord.getMinBufferSize(SAMPLERATE, CHANNELS, ENCODING);  // set buffersize as small as possible for minimum latency and calculate size in shorts for 16bit recording
    private static final int SHORTSIZE = BUFFERSIZE / 2; // short array size for getting data is half the size of the buffer!!!

    public int dataBit = 0;

    public MicCapture() {

        recorder = new AudioRecord(SOURCE, SAMPLERATE,
                CHANNELS, ENCODING, BUFFERSIZE);

    }

    public void startRecording() {
        recorder = new AudioRecord(SOURCE, SAMPLERATE,
                CHANNELS, ENCODING, BUFFERSIZE);
        recorder.startRecording();
    }

    public void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public short[] getData() {
        short[] data = new short[SHORTSIZE];
        recorder.read(data, 0, SHORTSIZE);
        return data;
    }

}