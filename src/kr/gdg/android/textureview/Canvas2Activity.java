/**
 * RenderThread.run is imported from Romain Guy's code.
 */

package kr.gdg.android.textureview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.FrameLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class Canvas2Activity extends Activity {
	public static String TAG = "CanvasActivity";
	private TextureView mTextureView;
	private RenderThread mThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		final AdView adView = new AdView(this, AdSize.SMART_BANNER,
				"a1513f5a0d88abc");
		final FrameLayout adContainer = (FrameLayout) findViewById(R.id.adContainer);
		adContainer.addView(adView);
		AdRequest adRequest = new AdRequest();
		// adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
		adView.loadAd(adRequest);

		mTextureView = (TextureView) findViewById(R.id.texture_view);
		mTextureView.setSurfaceTextureListener(new CanvasListener());
		mTextureView.setOpaque(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, ListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
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

	private class RenderThread extends Thread {
		private volatile boolean mRunning = true;

		@Override
		public void run() {
			float x = 0.0f;
			float y = 0.0f;
			float speedX = 5.0f;
			float speedY = 3.0f;

			Paint paint = new Paint();
			paint.setColor(0xff00ff00);

			while (mRunning && !Thread.interrupted()) {
				final Canvas canvas = mTextureView.lockCanvas(null);
				try {
					canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
					canvas.drawRect(x, y, x + 20.0f, y + 20.0f, paint);
				} finally {
					mTextureView.unlockCanvasAndPost(canvas);
				}

				if (x + 20.0f + speedX >= mTextureView.getWidth()
						|| x + speedX <= 0.0f) {
					speedX = -speedX;
				}
				if (y + 20.0f + speedY >= mTextureView.getHeight()
						|| y + speedY <= 0.0f) {
					speedY = -speedY;
				}

				x += speedX;
				y += speedY;

				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// Interrupted
				}
			}
		}

		public void stopRendering() {
			interrupt();
			mRunning = false;
		}

	}

	private class CanvasListener implements SurfaceTextureListener {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height) {
			Log.d(TAG, "onSurfaceTextureAvailable");
			mThread = new RenderThread();
			mThread.start();
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			Log.d(TAG, "onSurfaceTextureDestroyed");
			if (mThread != null) {
				mThread.stopRendering();
			}
			return true;
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height) {
			Log.d(TAG, "onSurfaceTextureSizeChanged");
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			Log.d(TAG, "onSurfaceTextureUpdated");
		}

	}
}
