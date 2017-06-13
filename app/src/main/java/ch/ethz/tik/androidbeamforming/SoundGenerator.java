package ch.ethz.tik.androidbeamforming;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Gabriel on 13.06.2017.
 */

public class SoundGenerator {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private static String TAG = SoundGenerator.class.getSimpleName();


    private final int sampleRate = 44100;
    private int numSamples;
    private double sample[];
    double startFreq;
    double endFreq;
    int dur;

    private byte generatedSnd[] = new byte[2 * numSamples];

    public SoundGenerator(double start_freq, double end_freq, int dur){
        this.startFreq = start_freq;
        this.endFreq = end_freq;
        this.dur = dur;
    };

    public void startSound() {
        final Thread makeSound = new Thread(new Runnable() {
            public void run() {
                genTone();
                playSound();
            }
        });
        makeSound.start();
    }

    private void genTone() {
        numSamples = dur * sampleRate;
        sample = new double[numSamples];
        double currentFreq = 0;
        for (int i = 0; i < numSamples; ++i) {
            currentFreq = (startFreq*Math.pow(endFreq/startFreq,(double)i/numSamples))/2;
            if (i == 1 || i == numSamples - 1)
                Log.d(TAG, "current freq: " + currentFreq);
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / currentFreq));
        }
        convertToPCM(numSamples);
    }

    void playSound(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

    private void convertToPCM(int numSamples) {
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        generatedSnd = new byte[2 * numSamples];
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

}
