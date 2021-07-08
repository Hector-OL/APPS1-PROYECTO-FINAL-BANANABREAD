package com.example.proyecto_foni;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.FileViewerViewHolder> implements OnDatabaseChangedListener {

    Context context;
    ArrayList<RecordingAudio> arrayList;
    LinearLayoutManager llm;
    DataBase db;

    public FileViewerAdapter(Context context, ArrayList<RecordingAudio> arrayList, LinearLayoutManager llm) {
        this.context = context;
        this.arrayList = arrayList;
        this.llm = llm;

        db = new DataBase(context);
        db.setOnDatabaseChangedListener(this);
    }

    @Override
    public void onNewDatabaseEntryAdded(RecordingAudio recordingAudio) {
        arrayList.add(recordingAudio);
        notifyItemInserted(arrayList.size() - 1);
    }

    @NonNull
    @Override
    public FileViewerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_view, parent, false);
        return new FileViewerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewerViewHolder holder, int position) {
        RecordingAudio recordingAudio = arrayList.get(position);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(recordingAudio.getLength());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(recordingAudio.getLength()) - TimeUnit.MINUTES.toSeconds(minutes);

        holder.txtVwName.setText(recordingAudio.getName());
        holder.txtVwLength.setText(String.format("%02d;%02d", minutes, seconds));
        holder.txtVwTime.setText(DateUtils.formatDateTime(context, recordingAudio.getTime_added(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class FileViewerViewHolder extends RecyclerView.ViewHolder {

        TextView txtVwName;
        TextView txtVwLength;
        TextView txtVwTime;
        View card_view;

        public FileViewerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            txtVwName = itemView.findViewById(R.id.txtVwName);
            txtVwLength = itemView.findViewById(R.id.txtVwLength);
            txtVwTime = itemView.findViewById(R.id.txtVwTime);
            card_view = itemView.findViewById(R.id.crdVw);

            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){

                    AudioPlayback audioPlayback = new AudioPlayback(db);
                    Bundle b = new Bundle();

                    b.putSerializable("item", arrayList.get(getAdapterPosition()));
                    audioPlayback.setArguments(b);

                    FragmentTransaction fragmentTransaction = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();

                    audioPlayback.show(fragmentTransaction, "dialog_playback");
                }
            });
        }
    }
}
