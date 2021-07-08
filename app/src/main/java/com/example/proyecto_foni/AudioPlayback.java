package com.example.proyecto_foni;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;


public class AudioPlayback extends DialogFragment {

    private RecordingAudio item;
    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    DataBase dataBase;

    private boolean isPlaying = false;

    long minutes = 0;
    long seconds = 0;

    TextView txtFileName;
    TextView txtLength;
    TextView txtProgress;
    SeekBar skBar;
    FloatingActionButton btnPlayR;
    Button btnDelete;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = (RecordingAudio)getArguments().getSerializable("item");

        minutes = TimeUnit.MILLISECONDS.toMinutes(item.getLength());
        seconds = TimeUnit.MILLISECONDS.toSeconds(item.getLength()) - TimeUnit.MINUTES.toSeconds(minutes);

    }

    public AudioPlayback(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.playback, null);
        ButterKnife.bind(this, view);
        txtFileName = view.findViewById(R.id.txtFileName);
        txtLength = view.findViewById(R.id.txtLength);
        txtProgress = view.findViewById(R.id.txtProgress);
        skBar = view.findViewById(R.id.skBar);
        btnPlayR = view.findViewById(R.id.btnPlayR);
        btnDelete = view.findViewById(R.id.btnDelete);
        setSeekBarValues();

        btnPlayR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onPlay(isPlaying);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isPlaying = !isPlaying;
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext()).setTitle("Eliminar audio")
                        .setMessage("¿Seguro que deseas eliminar el audio?")
                        .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(isPlaying){
                                    mediaPlayer.stop();
                                }
                                dataBase.deleteRecording(item.getName());
                                dismiss();
                                getActivity().recreate();

                            }
                        })
                        .setNeutralButton("CANCELAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        txtFileName.setText(item.getName());
        txtLength.setText(String.format("%02d:%02d", minutes, seconds));

        builder.setView(view);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return builder.create();
    }

    private void onPlay(boolean isPlaying) throws IOException {
        if(!isPlaying){
            if (mediaPlayer == null){
                starPlaying();
            } else {
                resumePlaying();
            }
        } else {
            pausePlaying();
        }
    }

    private void pausePlaying() {
        btnPlayR.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.pause();
    }

    private void resumePlaying() {
        btnPlayR.setImageResource(R.drawable.ic_media_pause);
        mediaPlayer.start();

        updateSeekBar();
    }

    private void starPlaying() throws IOException{
        btnPlayR.setImageResource(R.drawable.ic_media_pause);
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setDataSource(item.getPath());
        mediaPlayer.prepare();
        skBar.setMax(mediaPlayer.getDuration());

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });

        updateSeekBar();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setSeekBarValues() {
        ColorFilter colorFilter = new LightingColorFilter(getResources().getColor(R.color.black),
                getResources().getColor(R.color.black));

        skBar.getProgressDrawable().setColorFilter(colorFilter);
        skBar.getThumb().setColorFilter(colorFilter);

        skBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                    handler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) - TimeUnit.MINUTES.toSeconds(minutes);

                    txtProgress.setText(String.format("%02d:%02d", minutes, seconds));

                    updateSeekBar();
                }else if(mediaPlayer == null && fromUser){
                    try {
                        prepareMediaPlayerFromPoint(progress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void prepareMediaPlayerFromPoint(int progress) throws IOException {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setDataSource(item.getPath());
        mediaPlayer.prepare();
        skBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.seekTo(progress);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });
    }

    private void stopPlaying() {
        btnPlayR.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        skBar.setProgress(skBar.getMax());
        isPlaying = !isPlaying;

        txtProgress.setText(txtLength.getText());
        skBar.setProgress(skBar.getMax());
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                skBar.setProgress(mCurrentPosition);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition) - TimeUnit.MINUTES.toSeconds(minutes);

                txtProgress.setText(String.format("%02d:%02d", minutes, seconds));
                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(mRunnable, 1000);
    }
}
