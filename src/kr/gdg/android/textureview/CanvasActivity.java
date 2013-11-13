package kr.gdg.android.textureview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

public class CanvasActivity extends Activity {
	public static String TAG = "CanvasActivity";
	private TextureView mTextureView;
	private RenderThread mThread;
	private int mWidth;
	private int mHeight;

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
		private int sx, sy, ex, ey;
		private boolean sxToRight, syToBottom;
		private boolean exToRight, eyToBottom;

		@Override
		public void run() {
			Paint paint = new Paint();
			paint.setColor(0xff00ff00);
			paint.setColor(Color.RED);
			
			sx = (int) (Math.random() * mWidth);
			sy = (int) (Math.random() * mHeight);
			ex = (int) (Math.random() * mWidth);
			ey = (int) (Math.random() * mHeight);

			while (mRunning && !Thread.interrupted()) {
				final Canvas canvas = mTextureView.lockCanvas(null);
				try {
					canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);

					
					int strokeWidth = 5;
					paint.setStrokeWidth(strokeWidth);
					paint.setStyle(Paint.Style.STROKE);
					
					Path path = new Path();
					path.moveTo(sx, sy);
					path.lineTo(ex, ey);
					
					canvas.drawPath(path, paint);
					
				} finally {
					mTextureView.unlockCanvasAndPost(canvas);
				}
				
				if (sxToRight) {
					sx += 3;
					if (sx >= mWidth) {
						sxToRight = false;
					}
				} else {
					sx -= 3;
					if (sx < 0) {
						sxToRight = true;
					}
				}

				if (syToBottom) {
					sy += 3;
					if (sy >= mHeight) {
						syToBottom = false;
					}
				} else {
					sy -= 3;
					if (sy < 0) {
						syToBottom = true;
					}
				}
				
				if (exToRight) {
					ex += 3;
					if (ex >= mWidth) {
						exToRight = false;
					}
				} else {
					ex -= 3;
					if (ex < 0) {
						exToRight = true;
					}
				}

				if (eyToBottom) {
					ey++;
					if (ey >= mHeight) {
						eyToBottom = false;
					}
				} else {
					ey--;
					if (ey < 0) {
						eyToBottom = true;
					}
				}
				
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
			mWidth = mTextureView.getWidth();
			mHeight = mTextureView.getHeight();
			Log.d(TAG, "width: " + mWidth + " height: " + height);
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
			mWidth = mTextureView.getWidth();
			mHeight = mTextureView.getHeight();
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			Log.d(TAG, "onSurfaceTextureUpdated");
		}
	}
}
