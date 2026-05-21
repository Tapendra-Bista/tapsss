package com.tapsss.ui;


import android.Manifest;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;


import android.util.Log;

import android.view.LayoutInflater;

import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.tapsss.R;
import com.tapsss.RecordingService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;



import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";


    private File tempFileBeingProcessed;



   // Declare here

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private TextureView textureView;
    private SharedPreferences sharedPreferences;

    private TextView tvPreviewPlaceholder;
    private Button buttonStartStop;

    private boolean isPreviewEnabled = true;

    private View cardPreview;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Log.d(TAG, "All essential permissions granted");
                } else {
                    Log.d(TAG, "Some essential permissions denied");
                }
            });














 // Adjust as needed

    private static final int REQUEST_PERMISSIONS = 1;



// important
    private void requestEssentialPermissions() {
        Log.d(TAG, "requestEssentialPermissions: Requesting essential permissions");
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }
























    private void setupLongPressListener() {
        cardPreview.setOnLongClickListener(v -> true);
    }




    private void updatePreviewVisibility() {
        if (isRecording) {
            if (isPreviewEnabled) {
                textureView.setVisibility(View.INVISIBLE);
                tvPreviewPlaceholder.setVisibility(View.INVISIBLE);
            } else {
                textureView.setVisibility(View.INVISIBLE);
                tvPreviewPlaceholder.setVisibility(View.INVISIBLE);
                tvPreviewPlaceholder.setText("");
            }
        } else {
            textureView.setVisibility(View.INVISIBLE);
            tvPreviewPlaceholder.setVisibility(View.INVISIBLE);
            tvPreviewPlaceholder.setText("");
        }

        updateCameraPreview();
    }
    private void updateCameraPreview() {
        if (cameraCaptureSession != null && captureRequestBuilder != null && textureView.isAvailable()) {
            try {
                SurfaceTexture texture = textureView.getSurfaceTexture();
                if (texture == null) {
                    Log.e(TAG, "updateCameraPreview: SurfaceTexture is null");
                    return;
                }

                Surface previewSurface = new Surface(texture);

                captureRequestBuilder.removeTarget(previewSurface);
                if (isPreviewEnabled && isRecording) {
                    captureRequestBuilder.addTarget(previewSurface);
                }

                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error updating camera preview", e);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

//        requestLocationPermission();
        Log.d(TAG, "HomeFragment created.");

        // Request essential permissions on every launch
        requestEssentialPermissions();

        // Check if it's the first launch

    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)






    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating fragment_home layout");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up UI components");

        textureView = view.findViewById(R.id.textureView);

        tvPreviewPlaceholder = view.findViewById(R.id.tvPreviewPlaceholder);
        buttonStartStop = view.findViewById(R.id.buttonStartStop);















        cardPreview = view.findViewById(R.id.cardPreview);


        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        isPreviewEnabled = sharedPreferences.getBoolean("isPreviewEnabled", true);

        copyFontToInternalStorage();


        updateStats();



 // Start the tip animation

        setupButtonListeners();
        setupLongPressListener();
        updatePreviewVisibility();
    }

    private boolean areEssentialPermissionsGranted() {
        boolean cameraGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean recordAudioGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        boolean storageGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 and above
            storageGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else { // Below Android 11
            storageGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        return cameraGranted && recordAudioGranted && storageGranted;
    }

    private void debugPermissionsStatus() {
        Log.d(TAG, "Camera permission: " +
                (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ? "Granted" : "Denied"));
        Log.d(TAG, "Record Audio permission: " +
                (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ? "Granted" : "Denied"));
        Log.d(TAG, "Write External Storage permission: " +
                (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ? "Granted" : "Denied"));
    }


    private void setupButtonListeners() {
        buttonStartStop.setOnClickListener(v -> {
            debugPermissionsStatus();
            if (!areEssentialPermissionsGranted()) {
                debugPermissionsStatus();
//                showPermissionsInfoDialog();
            } else {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                    updateStats();
                }
            }
        });


    }
























    private void updateStats() {
        Log.d(TAG, "updateStats: Updating video statistics");
        File recordsDir = new File(requireContext().getExternalFilesDir(null), "tapsss");


        if (recordsDir.exists()) {
            File[] files = recordsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp4")) {
                        Log.d(TAG, "updateStats: "+file);
                    }
                }
            }
        }




    }






    private void startRecording() {
        Log.d(TAG, "startRecording: Initiating video recording from home fragment");

        // Set up the camera and MediaRecorder here
        if (!isRecording) {

            if (cameraDevice == null) {
                openCamera();
            } else {
                startRecordingVideo();
            }

            setVideoBitrate();

            buttonStartStop.setText("");


            tvPreviewPlaceholder.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);


            isRecording = true;
            updatePreviewVisibility();

            // Start the recording service
            Intent startIntent = new Intent(getActivity(), RecordingService.class);
            startIntent.setAction("ACTION_START_RECORDING");
            requireActivity().startService(startIntent);
        }
    }



