package com.eminent.usb.a3d;

import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.eminent.common.ImageProcess;

/**
 * Created by kentlee on 2017/12/6.
 */

public class A3DControllerAE extends A3DController {

    private int currentIntegration = 260;

    private float mtcnn_brightness = 0;
    private float frame_brightness = 0;

    private int mtcnn_size = 0;
    private int frame_size = 0;
    private int proportion = 0;

    private float brightness_low_thre = 50;
    private float brightness_high_thre = 80;

    private static int brightness_step[] = { 25, 25, 20, 20, 15, 10, 5 };

//    private static int fps = 0;
//    private static int integ = 0;

    private static float ratio = 0;

    private static int _step = 0;

//    private static Bitmap m_frame_bitmap;

    private boolean is_ae_mode = false;

    public void setThresholdLevel( double level ) {

    }

    public void setThresholds( float high, float low ) {
        brightness_high_thre = high;
        brightness_low_thre = low;
    }

    public A3DControllerAE( UsbDevice device, UsbDeviceConnection connection ) {
        super( device, connection );
    }

    public A3DControllerAE( UsbDevice device, UsbDeviceConnection connection, boolean ae_mode ) {
        super( device, connection );
        setAutoExposureMode( ae_mode );
    }

    public void setAutoExposureMode( boolean value ) {
        is_ae_mode = value;
        if ( is_ae_mode ) {
            resetSettings();
        }
    }

    @Override
    public boolean initCamera() {
        if ( !is_ae_mode )
            return super.initCamera();
        super.initCamera();
        return resetSettings();
    }

    @Override
    public boolean setIntegrationTime( float value ) {
        return !is_ae_mode && super.setIntegrationTime( value );
    }

    @Override
    public boolean setFrameRate( int fps ) {
        return !is_ae_mode && super.setFrameRate( fps );
    }

    @Override
    public boolean setCurrentPower( int miniAmp ) {
        return !is_ae_mode && super.setCurrentPower( miniAmp );
    }

    public boolean resetSettings() {
        currentIntegration = 260;
        return auto_set_fps_and_integration();
    }


    // full screen bitmap // call every frame at beginning
    public float calculate_frame_brightness( Bitmap bitmap ) {

        if ( !is_ae_mode || bitmap.getWidth()!= 144) {
            return 0.f;
        }

        frame_brightness = ImageProcess.averageBrightnessRGB( bitmap );
        frame_size = bitmap.getWidth() * bitmap.getHeight();
        return frame_brightness;
    }

    // raw face image bitmap // call while face > 0
    public boolean adjust_integration_time_with_face( Bitmap bitmap ) {

        if ( !is_ae_mode )
            return false;

        boolean status = false;

        mtcnn_brightness = ImageProcess.averageBrightnessRGB( bitmap );
        mtcnn_size = bitmap.getWidth() * bitmap.getHeight();

        ratio = ( (float) mtcnn_size / (float) frame_size );
        proportion = (int) ( ratio * 10 );

        if ( proportion <= 6 ) {
            _step = brightness_step[proportion];
        } else {
            _step = brightness_step[6];
        }

        if ( mtcnn_brightness <= brightness_low_thre ) {
            currentIntegration += _step;
            if ( currentIntegration > 402 )
                currentIntegration = 402;
            auto_set_fps_and_integration();
            status = true;
        }
        if ( mtcnn_brightness >= brightness_high_thre ) {
            currentIntegration -= _step;
            if ( currentIntegration < 1 )
                currentIntegration = 1;
            auto_set_fps_and_integration();
            status = true;
        }

        return status;
    }

    // call while no face
    public boolean adjust_integration_time() {

        if ( !is_ae_mode )
            return false;

        boolean status = false;

        if ( frame_brightness > 60 ) {
            currentIntegration -= 50;
            if ( currentIntegration < 1 )
                currentIntegration = 1;
            this.auto_set_fps_and_integration();
            status = true;
        }

        if ( 10 > frame_brightness ) {
            currentIntegration += 50;
            if ( currentIntegration > 402 )
                currentIntegration = 402;
            this.auto_set_fps_and_integration();
            status = true;
        }

        return status;
     }

    // ----------------------------------------------------------------------------------------- //

    private boolean auto_set_fps_and_integration() {

        boolean result = true;

        if ( ( 1 <= currentIntegration ) && ( currentIntegration < 60 ) ) {
            result &= super.setIntegrationTimeWithRegisterValue( currentIntegration );
            result &= super.setFrameRate( 60 );
        } else if ( ( 60 <= currentIntegration ) && ( currentIntegration < 310 ) ) {
            result &= super.setIntegrationTimeWithRegisterValue( currentIntegration / 2 );
            result &= super.setFrameRate( 30 );
        } else if ( ( 310 <= currentIntegration ) && ( currentIntegration < 402 ) ) {
            result &= super.setIntegrationTimeWithRegisterValue( (int) ( currentIntegration / 2.125 ) );
            result &= super.setFrameRate( 25 );
        } else {
            result &= super.setIntegrationTimeWithRegisterValue( 161 );
            result &= super.setFrameRate( 25 );
        }

        return result;
    }

}
