
package kr.gdg.android;

import java.io.IOException;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.GetChars;
import android.util.Log;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CameraActivity extends Activity {
    public static String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_content);
        final TextureView textureView = (TextureView) findViewById(R.id.texture_view);
        textureView
                .setSurfaceTextureListener(new CameraSurfaceTextureListener());
        Button leftButton = (Button) findViewById(R.id.rotate_left);
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float rotation = textureView.getRotation();
                textureView.setRotation(rotation - 5f);
                Log.d(TAG, "rotation: " + rotation);
            }
        });

        Button rightButton = (Button) findViewById(R.id.rotate_right);
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float rotation = textureView.getRotation();
                textureView.setRotation(rotation + 5f);
                Log.d(TAG, "rotation: " + rotation);
            }
        });

        Button increaseButton = (Button) findViewById(R.id.increse_alpha);
        increaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float alpha = textureView.getAlpha();
                textureView.setAlpha(alpha + 0.1f);
                Log.d(TAG, "alpha: " + alpha);
            }
        });

        Button decreaseButton = (Button) findViewById(R.id.decrease_alpha);
        decreaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float alpha = textureView.getAlpha();
                textureView.setAlpha(alpha - 0.1f);
                Log.d(TAG, "alpha: " + alpha);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_texture_view_camera,
                menu);
        return true;
    }
}

class CameraSurfaceTextureListener implements
        SurfaceTextureListener {

    private Camera mCamera;

    public CameraSurfaceTextureListener() {
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
    }
}
