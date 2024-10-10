package com.tapsss.ui;



import android.content.Context;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import com.tapsss.R;


public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;



    private static final String PREF_CAMERA_SELECTION = "camera_selection";
    private static final String PREF_VIDEO_QUALITY = "video_quality";

    private static final String CAMERA_FRONT = "front";
    private static final String CAMERA_BACK = "back";
    private static final String QUALITY_SD = "SD";
    private static final String QUALITY_HD = "HD";
    private static final String QUALITY_FHD = "FHD";
    static final String PREF_LOCATION_DATA = "location_data";



    private void updateButtonAppearance(MaterialButton button, boolean isSelected) {
        button.setIconTintResource(isSelected ? R.color.black : android.R.color.transparent); // color for check icon
        button.setStrokeColorResource(isSelected ? R.color.colorPrimary : R.color.material_on_surface_stroke); // the last color is for the button that's not selected
        button.setTextColor(getResources().getColor(isSelected ? R.color.black : R.color.material_on_surface_emphasis_medium));
        button.setBackgroundColor(getResources().getColor(isSelected ? R.color.colorPrimary : android.R.color.transparent));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);


        MaterialButtonToggleGroup cameraSelectionToggle = view.findViewById(R.id.camera_selection_toggle);
        // Setup spinner items with array resource
        Spinner qualitySpinner = view.findViewById(R.id.quality_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.video_quality_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(adapter);

        // Set up camera selection toggle
        setupCameraSelectionToggle(view, cameraSelectionToggle);

        // Set up video quality spinner
        setupQualitySpinner(view, qualitySpinner);


        return view;
    }
    private static final int REQUEST_LOCATION_PERMISSION = 1;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sharedPreferences.edit().putBoolean(PREF_LOCATION_DATA, true).apply();
            } else {

                sharedPreferences.edit().putBoolean(PREF_LOCATION_DATA, false).apply();
            }
        }
    }








    private void setupCameraSelectionToggle(View view, MaterialButtonToggleGroup cameraSelectionToggle) {
        MaterialButton backCameraButton = view.findViewById(R.id.button_back_camera);
        MaterialButton frontCameraButton = view.findViewById(R.id.button_front_camera);

        String currentCameraSelection = sharedPreferences.getString(PREF_CAMERA_SELECTION, CAMERA_BACK);

        if (currentCameraSelection.equals(CAMERA_FRONT)) {
            cameraSelectionToggle.check(R.id.button_front_camera);
            updateButtonAppearance(frontCameraButton, true);
            updateButtonAppearance(backCameraButton, false);
        } else {
            cameraSelectionToggle.check(R.id.button_back_camera);
            updateButtonAppearance(backCameraButton, true);
            updateButtonAppearance(frontCameraButton, false);
        }

        cameraSelectionToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String selectedCamera = (checkedId == R.id.button_front_camera) ? CAMERA_FRONT : CAMERA_BACK;
                sharedPreferences.edit().putString(PREF_CAMERA_SELECTION, selectedCamera).apply();
                updateButtonAppearance(backCameraButton, checkedId == R.id.button_back_camera);
                updateButtonAppearance(frontCameraButton, checkedId == R.id.button_front_camera);
            }
        });
    }

    private void setupQualitySpinner(View view, Spinner qualitySpinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.video_quality_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(adapter);

        // Set the selected item based on the saved preference
        String selectedQuality = sharedPreferences.getString(PREF_VIDEO_QUALITY, QUALITY_HD);
        int selectedIndex = 1; // Default to HD
        switch (selectedQuality) {
            case QUALITY_FHD:
                selectedIndex = 0;
                break;
            case QUALITY_SD:
                selectedIndex = 2;
                break;
        }
        qualitySpinner.setSelection(selectedIndex);

        // Save the selected quality when user changes it
        qualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuality = QUALITY_HD;
                switch (position) {
                    case 0:
                        selectedQuality = QUALITY_FHD;
                        break;
                    case 2:
                        selectedQuality = QUALITY_SD;
                        break;
                }
                sharedPreferences.edit().putString(PREF_VIDEO_QUALITY, selectedQuality).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }










}