//recording service section

    private void setVideoBitrate() {
        long videoBitrate = 6000000; // 6 Mbps
        Log.d(TAG, "setVideoBitrate: Set to " + videoBitrate + " bps");
    }

    private String getCameraSelection() {
        return sharedPreferences.getString("camera_selection", "back");
    }

    private void openCamera() {
        Log.d(TAG, "openCamera: Opening camera");
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = manager.getCameraIdList();
            String cameraId = getCameraSelection().equals("front") ? cameraIdList[1] : cameraIdList[0];
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d(TAG, "onOpened: Camera opened successfully");
                    cameraDevice = camera;
                    startRecordingVideo();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.w(TAG, "onDisconnected: Camera disconnected");
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "onError: Camera error: " + error);
                    camera.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, "openCamera: Error opening camera", e);
            e.printStackTrace();
        }
    }

    private void startRecordingVideo() {
        Log.d(TAG, "startRecordingVideo: Setting up video recording preview area");

        // Check if TextureView is available before starting recording
        if (!textureView.isAvailable())  {
            tvPreviewPlaceholder.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.VISIBLE);
            openCamera();
            Log.e(TAG, "startRecordingVideo: TextureView is now available");
        }

        if (null == cameraDevice || !textureView.isAvailable() || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "startRecordingVideo: Unable to start recording due to missing prerequisites");
            return;
        }
        try {
            setupMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(1920, 1080);
            Surface previewSurface = new Surface(texture);
            Surface recorderSurface = mediaRecorder.getSurface();
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            if (isPreviewEnabled) {
                captureRequestBuilder.addTarget(previewSurface);
            }
            captureRequestBuilder.addTarget(recorderSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recorderSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.d(TAG, "onConfigured: Camera capture session configured");
                            HomeFragment.this.cameraCaptureSession = cameraCaptureSession;
                            try {
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "onConfigured: Error setting repeating request", e);
                            }
                            mediaRecorder.start();
                            requireActivity().runOnUiThread(() -> {
                                isRecording = true;
                            });
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "onConfigureFailed: Failed to configure camera capture session");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "startRecordingVideo: Camera access exception", e);
        }
    }

    private void setupMediaRecorder() {
        try {
            File videoDir = new File(requireActivity().getExternalFilesDir(null), "tapsss");
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }
            String timestamp = new SimpleDateFormat("yyyyMMdd_hh_mm_ssa", Locale.getDefault()).format(new Date());
            File videoFile = new File(videoDir, "temp_" + timestamp + ".mp4");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(requireContext());
            } else {
                mediaRecorder = new MediaRecorder();
            }
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            
            // Audio settings
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);

            // Video settings
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoSize(1920, 1080);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoEncodingBitRate(6000000);

            mediaRecorder.setOutputFile(videoFile.getAbsolutePath());

            if (getCameraSelection().equals("front")) {
                mediaRecorder.setOrientationHint(270);
            } else {
                mediaRecorder.setOrientationHint(90);
            }

            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "setupMediaRecorder: Error setting up media recorder", e);
        }
    }






    private void stopRecording() {
        Log.d(TAG, "stopRecording: Stopping video recording");

        // Stop the recording service
        Intent stopIntent = new Intent(getActivity(), RecordingService.class);
        stopIntent.setAction("ACTION_STOP_RECORDING");
        requireActivity().startService(stopIntent);

        if (isRecording) {
            isRecording = false;
            try {
                if (mediaRecorder != null) {
                    try {
                        mediaRecorder.stop();
                    } catch (RuntimeException e) {
                        Log.e(TAG, "stopRecording: MediaRecorder stop failed", e);
                    }
                }
                if (cameraCaptureSession != null) {
                    cameraCaptureSession.stopRepeating();
                    cameraCaptureSession.abortCaptures();
                }
                releaseCamera();

                // Add watermarking here if necessary
                File latestVideoFile = getLatestVideoFile();
                if (latestVideoFile != null) {
                    String inputFilePath = latestVideoFile.getAbsolutePath();
                    String originalFileName = latestVideoFile.getName().replace("temp_", "");
                    String outputFilePath = latestVideoFile.getParent() + "/tapsss_" + originalFileName;
                    Log.d(TAG, "Watermarking: Input file path: " + inputFilePath);
                    Log.d(TAG, "Watermarking: Output file path: " + outputFilePath);

                    tempFileBeingProcessed = latestVideoFile;
                    addTextWatermarkToVideo(inputFilePath, outputFilePath);
                } else {
                    Log.e(TAG, "No video file found.");
                }

                buttonStartStop.setText("");

                tvPreviewPlaceholder.setVisibility(View.VISIBLE);
                textureView.setVisibility(View.INVISIBLE);

            } catch (CameraAccessException | IllegalStateException e) {
                Log.e(TAG, "stopRecording: Error stopping recording", e);
            }
            updatePreviewVisibility();
        }
    }


