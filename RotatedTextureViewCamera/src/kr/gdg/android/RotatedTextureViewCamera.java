
package kr.gdg.android;

import java.io.IOException;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

public class RotatedTextureViewCamera extends Activity {

    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextureView = new TextureView(this);
        mTextureView
                .setSurfaceTextureListener(new CameraSurfaceTextureListener(
                        mTextureView));
        setContentView(mTextureView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.activity_rotated_texture_view_camera,
                menu);
        return true;
    }
}

class CameraSurfaceTextureListener implements
        SurfaceTextureListener {

    private Camera mCamera;
    private TextureView mTextureView;

    public CameraSurfaceTextureListener(TextureView textureView) {
        mTextureView = textureView;
    }

    @Override
    public void onSurfaceTextureUpdated(
            SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(
            SurfaceTexture surface,
            int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(
            SurfaceTexture surface) {
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
        mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
        
        mTextureView.setRotation(0.1f);
        mTextureView.setAlpha(0.3f);
    }
}
