package com.example.proyecto_foni;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordingService extends Service {

    public static final int REPEAT_INTERVAL = 40;

    MediaRecorder mediaRecorder;

    long lStartT = 0;
    long lElapsedT = 0;
    boolean isRecording = false;

    Handler handler;
    File file;
    String sFileName;
    DataBase dataBase;

    public RecordingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataBase = new DataBase(getApplicationContext());
        handler = new Handler();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    private void startRecording(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        sFileName = "audio_"+ts;
        file = new File(Environment.getExternalStorageDirectory()+"/MySoundRec/"+sFileName+".mp3");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);

        try{
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            handler.post(updateVisualizer);
            lStartT = System.currentTimeMillis();
        }catch(IOException e){
            e.printStackTrace();

        }
    }

    private void stopRecording() {
        isRecording = false;
        handler.removeCallbacks(updateVisualizer);
        mediaRecorder.stop();
        lElapsedT = (System.currentTimeMillis()-lStartT);
        mediaRecorder.release();
        Toast.makeText(getApplicationContext(),"Audio guardado en "+file.getAbsolutePath(),Toast.LENGTH_SHORT).show();

        RecordingAudio recordingAudio = new RecordingAudio(sFileName, file.getAbsolutePath(), lElapsedT, System.currentTimeMillis());
        dataBase.addRecording(recordingAudio);
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) // if we are already recording
            {
                // get the current amplitude
                int x = mediaRecorder.getMaxAmplitude();
                Intent intent = new Intent("UPDATE_VISUALIZER");
                intent.putExtra("AMPLITUDE", x);
                sendBroadcast(intent);
                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL);
            }
        }
    };

    @Override
    public void onDestroy() {
        if(mediaRecorder!=null){
            stopRecording();
        }
        super.onDestroy();
    }
}