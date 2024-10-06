package com.tapsss.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tapsss.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordsFragment extends Fragment implements RecordsAdapter.OnVideoClickListener, RecordsAdapter.OnVideoLongClickListener {

    private RecyclerView recyclerView;
    private RecordsAdapter adapter;
    private boolean isGridView = true;
    private FloatingActionButton fabToggleView;
    private FloatingActionButton fabDeleteSelected;
    private final List<File> selectedVideos = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_view_records);
        fabToggleView = view.findViewById(R.id.fab_toggle_view);
        fabDeleteSelected = view.findViewById(R.id.fab_delete_selected);


        setupRecyclerView();
        setupFabListeners();

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
        RecyclerView.LayoutManager layoutManager = isGridView ?
                new GridLayoutManager(getContext(), 2) :
                new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setupFabListeners() {
        fabToggleView.setOnClickListener(v -> toggleViewMode());
        fabDeleteSelected.setOnClickListener(v -> confirmDeleteSelected());
    }

    private void toggleViewMode() {
        // Haptic Feedback
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(effect);
            }
        }
        isGridView = !isGridView;
        setLayoutManager();
        updateFabIcons();
    }

    private void updateFabIcons() {
        fabToggleView.setImageResource(isGridView ? R.drawable.ic_list : R.drawable.ic_grid);
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
        File recordsDir = new File(getContext().getExternalFilesDir(null), "tapsss");
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        loadRecordsList(); // Load the records when the view is created
    }

    @Override
    public void onVideoClick(File video) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("VIDEO_PATH", video.getAbsolutePath());
        startActivity(intent);
    }

    @Override
    public void onVideoLongClick(File video, boolean isSelected) {
        if (isSelected) {
            selectedVideos.add(video);
        } else {
            selectedVideos.remove(video);
        }
        updateDeleteButtonVisibility();
        // Haptic Feedback
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(effect);
            }
        }
    }

    private void updateDeleteButtonVisibility() {
        fabDeleteSelected.setVisibility(selectedVideos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void confirmDeleteSelected() {
        // Haptic Feedback
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(effect);
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eradicate Video(s)? \uD83D\uDCA3")
                .setMessage("Are you absolutely, positively sure you want to nuke these video(s) out of existence? \uD83D\uDE80\uD83D\uDCA5")
                .setNegativeButton("No, Keep 'Em! \uD83D\uDE05", null)
                .setPositiveButton("Yes, Nuke 'Em! \uD83C\uDF0B", (dialog, which) -> deleteSelectedVideos())
                .show();
    }

    private void deleteSelectedVideos() {
        for (File video : selectedVideos) {
            video.delete();
        }
        selectedVideos.clear();
        updateDeleteButtonVisibility();
        loadRecordsList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.records_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            confirmDeleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAll() {
        // Haptic Feedback
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(effect);
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete All Videos")
                .setMessage("Are you sure you want to delete all videos?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAllVideos())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAllVideos() {
        File recordsDir = new File(getContext().getExternalFilesDir(null), "tapsss");
        if (recordsDir.exists()) {
            File[] files = recordsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp4")) {
                        file.delete();
                    }
                }
            }
        }
        loadRecordsList();
    }
}
