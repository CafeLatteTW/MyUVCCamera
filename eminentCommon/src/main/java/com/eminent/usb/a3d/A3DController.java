package com.eminent.usb.a3d;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;


/**
 * Created by kentlee on 2017/10/11.
 */

public class A3DController {

    private static int s_interval = 135;
    private static int s_integration_max_reg_value = 161;

    private UsbDevice m_usb_device;
    private UsbDeviceConnection m_usb_connection;

    public A3DController( UsbDevice device, UsbDeviceConnection connection ) {
        m_usb_device = device;
        m_usb_connection = connection;
    }

    public void initDevice() {
        UsbInterface intf = m_usb_device.getInterface( 0 );
        m_usb_connection.claimInterface( intf, true );
    }

    public boolean resetSettings() {
        boolean result = true;
        result &= cameraWrite( (byte) 0x95, (byte) 0x20 ); // 0x40 = 60/4, 0x20 = 60/2, 0x00 = 60 FPS
        result &= cameraWrite( (byte) 0x10, (byte) 0x0f ); // 0x0- = 400mA, 0x1- = 200mA, 0x2- = 100mA, 0x3- = 50mA
        result &= cameraWrite( (byte) 0x14, (byte) 0x02 );
        result &= cameraWrite( (byte) 0x08, (byte) 0x04 );
        result &= cameraWrite( (byte) 0x00, (byte) 0xa1 ); // integration time = 0x1a ~ 0xa1
        return result;
    }


    public boolean initCamera() {
        initDevice();
        return resetSettings();
    }

    public boolean setIntegrationTime( float value ) {

        value = Math.min( 1.f, Math.max( 0.f, value ) );

        int set_value = (int) ( value * (float) s_interval ) + ( s_integration_max_reg_value - s_interval );

        set_value = Math.min( s_integration_max_reg_value, Math.max( 0, set_value ) );

        return set_integration_time( set_value );
    }

    public boolean setIntegrationTimeWithRegisterValue( int value ) {

        value = Math.min( s_integration_max_reg_value, Math.max( 0, value ) );

        return set_integration_time( value );
    }

    public boolean setFrameRate( int fps ) {
        // 0x30 = 15, 0x20 = 30, 0x10 = 60 FPS

        if ( fps == 25 ) {
            set_n_table( (byte) 183 );
            set_m_table( (byte) 8 );
            return set_frame_rate( 0x20 );
        }

        set_n_table( (byte) 117 );
        set_m_table( (byte) 10 );

        byte b = 0x20;

        if ( fps == 60 ) {
            b = 0x10;
        } else if ( fps == 30 ) {
            b = 0x20;
        } else if ( fps == 15 ) {
            b = 0x30;
        }

        return set_frame_rate( b );
    }

    public boolean setCurrentPower( int miniAmp ) {
        // 0x0- = 400mA, 0x1- = 200mA, 0x2- = 100mA, 0x3- = 50mA
        byte b = 0x00;

        if ( miniAmp == 200 )
            b = 0x10;
        else if ( miniAmp == 100 )
            b = 0x20;
        else if ( miniAmp == 50 )
            b = 0x30;

        return set_current_power( b );
    }

    public boolean setPowerOn( boolean on ) {
        return on ? set_power_on() : set_power_off();
    }

    public boolean turnOn() {
        return setPowerOn( true );
    }

    public boolean turnOff() {
        return setPowerOn( false );
    }

    public boolean cameraRead( byte addr, byte[] read_buffer ) {

        if ( read_buffer.length < 2 )
            return false;

        final int[] result = new int[2];

        {
            byte[] buffer = new byte[] { addr, 0x00 };
            result[0] = control_transfer( 0x21, 0x01, 0x0400, 0x0400, buffer );
        }

        {
            byte[] buffer = read_buffer;
            result[1] = control_transfer( 0xa1, 0x81, 0x0500, 0x0400, buffer );
        }

//        Log.i( "USBCameraController", "cameraRead result = " + result[0] + ", " + result[1] );

        return ( result[0] == 2 && result[1] == 2 );
    }

    public boolean cameraWrite( byte addr, byte data ) {

        final int[] result = new int[2];

        {
            byte[] buffer = new byte[] { addr, 0x00 };
            result[0] = control_transfer( 0x21, 0x01, 0x0400, 0x0400, buffer );
        }

        {
            byte[] buffer = new byte[] { data, 0x00 };
            result[1] = control_transfer( 0x21, 0x01, 0x0500, 0x0400, buffer );
        }

//        Log.i( "USBCameraController", "cameraWrite result = " + result[0] + ", " + result[1] );

        return ( result[0] == 2 && result[1] == 2 );
    }

    // =============================================================================== //

    protected int control_transfer( int requestType, int request, int value, int index, byte[] buffer ) {
        return m_usb_connection.controlTransfer( requestType, request, value, index, buffer, buffer.length, 2000 );
    }

    protected boolean set_integration_time( int reg_value ) {
        byte b = (byte) ( reg_value & 0xFF );
        return cameraWrite( (byte) 0x00, b );
    }

    protected boolean read_integration_time( byte[] read_buffer ) {
        return cameraRead( (byte) 0x00, read_buffer );
    }

    protected boolean set_m_table( int reg_value ) {
        return cameraWrite( (byte) 0x07, (byte) ( reg_value & 0xFF ) );
    }

    protected boolean set_n_table( int reg_value ) {
        return cameraWrite( (byte) 0x06, (byte) ( reg_value & 0xFF ) );
    }

    protected boolean set_frame_rate( int reg_value ) {
        return cameraWrite( (byte) 0x95, (byte) ( reg_value & 0xFF ) );
    }

    protected boolean read_frame_rate( byte[] read_buffer ) {
        return cameraRead( (byte) 0x95, read_buffer );
    }

    protected boolean set_current_power( int reg_value ) {
        byte b = ( (byte) ( reg_value & 0xF0 ) );
        return cameraWrite( (byte) 0x10, (byte) ( b | 0x0f ) );
    }

    protected boolean read_current_power( byte[] read_buffer ) {
        return cameraRead( (byte) 0x10, read_buffer );
    }

    protected boolean set_power_off() {
        boolean result = cameraWrite( (byte) 0x10, (byte) 0x07 );
        return result & cameraWrite( (byte) 0x8d, (byte) 0xa1 );
    }

    protected boolean set_power_on() {
        boolean result = cameraWrite( (byte) 0x10, (byte) 0x0f );
        return result & cameraWrite( (byte) 0x8d, (byte) 0x00 );
    }

}
