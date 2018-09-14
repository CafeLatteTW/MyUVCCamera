package com.eminent.usb.a3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.eminent.usb.UsbCameraAdaptor;
import com.eminent.widget.SimpleCameraTextureView;
import com.serenegiant.usb.USBMonitor;

/**
 * Created by kentlee on 2018/2/9.
 */

public class A3DAdaptor extends UsbCameraAdaptor {

    private A3DController m_a3d_controller;

    public A3DAdaptor( Context program, UsbManager usb_manager, SimpleCameraTextureView camera_view, FrameProcessor frame_processor ) {
        super( program, usb_manager, camera_view, frame_processor );
    }

    private boolean m_camera_on = false;

    @Override
    protected void init_camera_attributes() {
        // 因為A3D在真正啟動之後才能對 control transfer 發 request
        // 目前能確定發 request 而不會掛掉的時機是
        // 在第一次發 frame data 的 call back function 之後
        // 所以必須塞在 call back function 裡面
        // 如果能找到在 usb_connect 階段呼叫此 function 的方法是最理想
        if ( !m_camera_on ) {
            m_a3d_controller.initCamera();
            m_camera_on = true;
        }
    }

    @Override
    protected void usb_connect( final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew ) {
        super.usb_connect( device, ctrlBlock, createNew );
        UsbDeviceConnection connection = super.getConnection( device );
        m_a3d_controller = new A3DController(device, connection);
    }

    public boolean isCameraOn() {
        return m_camera_on;
    }

    public boolean setIrOn( boolean isIrOn ) { return m_a3d_controller.setPowerOn( isIrOn ); }

    public boolean setIntegrationTime(float value){
        return m_a3d_controller.setIntegrationTime(value);
    }

}
