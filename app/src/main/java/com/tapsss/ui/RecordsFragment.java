package com.tapsss.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.tapsss.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordsFragment extends Fragment implements RecordsAdapter.OnVideoClickListener, RecordsAdapter.OnVideoLongClickListener {

    private RecyclerView recyclerView;
    private RecordsAdapter adapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view_records);




        setupRecyclerView();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordsList(); // Load the list in the background
    }

    private void setupRecyclerView() {
        setLayoutManager();
        adapter = new RecordsAdapter(getContext(), new ArrayList<>(), this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
    }







    @SuppressLint("NotifyDataSetChanged")
    private void loadRecordsList() {
        executorService.submit(() -> {
            List<File> recordsList = getRecordsList();
            requireActivity().runOnUiThread(() -> adapter.updateRecords(recordsList));
        });
    }

    private List<File> getRecordsList() {
        List<File> recordsList = new ArrayList<>();
        File recordsDir = new File(requireContext().getExternalFilesDir(null), "tapsss");
        if (recordsDir.exists()) {
            // Introduce a delay before refreshing the list
            new Handler(Looper.getMainLooper()).postDelayed(this::loadRecordsList, 500);
            File[] files = recordsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp4")) {
                        recordsList.add(file);
                    }
                }
            }
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::loadRecordsList, 500);
        return recordsList;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        loadRecordsList(); // Load the records when the view is created
    }

    @Override
    public void onVideoClick(File video) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("VIDEO_PATH", video.getAbsolutePath());
        startActivity(intent);
    }









    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.records_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }




    @Override
    public void onVideoLongClick(File video, boolean isSelected) {

    }
}