//recording service section

    private void releaseCamera() {
        Log.d(TAG, "releaseCamera: Releasing camera resources");
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        cameraCaptureSession = null;
        captureRequestBuilder = null;
    }

// below methods are new

    private File getLatestVideoFile() {
        File videoDir = new File(requireActivity().getExternalFilesDir(null), "tapsss");
        File[] files = videoDir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        // Sort files by last modified date
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return files[0]; // Return the most recently modified file
    }






    private void addTextWatermarkToVideo(String inputFilePath, String outputFilePath) {
        String fontPath = requireContext().getFilesDir().getAbsolutePath() + "/ubuntu_regular.ttf";
        String watermarkText =  "";
        String watermarkOption = getWatermarkOption();
        
        // Use a temporary name while processing to prevent opening unfinished files
        String processingPath = outputFilePath + ".proc";

        switch (watermarkOption) {
            case "timestamp":
                break;
            case "no_watermark":
                String ffmpegCommandNoWatermark = String.format("-i %s -codec copy %s", inputFilePath, outputFilePath);
                executeFFmpegCommand(ffmpegCommandNoWatermark, inputFilePath, outputFilePath);
                return;
            default:
                watermarkText = "Captured by tapsss...... t/iE7Ppv2QJV5k+pyebq8g==";
                break;
        }

        int fontSize = getFontSizeBasedOnBitrate();

        @SuppressLint("DefaultLocale") String ffmpegCommand = String.format(
                "-i %s -vf \"drawtext=text='%s':x=10:y=10:fontsize=%d:fontcolor=white:fontfile=%s\" -c:v libx264 -crf 26 -preset superfast -c:a copy %s",
                inputFilePath, watermarkText, fontSize, fontPath, processingPath
        );

        executeFFmpegCommand(ffmpegCommand, inputFilePath, outputFilePath);
    }

    private int getFontSizeBasedOnBitrate() {
        int fontSize;
        int videoBitrate = getVideoBitrate();

        if (videoBitrate >= 6000000) {
            fontSize = 24; // FHD quality
        } else {
            fontSize = 16; // HD or lower quality
        }

        Log.d(TAG, "Determined Font Size: " + fontSize);
        return fontSize;
    }

    private int getVideoBitrate() {
        return 6000000; // 6 Mbps
    }

    private void executeFFmpegCommand(String ffmpegCommand, String inputPath, String outputPath) {
        String processingPath = outputPath + ".proc";
        Log.d(TAG, "FFmpeg Command: " + ffmpegCommand);
        FFmpegKit.executeAsync(ffmpegCommand, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                // Rename temporary processing file to final file name
                File procFile = new File(processingPath);
                File finalFile = new File(outputPath);
                if (procFile.exists() && procFile.renameTo(finalFile)) {
                    Log.d(TAG, "Video processed and renamed successfully.");
                }

                // Delete the original temp recording
                File tempFile = new File(inputPath);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            } else {
                Log.e(TAG, "Failed to process video: " + session.getFailStackTrace());
            }
        });
    }


    private String getWatermarkOption() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getString("watermark_option", "timestamp_tapsss");
    }




    private void copyFontToInternalStorage() {
        File outFile = new File(requireContext().getFilesDir(), "ubuntu_regular.ttf");
        if (outFile.exists()) {
            Log.d(TAG, "Font already exists in internal storage.");
            return;
        }
        AssetManager assetManager = requireContext().getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open("ubuntu_regular.ttf");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = Files.newOutputStream(outFile.toPath());
            } else {
                out = new java.io.FileOutputStream(outFile);
            }
            copyFile(in, out);
            Log.d(TAG, "Font copied to internal storage.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NO-OP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NO-OP
                }
            }
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }








    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up resources");

        releaseCamera();
        
        // Nullify view references to prevent memory leaks
        textureView = null;
        buttonStartStop = null;
        tvPreviewPlaceholder = null;
        cardPreview = null;
    }
}