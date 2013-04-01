/*
 * Originally from Romain Guy (romainguy@urious-creature.com)'s source code.
 */

package kr.gdg.android.textureview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL11;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
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

public class GLTriangleActivity extends Activity {
    private TextureView mTextureView;
    private FloatBuffer mVertices;
    private final float[] mVerticesData = {
            0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f
    };

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

        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);

        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mTextureView
                .setSurfaceTextureListener(new GLSurfaceTextureListener());
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
        private static final int EGL_OPENGL_ES2_BIT = 4;
        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private static final String TAG = "RenderThread";
        private SurfaceTexture mSurface;
        private EGLDisplay mEglDisplay;
        private EGLSurface mEglSurface;
        private EGLContext mEglContext;
        private int mProgram;
        private EGL10 mEgl;
        private GL11 mGl;

        public RenderThread(SurfaceTexture surface) {
            mSurface = surface;
        }

        @Override
        public void run() {
            initGL();

            int attribPosition = GLES20.glGetAttribLocation(mProgram,
                    "position");
            checkGlError();

            GLES20.glEnableVertexAttribArray(attribPosition);
            checkGlError();

            GLES20.glUseProgram(mProgram);
            checkGlError();

            while (true) {
                checkCurrent();

                mVertices.position(0);
                GLES20.glVertexAttribPointer(attribPosition, 3,
                        GLES20.GL_FLOAT, false, 0, mVertices);
                checkGlError();

                GLES20.glClearColor(1.0f, 1.0f, 0, 0);
                checkGlError();

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                checkGlError();

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
                Log.d(TAG, "draw!!");
                checkGlError();

                if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                    Log.e(TAG, "cannot swap buffers!");
                }
                checkEglError();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        private void checkCurrent() {
            if (!mEglContext.equals(mEgl.eglGetCurrentContext())
                    || !mEglSurface.equals(mEgl
                            .eglGetCurrentSurface(EGL10.EGL_DRAW))) {
                checkEglError();
                if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                        mEglSurface, mEglContext)) {
                    throw new RuntimeException(
                            "eglMakeCurrent failed "
                                    + GLUtils.getEGLErrorString(mEgl
                                            .eglGetError()));
                }
                checkEglError();
            }
        }

        private void checkEglError() {
            final int error = mEgl.eglGetError();
            if (error != EGL10.EGL_SUCCESS) {
                Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error));
            }
        }

        private void checkGlError() {
            final int error = mGl.glGetError();
            if (error != GL11.GL_NO_ERROR) {
                Log.e(TAG, "GL error = 0x" + Integer.toHexString(error));
            }
        }

        private int buildProgram(String vertexSource, String fragmentSource) {
            final int vertexShader = buildShader(GLES20.GL_VERTEX_SHADER,
                    vertexSource);
            if (vertexShader == 0) {
                return 0;
            }

            final int fragmentShader = buildShader(
                    GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (fragmentShader == 0) {
                return 0;
            }

            final int program = GLES20.glCreateProgram();
            if (program == 0) {
                return 0;
            }

            GLES20.glAttachShader(program, vertexShader);
            checkGlError();

            GLES20.glAttachShader(program, fragmentShader);
            checkGlError();

            GLES20.glLinkProgram(program);
            checkGlError();

            int[] status = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status,
                    0);
            checkGlError();
            if (status[0] == 0) {
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                checkGlError();
            }

            return program;
        }

        private int buildShader(int type, String shaderSource) {
            final int shader = GLES20.glCreateShader(type);
            if (shader == 0) {
                return 0;
            }

            GLES20.glShaderSource(shader, shaderSource);
            checkGlError();
            GLES20.glCompileShader(shader);
            checkGlError();

            int[] status = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status,
                    0);
            if (status[0] == 0) {
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                return 0;
            }

            return shader;
        }

        private void initGL() {
            final String vertexShaderSource = "attribute vec4 position;\n"
                    +
                    "void main () {\n" +
                    "   gl_Position = position;\n" +
                    "}";

            final String fragmentShaderSource = "precision mediump float;\n"
                    +
                    "void main () {\n" +
                    "   gl_FragColor = vec4(1.0, 0.0, 0.0, 0.0);\n" +
                    "}";

            mEgl = (EGL10) EGLContext.getEGL();

            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed "
                        + GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed "
                        + GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }

            int[] configsCount = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            int[] configSpec = {
                    EGL10.EGL_RENDERABLE_TYPE,
                    EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 0,
                    EGL10.EGL_STENCIL_SIZE, 0,
                    EGL10.EGL_NONE
            };

            EGLConfig eglConfig = null;
            if (!mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1,
                    configsCount)) {
                throw new IllegalArgumentException(
                        "eglChooseConfig failed "
                                + GLUtils.getEGLErrorString(mEgl
                                        .eglGetError()));
            } else if (configsCount[0] > 0) {
                eglConfig = configs[0];
            }
            if (eglConfig == null) {
                throw new RuntimeException("eglConfig not initialized");
            }

            int[] attrib_list = {
                    EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
            };
            mEglContext = mEgl.eglCreateContext(mEglDisplay,
                    eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError();
            mEglSurface = mEgl.eglCreateWindowSurface(
                    mEglDisplay, eglConfig, mSurface, null);
            checkEglError();
            if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
                int error = mEgl.eglGetError();
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e(TAG,
                            "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                    return;
                }
                throw new RuntimeException(
                        "eglCreateWindowSurface failed "
                                + GLUtils.getEGLErrorString(error));
            }

            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                    mEglSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent failed "
                        + GLUtils.getEGLErrorString(mEgl.eglGetError()));
            }
            checkEglError();

            mGl = (GL11) mEglContext.getGL();
            checkEglError();

            mProgram = buildProgram(vertexShaderSource,
                    fragmentShaderSource);
        }
    }

    private class GLSurfaceTextureListener implements
            SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                int width, int height) {
            new RenderThread(surface).start();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

    }
}
