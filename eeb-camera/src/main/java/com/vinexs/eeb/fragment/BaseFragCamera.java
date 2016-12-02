/*
 * Copyright (c) 2015. Vin @ vinexs.com (MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.vinexs.eeb.fragment;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.vinexs.eeb.BaseFragment;
import com.vinexs.eeb.camera.R;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"unused", "deprecation"})
public abstract class BaseFragCamera extends BaseFragment implements
        SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {

    public static final String argsFacing = "camera_facing";

    protected BaseFragCamera.Callback cameraCallback = null;

    public interface Callback {

        void onPreviewFrame(final byte[] bytes, Camera camera, final int previewWidth, final int previewHeight);

        void onPictureTaken(final byte[] data, Camera camera, final int pictureWidth, final int pictureHeight);
    }

    /* Views */
    public SurfaceView surfaceView = null;

    /* Environment */
    private int cameraType = 0;
    private int bestPictureWidth = 240;
    private int bestPictureHeight = 320;
    private int bestPreviewWidth = 0;
    private int bestPreviewHeight = 0;

    /* Camera Component  */
    private Camera camera;
    private boolean isPreviewing;
    private boolean isCameraSwitching;
    private int totalCamera = 0;
    protected int cameraCurrentFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    /* Motion Sensor Component */
    private SensorManager sensorManager;
    private boolean accelerometerPresent;
    private Sensor accelerometerSensor;
    private float lastMotionX = 0;
    private float lastMotionY = 0;
    private float lastMotionZ = 0;

    /* Timer for auto focusing*/
    private static Timer timer = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            cameraCallback = (BaseFragCamera.Callback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    final SensorEventListener accelerometerListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (Math.abs(sensorEvent.values[0] - lastMotionX) > 1 ||
                    Math.abs(sensorEvent.values[1] - lastMotionY) > 1 ||
                    Math.abs(sensorEvent.values[2] - lastMotionZ) > 1) {
                requestCameraFocus();
                lastMotionX = sensorEvent.values[0];
                lastMotionY = sensorEvent.values[1];
                lastMotionZ = sensorEvent.values[2];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        totalCamera = Camera.getNumberOfCameras();
        if (totalCamera == 0) {
            Log.e(getTag(), "No camera was found on this device.");
            getActivity().finishActivity(Activity.RESULT_CANCELED);
        }
        Log.d(getTag(), totalCamera + " camera was found on this device.");

        if (args != null && args.containsKey(argsFacing)) {
            cameraCurrentFacing = args.getInt(argsFacing);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutResId = getLayoutResId();
        if (layoutResId == 0) {
            Log.e(getTag(), "Undefined layout in fragment.");
            getActivity().finish();
            return null;
        }
        return inflater.inflate(layoutResId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int surfaceViewResId = getSurfaceResId();
        if (surfaceViewResId != 0) {
            surfaceView = (SurfaceView) view.findViewById(surfaceViewResId);
        }

        // Is scanner mode.
        int scannerBarResId = getScannerBarResId();
        if (scannerBarResId != 0) {
            ImageView scanEffect = (ImageView) view.findViewById(scannerBarResId);
            Animation scanEffectAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.lib_camera_scan_effect);
            scanEffect.startAnimation(scanEffectAnimation);
        }

        // Can switch camera
        int switchButtonResId = getSwitchCameraButtonResId();
        if (switchButtonResId != 0 && totalCamera >= 2) {
            View switchButton = view.findViewById(switchButtonResId);
            switchButton.setVisibility(View.VISIBLE);
            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchCamera();
                }
            });
        }

        // Can take picture
        int shutterButtonResId = getShutterButtonResId();
        if (shutterButtonResId != 0) {
            View shutterButton = view.findViewById(shutterButtonResId);
            shutterButton.setVisibility(View.VISIBLE);
            shutterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePicture();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCamera();
        resumeMotionSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseCamera();
        pauseMotionSensor();
    }

    /* Camera Component  */
    public void resumeCamera() {
        Log.d(getTag(), "Camera open at " + cameraCurrentFacing + ".");

        camera = Camera.open(cameraCurrentFacing);
        measureBestScreenEnvironment();
        setCameraDisplayOrientation();

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCameraFocus();
            }
        });
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
    }

    public void pauseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;

            System.gc();

            surfaceView.setOnClickListener(null);
            surfaceView.getHolder().removeCallback(this);
            Log.d(getTag(), "Camera is released and close now.");
        }
    }

    public void switchCamera() {
        if (totalCamera >= 2 && !isCameraSwitching) {
            Log.d(getTag(), "Request switching camera...");
            isCameraSwitching = true;

            pauseCamera();

            ViewGroup.LayoutParams surfaceParam = surfaceView.getLayoutParams();
            SurfaceView newSurfaceView = new SurfaceView(getActivity());
            newSurfaceView.setLayoutParams(surfaceParam);
            newSurfaceView.setId(getSurfaceResId());

            ViewGroup surfaceViewParent = (ViewGroup) surfaceView.getParent();

            surfaceViewParent.removeView(surfaceView);
            surfaceViewParent.addView(newSurfaceView);
            surfaceView = newSurfaceView;

            cameraCurrentFacing = (cameraCurrentFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;

            isCameraSwitching = false;
            resumeCamera();
        }
    }

    public void measureBestScreenEnvironment() {
        bestPictureWidth = 240;
        bestPictureHeight = 320;
        bestPreviewWidth = 0;
        bestPreviewHeight = 0;
        if (camera == null) {
            return;
        }
        List<Camera.Size> supportedPictureSizes = camera.getParameters().getSupportedPictureSizes();
        List<Camera.Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();

        // Check best sizing
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int monitorWidth = displaymetrics.widthPixels;
        int monitorHeight = displaymetrics.heightPixels;

        boolean foundByMonSize = false;
        boolean foundByRatio = false;

        // Found Preview Size By Monitor Size
        for (Camera.Size set : supportedPreviewSizes) {
            if (set.width == monitorHeight && set.height == monitorWidth) {
                foundByMonSize = true;
                bestPreviewWidth = set.width;
                bestPreviewHeight = set.height;
            }
        }

        // Found Preview Size By Monitor Ratio
        if (!foundByMonSize) {
            double monitorRatio = Double.parseDouble(monitorHeight + ".0") / Double.parseDouble(monitorWidth + ".0");
            for (Camera.Size set : supportedPreviewSizes) {
                double PreviewRatio = Double.parseDouble(set.width + ".0") / Double.parseDouble(set.height + ".0");
                if (monitorRatio == PreviewRatio && set.width >= bestPreviewWidth && set.height >= bestPreviewHeight) {
                    foundByRatio = true;
                    bestPreviewWidth = set.width;
                    bestPreviewHeight = set.height;
                }
            }
        }

        // Use Bigger
        if (!foundByMonSize && !foundByRatio) {
            for (Camera.Size set : supportedPreviewSizes) {
                if (set.width >= bestPreviewWidth && set.height >= bestPreviewHeight) {
                    bestPreviewWidth = set.width;
                    bestPreviewHeight = set.height;
                }
            }
        }

        // Force changing surface view size to preview size.
        for (Camera.Size set : supportedPictureSizes) {
            if (set.width >= bestPictureWidth && set.height >= bestPictureHeight) {
                bestPictureWidth = set.width;
                bestPictureHeight = set.height;
            }
        }

        Log.d(getTag(), "Decide Preview Size as Higher :" + bestPreviewWidth + " x " + bestPreviewHeight);
        Log.d(getTag(), "Decide Picture Size as Higher :" + bestPictureWidth + " x " + bestPictureHeight);
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraCurrentFacing, info);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void takePicture() {
        if (camera == null) {
            return;
        }
        // After checking write permission, it finally can run.
        camera.takePicture(new Camera.ShutterCallback() {

            @Override
            public void onShutter() {
                onShutterAction();
            }
        }, null, this);
    }

    // region Motion Sensor Component ==============================================================

    public void resumeMotionSensor() {
        if (sensorManager == null) {
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            accelerometerPresent = (sensorList.size() > 0);
            accelerometerSensor = sensorList.get(0);
        }
        if (accelerometerPresent) {
            sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void pauseMotionSensor() {
        if (accelerometerPresent) {
            sensorManager.unregisterListener(accelerometerListener);
        }
    }

    // endregion Motion Sensor Component ===========================================================

    // region Focusing Event =======================================================================

    public void setFocusingTimer() {
        if (timer != null) {
            timer.cancel();
        }
        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    requestCameraFocus();
                }
            }, 5000, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestCameraFocus() {
        if (camera == null) {
            return;
        }
        try {
            camera.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                }
            });
            Log.d(getTag(), "Focusing...");
        } catch (Exception ignored) {
        }
        setFocusingTimer();
    }

    // endregion Focusing Event ====================================================================

    // region Camera Callback ======================================================================
    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        cameraCallback.onPictureTaken(bytes, camera, bestPictureWidth, bestPictureHeight);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        cameraCallback.onPreviewFrame(bytes, camera, bestPreviewWidth, bestPreviewHeight);
    }

    // endregion Camera Callback ===================================================================

    // region Surface Holder Callback ==============================================================

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("Camera", "surfaceCreated");

        Camera.Parameters camParam = camera.getParameters();
        setDisplayOrientation(camera, 90);
        camParam.setPictureSize(bestPictureWidth, bestPictureHeight);
        camParam.setPreviewSize(bestPreviewWidth, bestPreviewHeight);
        camParam.setJpegQuality(100);
        camera.setParameters(camParam);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (isPreviewing) {
            camera.stopPreview();
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.setPreviewCallback(this);
        camera.startPreview();
        isPreviewing = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isPreviewing = false;
    }

    void setDisplayOrientation(Camera camera, int angle) {
        try {
            Method downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, angle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // endregion Surface Holder Callback ===========================================================

    // region Abstract Methods =====================================================================

    @Override
    public abstract int getLayoutResId();

    @Override
    public abstract int getToolbarResId();

    public abstract int getSurfaceResId();

    public abstract int getScannerBarResId();

    public abstract int getSwitchCameraButtonResId();

    public abstract int getShutterButtonResId();

    public abstract void onShutterAction();

    // endregion Abstract Methods ==================================================================
}
