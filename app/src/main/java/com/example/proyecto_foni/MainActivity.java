package com.example.proyecto_foni;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_PERMISSION_CODE = 1000;

    Chronometer chrmTime;
    FloatingActionButton btnRecord;
    Button btnList;
    VisualizerView visualizerView;
    Intent intent;

    private boolean mStartRecording = true;
    long timeWhenPaused = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BroadcastReceiver broadcastReceiver = new MiBroadCast();
        IntentFilter intentFilter = new IntentFilter("UPDATE_VISUALIZER");
        registerReceiver(broadcastReceiver, intentFilter);

        chrmTime = findViewById(R.id.chrmTime);
        btnRecord = findViewById(R.id.btnRecord);
        visualizerView = findViewById(R.id.visualizer);
        btnList = findViewById(R.id.btnList);

        if(!checkPermissionFromDevice()){
            requestPermission();
        }

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionFromDevice()) {
                    onRecord(mStartRecording);
                    mStartRecording = !mStartRecording;
                } else {
                    requestPermission();
                }
            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), FileViewer.class);
                startActivity(intent);
            }
        });

        btnRecord.setColorPressed(getResources().getColor(R.color.white));

    }

    private void onRecord(boolean start){
        intent = new Intent(this, RecordingService.class);


        if(start){

            btnRecord.setImageResource(R.drawable.ic_media_stop);
            //Toast.makeText(this,"La grabacion ha iniciado",Toast.LENGTH_SHORT).show();

            File folder = new File(Environment.getExternalStorageDirectory()+ "/MySoundRec");

            if(!folder.exists()){
                folder.mkdir();
            }
            chrmTime.setBase(SystemClock.elapsedRealtime());
            chrmTime.start();

            startService(intent);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            btnRecord.setImageResource(R.drawable.ic_mic_white);
            chrmTime.stop();
            chrmTime.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            stopService(intent);
            visualizerView.clear();
            visualizerView.invalidate();

        }
    }

    class MiBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            visualizerView.addAmplitude(intent.getIntExtra("AMPLITUDE", 0)); // update the VisualizeView
            visualizerView.invalidate(); // refresh the VisualizerView
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso Garantizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean checkPermissionFromDevice() {
        int iStorageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int iAudioResult = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return iStorageResult == PackageManager.PERMISSION_GRANTED && iAudioResult == PackageManager.PERMISSION_GRANTED;
    }
}