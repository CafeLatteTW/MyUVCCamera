package com.eminent.usb;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.Surface;

import com.eminent.widget.SimpleCameraTextureView;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;

/**
 * Created by kentlee on 2018/2/6.
 */

public abstract class UsbCameraAdaptor extends UsbAdaptor {

    public interface FrameProcessor {
        void processImageFrame( final ByteBuffer frame );
    }

    private FrameProcessor m_frame_processor;

    private SimpleCameraTextureView mUVCCameraView;

    private UVCCamera mUVCCamera;
    private Surface mPreviewSurface;

    private int m_scale_size = 144;

    private final Object mSync = new Object();

    public UsbCameraAdaptor( Context program, UsbManager usb_manager, SimpleCameraTextureView camera_view, FrameProcessor frame_processor ) {
        super( program, usb_manager );
        m_frame_processor = frame_processor;
        mUVCCameraView = camera_view;
        mUVCCameraView.setAspectRatio( m_scale_size / (float) m_scale_size );
    }

    abstract protected void init_camera_attributes();

    @Override
    public void start() {
        super.start();
        synchronized ( mSync ) {
            if ( mUVCCamera != null ) {
                mUVCCamera.startPreview();
            }
        }
    }

    @Override
    public void stop() {
        synchronized ( mSync ) {
            if ( mUVCCamera != null ) {
                mUVCCamera.stopPreview();
            }
        }
        super.stop();
    }

    @Override
    public void release() {
        synchronized ( mSync ) {
            if ( mUVCCamera != null ) {
                try {
                    mUVCCamera.setStatusCallback( null );
                    mUVCCamera.setButtonCallback( null );
                    mUVCCamera.close();
                    mUVCCamera.destroy();
                } catch ( final Exception e ) {
                    //
                }
                mUVCCamera = null;
            }
        }
        super.release();
    }

    @Override
    protected void usb_connect( final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew ) {
        final UVCCamera camera = new UVCCamera();
        camera.open( ctrlBlock );

        if ( mPreviewSurface != null ) {
            mPreviewSurface.release();
            mPreviewSurface = null;
        }

        try {
            // added by Robert
            camera.setPreviewSize( m_scale_size, m_scale_size, 1, 60, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH );
        } catch ( final IllegalArgumentException e ) {
            // fallback to YUV mode
            try {
                //camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                // added by Robert
                camera.setPreviewSize( m_scale_size, m_scale_size, 1, 60, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH );
            } catch ( final IllegalArgumentException e1 ) {
                camera.destroy();
                return;
            }
        }

        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();

        if ( st != null ) {
            mPreviewSurface = new Surface( st );
            camera.setPreviewDisplay( mPreviewSurface );

            camera.setFrameCallback( new IFrameCallback() {
                @Override
                public void onFrame( final ByteBuffer frame ) {
                    m_frame_processor.processImageFrame( frame );
                    init_camera_attributes();
                }
            }, UVCCamera.PIXEL_FORMAT_RGBX );

            camera.startPreview();
        }

        synchronized ( mSync ) {
            mUVCCamera = camera;
            // added by Robert
            mUVCCamera.updateCameraParams();
        }

    }

}
