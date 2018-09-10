package com.implement.leo.eminentcamera;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.eminent.usb.UsbCameraAdaptor;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import com.eminent.usb.a3d.A3DAdaptor;
import com.eminent.widget.SimpleCameraTextureView;

import java.nio.ByteBuffer;

public final class MainActivity extends BaseActivity implements UsbCameraAdaptor.FrameProcessor {
    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SimpleCameraTextureView mUVCCameraView;
    // for open&start / stop&close camera preview
    private ImageButton mCameraButton;
    private Surface mPreviewSurface;
    private SeekBar mSbIntT;

    private A3DAdaptor mA3dAdaptor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraButton = findViewById(R.id.cameraButton);
        mCameraButton.setOnClickListener(mOnClickListener);
        mUVCCameraView = findViewById(R.id.simpleCameraView);
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

        mA3dAdaptor = new A3DAdaptor(this, this.getUsbManager(), this.getUVCCameraTextureView(), this);

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
                if ( mA3dAdaptor.requestDevicePermission( R.xml.device_filter ) ) {
                    Toast.makeText( MainActivity.this, "USB START", Toast.LENGTH_SHORT ).show();
                }
            }
        }
    };

    @Override
    public void processImageFrame( ByteBuffer frame ) {
        // TODO: 2018/9/7  


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
}
