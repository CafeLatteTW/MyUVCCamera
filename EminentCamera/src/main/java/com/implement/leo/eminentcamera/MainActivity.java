package com.implement.leo.eminentcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.eminent.usb.UsbCameraAdaptor;
import com.serenegiant.common.BaseActivity;

// the package of IR camera control and widget to display
import com.eminent.usb.a3d.A3DAdaptor;
import com.eminent.widget.SimpleCameraTextureView;

import java.nio.ByteBuffer;

public final class MainActivity extends BaseActivity implements UsbCameraAdaptor.FrameProcessor {
    private final Object mSync = new Object();
    private ImageButton mCameraButton;              // open camera stream
    private A3DAdaptor mA3dAdaptor;                 // for accessing USB and USB camera
    private SimpleCameraTextureView mUVCCameraView; // preview camera stream
    private ImageView mImgView;                     // display processed image (processImageFrame)
    private SeekBar mSbIntT;                        // UI to adjust camera integration time


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraButton = findViewById(R.id.cameraButton);
        mCameraButton.setOnClickListener(mOnClickListener);
        mImgView = findViewById(R.id.imgView);
        mUVCCameraView = findViewById(R.id.simpleCameraView);
        mUVCCameraView.setAlpha((float)0.0);
        mA3dAdaptor = new A3DAdaptor(this, this.getUsbManager(), this.getUVCCameraTextureView(), this);
        mSbIntT = findViewById(R.id.sbIntT);
        mSbIntT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mA3dAdaptor != null && mA3dAdaptor.isCameraOn()) {
                    int valueIntT = seekBar.getProgress();
                    mA3dAdaptor.setIntegrationTime((float) (valueIntT / 10.0));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mA3dAdaptor.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mA3dAdaptor.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mA3dAdaptor.release();
    }

    public UsbManager getUsbManager() {
        return (UsbManager) getSystemService( Context.USB_SERVICE );
    }

    public com.eminent.widget.SimpleCameraTextureView getUVCCameraTextureView() {
        return (SimpleCameraTextureView) findViewById( R.id.simpleCameraView );
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            synchronized (mSync) {
                // request permission to access camera and to start.
                if ( mA3dAdaptor.requestDevicePermission( R.xml.device_filter ) ) {
                    Toast.makeText( MainActivity.this, "USB START", Toast.LENGTH_SHORT ).show();
                }
            }
        }
    };

    private Bitmap mFrameBitmap = Bitmap.createBitmap( 144, 144, Bitmap.Config.ARGB_8888 );

    // Implement the Interface "UsbCameraAdaptor.FrameProcessor"
    @Override
    public void processImageFrame( ByteBuffer frame ) {
        // do some image processing or  implement algorithm here
        mFrameBitmap.copyPixelsFromBuffer( frame );        // don't forget to initialize m_frame_bitmap

        // use View.post to do UI operating
        mImgView.post(new Runnable() {
            @Override
            public void run() {
                mImgView.setImageBitmap(RotateBitmap(mFrameBitmap, 90, true));
            }
        });


    }

//    // if you need frame data as byte array on Java side, you can use this callback method with UVCCamera#setFrameCallback
//    // if you need to create Bitmap in IFrameCallback, please refer following snippet.
//    final Bitmap bitmap = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, Bitmap.Config.ARGB_8888);
//    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
//        @Override
//        public void onFrame(final ByteBuffer frame) {
//            frame.clear();
//            synchronized (bitmap) {
//                bitmap.copyPixelsFromBuffer(frame);
//            }
//			mImageView.post(mUpdateImageTask);
//
//        }
//    };
//
//    private final Runnable mUpdateImageTask = new Runnable() {
//        @Override
//        public void run() {
//            synchronized (bitmap) {
//				mImageView.setImageBitmap(bitmap);
//            }
//        }
//    };

    public A3DAdaptor getA3DAdaptor() {
        return mA3dAdaptor;
    }

    private static Bitmap RotateBitmap(Bitmap source, float angle, boolean flip ) {
        Matrix matrix = new Matrix();
        if ( flip ) {
            matrix.setScale( -1, 1 );
            matrix.postTranslate( source.getWidth(), 0 );
            matrix.postRotate( angle * -1 );
        } else {
            matrix.postRotate( angle );
        }

        return Bitmap.createBitmap( source, 0, 0, source.getWidth(), source.getHeight(), matrix, true );
    }
}
