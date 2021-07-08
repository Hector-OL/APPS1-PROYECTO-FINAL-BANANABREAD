package com.example.proyecto_foni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class FileViewer extends AppCompatActivity {

    RecyclerView recyclerView;
    DataBase dataBase;
    private FileViewerAdapter fileViewAdapter;

    ArrayList<RecordingAudio> arrayListAudios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_viewer);
        recyclerView = findViewById(R.id.rclrVw);
        dataBase = new DataBase(this);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        llm.setOrientation(LinearLayoutManager.VERTICAL);

        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        arrayListAudios = dataBase.getAllAudios();

        if (arrayListAudios == null) {
            Toast.makeText(this, "Audios no encontrados", Toast.LENGTH_SHORT).show();
        } else {
            fileViewAdapter = new FileViewerAdapter(this, arrayListAudios, llm);
            recyclerView.setAdapter(fileViewAdapter);
        }
    }
}