
package kr.gdg.android;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

public class TextureViewMoviePlayer extends Activity {

    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextureView = new TextureView(this);
        mTextureView
                .setSurfaceTextureListener(new MoviePlayerSurfaceTextureListener(
                        this));
        setContentView(mTextureView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.activity_texture_view_movie_player,
                menu);
        return true;
    }
}

class MoviePlayerSurfaceTextureListener implements
        SurfaceTextureListener {
    private Activity mActivity;

    public MoviePlayerSurfaceTextureListener(Activity activity) {
        mActivity = activity;
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
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(
            SurfaceTexture surfaceTexture,
            int width, int height) {
        Uri videoUri = Uri.parse("android.resource://"
                + mActivity.getPackageName() + "/" + R.raw.twi);
        MediaPlayer player = MediaPlayer.create(mActivity, videoUri);
        Surface surface = new Surface(surfaceTexture);
        player.setSurface(surface);
        player.start();
    }
}
