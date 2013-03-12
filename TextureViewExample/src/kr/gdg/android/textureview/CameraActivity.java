
package kr.gdg.android.textureview;

import java.io.IOException;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.FrameLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class CameraActivity extends Activity {
    public static String TAG = "CameraActivity";

    private CameraSurfaceTextureListener mCameraSurfaceTextureListener;
    private OrientationEventListener mOrientationEventListener;
    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_content);

        final AdView adView = new AdView(this, AdSize.SMART_BANNER,
                "a1513f5a0d88abc");
        final FrameLayout adContainer = (FrameLayout) findViewById(R.id.adContainer);
        adContainer.addView(adView);
        AdRequest adRequest = new AdRequest();
        //adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adView.loadAd(adRequest);

        mCameraSurfaceTextureListener = new CameraSurfaceTextureListener(
                this);

        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mTextureView
                .setSurfaceTextureListener(mCameraSurfaceTextureListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_texture_view_camera,
                menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.increase_alpha:
                mTextureView.setAlpha(mTextureView.getAlpha() + 0.1f);
                return true;
            case R.id.decrease_alpha:
                mTextureView.setAlpha(mTextureView.getAlpha() - 0.1f);
                return true;
            case R.id.rotate_left:
                mTextureView.setRotation(mTextureView.getRotation() - 5f);
                return true;
            case R.id.rotate_right:
                mTextureView.setRotation(mTextureView.getRotation() + 5f);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(this,
                    SensorManager.SENSOR_DELAY_NORMAL) {
                private int mOrientation;

                @Override
                public void onOrientationChanged(int orientation) {
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != Surface.ROTATION_0) {
                            mOrientation = Surface.ROTATION_0;
                        }
                    } else if (orientation >= 45 && orientation < 135) {
                        if (mOrientation != Surface.ROTATION_90) {
                            mOrientation = Surface.ROTATION_90;
                        }
                    } else if (orientation >= 135 && orientation < 225) {
                        if (mOrientation != Surface.ROTATION_180) {
                            mOrientation = Surface.ROTATION_180;
                        }
                    } else if (mOrientation != Surface.ROTATION_270) {
                        mOrientation = Surface.ROTATION_270;
                    }

                    if (lastOrientation != mOrientation) {
                        Log.d("!!!!", "rotation!!! lastOrientation:"
                                + lastOrientation + " mOrientation:"
                                + mOrientation + " orientaion:"
                                + orientation);
                    }
                }
            };
        }

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationEventListener.disable();
    }
}

interface CameraHelper {
    Camera getCamera();
}

class CameraSurfaceTextureListener implements
        SurfaceTextureListener {
    private Camera mCamera;
    private Activity mActivity;
    private CameraInfo mBackCameraInfo;

    public CameraSurfaceTextureListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
            int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(
            SurfaceTexture surface,
            int width, int height) {
        Log.d("!!!!", "onSurfaceTextureAvailable!!!");
        Pair<CameraInfo, Integer> backCamera = getBackCamera();
        final int backCameraId = backCamera.second;
        mBackCameraInfo = backCamera.first;
        mCamera = Camera.open(backCameraId);
        cameraDisplayRotation();

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void cameraDisplayRotation() {
        final int rotation = mActivity.getWindowManager()
                .getDefaultDisplay().getRotation();
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

        final int displayOrientation = (mBackCameraInfo.orientation
                - degrees + 360) % 360;
        mCamera.setDisplayOrientation(displayOrientation);
    }

    private Pair<CameraInfo, Integer> getBackCamera() {
        CameraInfo cameraInfo = new CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                return new Pair<Camera.CameraInfo, Integer>(cameraInfo,
                        Integer.valueOf(i));
            }
        }
        return null;
    }

    public boolean isCameraOpen() {
        return mCamera != null;
    }
}
