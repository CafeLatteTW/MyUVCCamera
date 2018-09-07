package com.eminent.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.eminent.common.ThreadHelper;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;

import java.util.List;

/**
 * Created by kentlee on 2018/2/1.
 */

public abstract class UsbAdaptor {

    private static final String TAG = UsbAdaptor.class.getSimpleName();

    private final ThreadHelper m_thread_helper = new ThreadHelper( TAG );

    private final Object mSync = new Object();

    private Context m_program = null;

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UsbManager mUSBManager;

    // ------------------------------------------------------------------------------------- //

    public UsbAdaptor( Context program, UsbManager usb_manager ) {
        m_program = program;

        mUSBManager = usb_manager;

        mUSBMonitor = new USBMonitor( program, mOnDeviceConnectListener );

        m_thread_helper.create();
    }

    // ------------------------------------------------------------------------------------- //

    public Context getProgram() {
        return m_program;
    }

    public UsbDeviceConnection getConnection( UsbDevice device ) {
        return mUSBManager.openDevice( device );
    }

    private int m_deviceFilterXmlId = -1;

    public boolean requestDevicePermission( final int deviceFilterXmlId ) {
        synchronized ( mSync ) {
            if ( m_deviceFilterXmlId < 0 ) {
                mUSBMonitor.dumpDevices();
                final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters( m_program, deviceFilterXmlId );
                final List<UsbDevice> usb_list = mUSBMonitor.getDeviceList( filter.get( 0 ) );
                if ( usb_list.size() > 0 ) {
                    UsbDevice item = usb_list.get( 0 );
                    mUSBMonitor.requestPermission( item );
                    m_deviceFilterXmlId = deviceFilterXmlId;
                    return true;
                }
                // CameraDialog.showDialog( DemoActivity.this );
            }
        }
        return false;
    }

    public void start() {
        synchronized ( mSync ) {
            mUSBMonitor.register();
        }

    }

    public void stop() {
        synchronized ( mSync ) {
            if ( mUSBMonitor != null ) {
                mUSBMonitor.unregister();
            }
        }
    }

    public void release() {
        synchronized ( mSync ) {
            if ( mUSBMonitor != null ) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
            m_thread_helper.release();
        }
    }

    protected void queueEvent( Runnable task ) {
        m_thread_helper.queueEvent( task, 0 );
    }

    protected void queueEvent( Runnable task, int delayMillis ) {
        m_thread_helper.queueEvent( task, delayMillis );
    }

    protected void removeEvent( Runnable task ) {
        m_thread_helper.removeEvent( task );
    }

    // ------------------------------------------------------------------------------------- //

    private USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {

        @Override
        public void onAttach( final UsbDevice device ) {
            Toast.makeText( m_program, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT ).show();
            queueEvent( new Runnable() {
                @Override
                public void run() {
                    usb_attatch( device );
                }
            } );
        }

        @Override
        public void onConnect( final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew ) {
            Toast.makeText( m_program, "USB_CONNECTED", Toast.LENGTH_SHORT ).show();
            queueEvent( new Runnable() {
                @Override
                public void run() {
                    usb_connect( device, ctrlBlock, createNew );
                }
            } );
        }

        @Override
        public void onDisconnect( final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock ) {
            Toast.makeText( m_program, "USB_DISCONNECTED", Toast.LENGTH_SHORT ).show();
            queueEvent( new Runnable() {
                @Override
                public void run() {
                    usb_disconnect( device, ctrlBlock );
                }
            } );
        }

        @Override
        public void onDettach( final UsbDevice device ) {
            Toast.makeText( m_program, "USB_DEATTACHED", Toast.LENGTH_SHORT ).show();
            usb_dettach( device );
        }

        @Override
        public void onCancel( final UsbDevice device ) {
        }
    };

    protected abstract void usb_connect( final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew );

    protected void usb_disconnect( final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock ) {}

    protected void usb_attatch( final UsbDevice device ) {}

    protected void usb_dettach( final UsbDevice device ) {}
}
