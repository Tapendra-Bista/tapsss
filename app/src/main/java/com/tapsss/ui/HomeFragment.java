package com.tapsss.ui;


import android.Manifest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
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
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.util.Log;

import android.view.LayoutInflater;

import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.arthenica.ffmpegkit.FFmpegKit;
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


import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    static {
        new Handler();
    }

    private static final String PREF_VIDEO_QUALITY = "video_quality";
    private static final String QUALITY_SD = "SD";
    private static final String QUALITY_HD = "HD";
    private static final String QUALITY_FHD = "FHD";


    private TextView tvPreviewPlaceholder;
    private Button buttonStartStop;

    private boolean isPreviewEnabled = true;

    private View cardPreview;
    private Vibrator vibrator;













 // Adjust as needed

    private static final int REQUEST_PERMISSIONS = 1;



// important
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestEssentialPermissions() {
        Log.d(TAG, "requestEssentialPermissions: Requesting essential permissions");
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 and above
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else { // Below Android 11
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestEssentialPermissions: Requesting permission: " + permission);
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }
























    private void setupLongPressListener() {
        cardPreview.setOnLongClickListener(v -> {
            if (isRecording) {
                // Start scaling down animation
                cardPreview.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100) // Reduced duration for quicker scale-down
                        .start();

                // Perform haptic feedback
                performHapticFeedback();

                // Execute the task immediately
                isPreviewEnabled = !isPreviewEnabled;
                updatePreviewVisibility();
                savePreviewState();



                // Scale back up quickly with a wobble effect
                cardPreview.postDelayed(() -> cardPreview.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(50) // Shorter duration for quicker scale-up
                        .start(), 60); // No Delay to ensure it happens after the initial scaling down

            } else {
                // Handling when recording is not active




                // Ensure the placeholder is visible
                tvPreviewPlaceholder.setVisibility(View.VISIBLE);
                tvPreviewPlaceholder.setPadding(16, tvPreviewPlaceholder.getPaddingTop(), 16, tvPreviewPlaceholder.getPaddingBottom());
                performHapticFeedback();

                // Trigger the red blinking animation
                tvPreviewPlaceholder.setBackgroundColor(Color.RED);
                tvPreviewPlaceholder.postDelayed(() -> tvPreviewPlaceholder.setBackgroundColor(Color.TRANSPARENT), 100); // Blinking duration

                // Wobble animation
                ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(tvPreviewPlaceholder, "scaleX", 1.1f);
                ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(tvPreviewPlaceholder, "scaleY", 1.1f);
                ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(tvPreviewPlaceholder, "scaleX", 1.0f);
                ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(tvPreviewPlaceholder, "scaleY", 1.0f);

                scaleXUp.setDuration(50);
                scaleYUp.setDuration(50);
                scaleXDown.setDuration(50);
                scaleYDown.setDuration(50);

                AnimatorSet wobbleSet = new AnimatorSet();
                wobbleSet.play(scaleXUp).with(scaleYUp).before(scaleXDown).before(scaleYDown);
                wobbleSet.start();
            }
            return true;
        });
    }




    private void updatePreviewVisibility() {
        if (isRecording) {
            if (isPreviewEnabled) {
                textureView.setVisibility(View.VISIBLE);
                tvPreviewPlaceholder.setVisibility(View.GONE);
            } else {
                textureView.setVisibility(View.INVISIBLE);
                tvPreviewPlaceholder.setVisibility(View.VISIBLE);
                tvPreviewPlaceholder.setText("");
            }
        } else {
            textureView.setVisibility(View.INVISIBLE);
            tvPreviewPlaceholder.setVisibility(View.VISIBLE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestEssentialPermissions();
        }

        // Check if it's the first launch

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();


        Log.d(TAG, "HomeFragment resumed.");

        IntentFilter filter = new IntentFilter("RECORDING_STATE_CHANGED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().registerReceiver(recordingStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }

        updateStats();
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "HomeFragment paused.");

        requireActivity().unregisterReceiver(recordingStateReceiver);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating fragment_home layout");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    private void performHapticFeedback() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }
    private void savePreviewState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isPreviewEnabled", isPreviewEnabled);
        editor.apply();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up UI components");

        textureView = view.findViewById(R.id.textureView);

        tvPreviewPlaceholder = view.findViewById(R.id.tvPreviewPlaceholder);
        buttonStartStop = view.findViewById(R.id.buttonStartStop);















        cardPreview = view.findViewById(R.id.cardPreview);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

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
                    if (file.isFile()) {
                        file.getName();
                    }
                }
            }
        }




    }


    private final BroadcastReceiver recordingStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("RECORDING_STATE_CHANGED".equals(intent.getAction())) {
                boolean isRecording = intent.getBooleanExtra("isRecording", false);
                buttonStartStop.setText("");
                if (isRecording) {


                    tvPreviewPlaceholder.setVisibility(View.GONE);
                    textureView.setVisibility(View.VISIBLE);
                } else {


                    tvPreviewPlaceholder.setVisibility(View.VISIBLE);
                    textureView.setVisibility(View.GONE);
                }
            }
        }
    };



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
        String selectedQuality = sharedPreferences.getString(PREF_VIDEO_QUALITY, QUALITY_HD);
        long videoBitrate;
        switch (selectedQuality) {
            case QUALITY_SD:
                videoBitrate = 1000000; // 1 Mbps
                break;
            case QUALITY_HD:
                videoBitrate = 5000000; // 5 Mbps
                break;
            case QUALITY_FHD:
                videoBitrate = 10000000; // 10 Mbps
                break;
            default:
                videoBitrate = 5000000; // Default to HD
                break;
        }
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
                    cameraDevice.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "onError: Camera error: " + error);
                    cameraDevice.close();
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
            Log.e(TAG, "startRecordingVideo: TextureView is now available             550");
        }

        if (null == cameraDevice || !textureView.isAvailable() || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "startRecordingVideo: Unable to start recording due to missing prerequisites");
            return;
        }
        try {
            Log.e(TAG, "startRecordingVideo: TextureView found, success             556+");
            setupMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(720, 1080);
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
                                e.printStackTrace();
                            }
                            mediaRecorder.start();
                            requireActivity().runOnUiThread(() -> {
                                // Haptic Feedback

                                isRecording = true;
                                Toast.makeText(getContext(), "T", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "onConfigureFailed: Failed to configure camera capture session");

                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "startRecordingVideo: Camera access exception", e);
            e.printStackTrace();
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

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(videoFile.getAbsolutePath());

            String selectedQuality = sharedPreferences.getString(PREF_VIDEO_QUALITY, QUALITY_HD);
            switch (selectedQuality) {
                case QUALITY_SD:
                    mediaRecorder.setVideoSize(640, 480);
                    mediaRecorder.setVideoEncodingBitRate(1000000); // 1 Mbps
                    mediaRecorder.setVideoFrameRate(30);
                    break;
                case QUALITY_FHD:
                    mediaRecorder.setVideoSize(1920, 1080);
                    mediaRecorder.setVideoEncodingBitRate(10000000); // 10 Mbps
                    mediaRecorder.setVideoFrameRate(30);
                    break;
                default:
                    mediaRecorder.setVideoSize(1280, 720);
                    mediaRecorder.setVideoEncodingBitRate(5000000); // 5 Mbps
                    mediaRecorder.setVideoFrameRate(30);
                    break;
            }

            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            if (getCameraSelection().equals("front")) {
                mediaRecorder.setOrientationHint(270);
            } else {
                mediaRecorder.setOrientationHint(90);
            }


            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "setupMediaRecorder: Error setting up media recorder", e);
            e.printStackTrace();
        }
    }


    private void checkAndDeleteSpecificTempFile() {
        if (tempFileBeingProcessed != null) {

            // Construct tapsss_ filename with the same timestamp
            String outputFilePath = tempFileBeingProcessed.getParent() + "/tapsss_" + tempFileBeingProcessed.getName().replace("temp_", "");
            File outputFile = new File(outputFilePath);

            // Check if the tapsss_ file exists
            if (outputFile.exists()) {
                // Delete temp file
                if (tempFileBeingProcessed.delete()) {
                    Log.d(TAG, "Temp file deleted successfully.");
                } else {
                    Log.e(TAG, "Failed to delete temp file.");
                }
                // Reset tempFileBeingProcessed to null after deletion
                tempFileBeingProcessed = null;
            } else {
                // tapsss_ file does not exist yet
                Log.d(TAG, "Matching tapsss_ file not found. Temp file remains.");
            }
        }
    }



    private void startMonitoring() {
        final long CHECK_INTERVAL_MS = 1000; // 1 second

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::checkAndDeleteSpecificTempFile, 0, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }




    private void stopRecording() {
        Log.d(TAG, "stopRecording: Stopping video recording");

        // Stop the recording service
        Intent stopIntent = new Intent(getActivity(), RecordingService.class);
        stopIntent.setAction("ACTION_STOP_RECORDING");
        requireActivity().startService(stopIntent);

        if (isRecording) {
            try {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.abortCaptures();
                releaseCamera();

                Toast.makeText(getContext(), "F", Toast.LENGTH_SHORT).show();

                // Add watermarking here if necessary
                // Get the latest video file
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

                isRecording = false;
                buttonStartStop.setText("");

                tvPreviewPlaceholder.setVisibility(View.VISIBLE);
                textureView.setVisibility(View.INVISIBLE);


            } catch (CameraAccessException | IllegalStateException e) {
                Log.e(TAG, "stopRecording: Error stopping recording", e);
                e.printStackTrace();
            }
            isRecording = false;
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


        switch (watermarkOption) {
            case "timestamp":

                break;
            case "no_watermark":
                // No watermark, so just copy the video as is
                String ffmpegCommandNoWatermark = String.format("-i %s -codec copy %s", inputFilePath, outputFilePath);
                executeFFmpegCommand(ffmpegCommandNoWatermark);
                return;
            default:
                watermarkText = "Captured by tapsss...... t/iE7Ppv2QJV5k+pyebq8g==";
                break;
        }


        Log.d(TAG, "Font Path: " + fontPath);

        // Determine the font size based on the video bitrate
        int fontSize = getFontSizeBasedOnBitrate();

        // Use -q:v 0 to keep the same quality as input
        @SuppressLint("DefaultLocale") String ffmpegCommand = String.format(
                "-i %s -vf \"drawtext=text='%s':x=10:y=10:fontsize=%d:fontcolor=white:fontfile=%s\" -q:v 0 -codec:a copy %s",
                inputFilePath, watermarkText, fontSize, fontPath, outputFilePath
        );

        executeFFmpegCommand(ffmpegCommand);
    }

    private int getFontSizeBasedOnBitrate() {
        int fontSize;
        int videoBitrate = getVideoBitrate(); // Ensure this method retrieves the correct bitrate based on the selected quality

        if (videoBitrate <= 1000000) {
            fontSize = 12; //SD quality
        } else if (videoBitrate == 10000000) {
            fontSize = 24; // FHD quality
        } else {
            fontSize = 16; // HD or higher quality
        }

        Log.d(TAG, "Determined Font Size: " + fontSize);
        return fontSize;
    }

    private int getVideoBitrate() {
        String selectedQuality = sharedPreferences.getString(PREF_VIDEO_QUALITY, QUALITY_HD);
        int bitrate;
        switch (selectedQuality) {
            case QUALITY_SD:
                bitrate = 1000000; // 1 Mbps
                break;
            case QUALITY_HD:
                bitrate = 5000000; // 5 Mbps
                break;
            case QUALITY_FHD:
                bitrate = 10000000; // 10 Mbps
                break;
            default:
                bitrate = 5000000; // Default to HD
                break;
        }
        Log.d(TAG, "Selected Video Bitrate: " + bitrate + " bps");
        return bitrate;
    }

    private void executeFFmpegCommand(String ffmpegCommand) {
        Log.d(TAG, "FFmpeg Command: " + ffmpegCommand);
        FFmpegKit.executeAsync(ffmpegCommand, session -> {
            if (session.getReturnCode().isSuccess()) {
                Log.d(TAG, "Watermark added successfully.");
                //  monitoring temp files
                startMonitoring();

                // Notify the adapter to update the thumbnail

            } else {
                Log.e(TAG, "Failed to add watermark: " + session.getFailStackTrace());
            }
        });
    }


    private String getWatermarkOption() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getString("watermark_option", "timestamp_tapsss");
    }




    private void copyFontToInternalStorage() {
        AssetManager assetManager = requireContext().getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open("ubuntu_regular.ttf");
            File outFile = new File(getContext().getFilesDir(), "ubuntu_regular.ttf");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = Files.newOutputStream(outFile.toPath());
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
    }
}