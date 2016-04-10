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
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vinexs.eeb.BaseFragment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"unused", "deprecation"})
public class BaseFragCamera extends BaseFragment {

    protected BaseFragCamera.Callback cameraCallback = null;

    public interface Callback {
        void onPreviewFrame(byte[] bytes, Camera camera);

        void onPictureTaken(byte[] data, Camera camera);

        void onShutter();
    }

    /* Views */
    private SurfaceView surfaceView = null;

    /* Environment */
    private int bestPictureWidth = 240;
    private int bestPictureHeight = 320;
    private int bestPreviewWidth = 0;
    private int bestPreviewHeight = 0;

    /* Camera Component  */
    private Camera camera;
    private boolean isPreviewing;
    private boolean isCameraSwitching;
    private int totalCamera = 0;
    private int cameraCurrentFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    /* Motion Sensor Component */
    private SensorManager sensorManager;
    private boolean accelerometerPresent;
    private Sensor accelerometerSensor;
    private float lastMotionX = 0;
    private float lastMotionY = 0;
    private float lastMotionZ = 0;

    /* Timer for auto focusing*/
    private static Timer timer = null;

    final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    final SensorEventListener accelerometerListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (Math.abs(sensorEvent.values[0] - lastMotionX) > 1 || Math.abs(sensorEvent.values[1] - lastMotionY) > 1 || Math.abs(sensorEvent.values[2] - lastMotionZ) > 1) {
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
    public int getLayoutResId() {
        return 0;
    }

    @Override
    public int getToolbarResId() {
        return 0;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            cameraCallback = (BaseFragCamera.Callback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        totalCamera = Camera.getNumberOfCameras();
        if (totalCamera == 0) {
            getActivity().finishActivity(Activity.RESULT_CANCELED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams rpl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(rpl);
        surfaceView = new SurfaceView(getActivity());
        surfaceView.setLayoutParams(rpl);
        layout.addView(surfaceView);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCamera(cameraCurrentFacing);
        resumeMotionSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseCamera();
        pauseMotionSensor();
    }

    /* Camera Component  */
    public void resumeCamera(int cameraFacing) {
        camera = Camera.open(cameraFacing);
        Log.d(getTag(), "Camera open at " + cameraFacing + ".");
        measureBestScreenEnvironment();
        projectImageToSurface();
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCameraFocus();
            }
        });
    }

    public void pauseCamera() {
        if (camera != null) {
            camera.stopPreview();
            //Delay for preview stop process.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    camera.release();
                    camera = null;
                    Log.d(getTag(), "Camera is released and close now.");
                }
            }, 500);
        }
    }

    public void projectImageToSurface() {
        try {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
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
                    camera.startPreview();
                    isPreviewing = true;
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    isPreviewing = false;
                }

                protected void setDisplayOrientation(Camera camera, int angle) {
                    try {
                        Method downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);
                        if (downPolymorphic != null) {
                            downPolymorphic.invoke(camera, angle);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    onPreviewFrameReceived(bytes, camera);
                }
            });

        } catch (Exception e) {
            Log.d(getTag(), "Exception occurred in opening camera", e);
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

    public void requestCameraFocus() {
        if (camera == null) {
            return;
        }
        try {
            camera.autoFocus(autoFocusCallback);
            Log.d(getTag(), "Focusing...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFocusingTimer();
    }

    public void switchCamera() {
        if (totalCamera >= 2 && !isCameraSwitching) {
            isCameraSwitching = true;
            final int fSwitchCameraTo = (cameraCurrentFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            pauseCamera();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    resumeCamera(fSwitchCameraTo);
                    cameraCurrentFacing = fSwitchCameraTo;
                    isCameraSwitching = false;
                }
            }, 750);
        }
    }

    public void takePicture() {
        if (camera == null) {
            return;
        }
        camera.takePicture(new Camera.ShutterCallback() {

            @Override
            public void onShutter() {
                onShutterAction();
            }
        }, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                onPictureFrameReceived(data, camera);
            }
        });
    }

    public void onPreviewFrameReceived(byte[] data, Camera camera) {
        cameraCallback.onPreviewFrame(data, camera);
    }

    public void onPictureFrameReceived(byte[] data, Camera camera) {
        cameraCallback.onPictureTaken(data, camera);
    }

    public void onShutterAction() {
        cameraCallback.onShutter();
    }

    /* Motion Sensor Component */
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

    /* Timer for auto focusing */
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

}
