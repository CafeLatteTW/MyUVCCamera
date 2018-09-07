package com.eminent.common;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.nio.ByteBuffer;

/**
 * Created by kentlee on 2017/11/16.
 */

public class ImageProcess {

    public static byte[] bitmapARGBToByteArrayRGB( Bitmap img ) {
        return bitmapARGBToByteArray( img, false );
    }

    public static byte[] bitmapARGBToByteArrayGRAY( Bitmap img ) {
        return bitmapARGBToByteArray( img, true );
    }

    public static int[] bitmapARGBToIntArrayRGB( Bitmap img ) {
        return byteArrayToIntArray( bitmapARGBToByteArrayRGB( img ) );
    }

    public static int[] bitmapARGBToIntArrayGRAY( Bitmap img ) {
        return byteArrayToIntArray( bitmapARGBToByteArrayGRAY( img ) );
    }

    public static float[] bitmapARGBToNormalizedFloatArrayRGB( Bitmap img ) {
        return whitenNormalize( bitmapARGBToByteArrayRGB( img ) );
    }

    public static float[] bitmapARGBToNormalizedFloatArrayGRAY( Bitmap img ) {
        return whitenNormalize( bitmapARGBToByteArrayGRAY( img ) );
    }

    public static float averageBrightnessGRAY( Bitmap image_bitmap ) {
        int[] frame_array = bitmapARGBToIntArrayGRAY( image_bitmap );
        return get_array_avg( frame_array );
    }

    public static float averageBrightnessRGB( Bitmap image_bitmap ) {
        int[] frame_array = bitmapARGBToIntArrayRGB( image_bitmap );
        return get_array_avg( frame_array );
    }

    // ============================================================================= //

    private static byte[] bitmapARGBToByteArray( Bitmap img, boolean isGray ) {

        int w = img.getWidth();
        int h = img.getHeight();

        ByteBuffer byteBuffer = ByteBuffer.allocate( img.getAllocationByteCount() );

        img.copyPixelsToBuffer( byteBuffer );

        byte[] argbArray = byteBuffer.array();
        byte[] byteArray;

        if ( isGray ) {

            byteArray = new byte[w * h];

            for ( int i = 0 ; i < w * h ; i++ ) {
                byteArray[i] = argbArray[i * 4];
            }

        } else {

            byteArray = new byte[w * h * 3];

            for ( int i = 0 ; i < w * h ; i++ ) {
                byteArray[i * 3] = argbArray[i * 4];
                byteArray[i * 3 + 1] = argbArray[i * 4 + 1];
                byteArray[i * 3 + 2] = argbArray[i * 4 + 2];
            }
        }

        return byteArray;
    }

    private static int[] byteArrayToIntArray( byte[] byteArray ) {

        int[] intArray = new int[byteArray.length];

        for ( int i = 0 ; i < byteArray.length ; i++ ) {
            intArray[i] = ( byteArray[i] & 0xFF );
        }

        return intArray;
    }

    private static float[] whitenNormalize( byte[] byteArray ) {

        long sum = 0;
        long sqr_sum = 0;

        int[] intArray = new int[byteArray.length];

        for ( int i = 0 ; i < intArray.length ; i++ ) {
            int p = byteArray[i] & 0xFF;
            intArray[i] = p;
            sum += p;
            sqr_sum += p * p;
        }

        double mean = (double) sum / (double) byteArray.length;
        double std = Math.sqrt( (double) sqr_sum / (double) byteArray.length - ( mean * mean ) );
        double std_adj = Math.max( std, 1.0 / Math.sqrt( (double) byteArray.length ) );

        float[] floatArray = new float[byteArray.length];

        for ( int i = 0 ; i < byteArray.length ; i++ ) {
            floatArray[i] = (float) ( ( (double) intArray[i] - mean ) * 1.0 / std_adj );
        }

        return floatArray;
    }

    private static Bitmap FlipBitmap( Bitmap source ) {
        Matrix matrix = new Matrix();
        matrix.setScale( -1, 1 );
        matrix.postTranslate( source.getWidth(), 0 );
        return Bitmap.createBitmap( source, 0, 0, source.getWidth(), source.getHeight(), matrix, true );
    }

    private static float get_array_avg( int[] value ) {
        long sum = 0;
        for ( int b : value ) {
            long p = b & 0xFF;
            sum += p;
        }
        try {
            return (float) sum / (float) value.length;
        } catch ( Exception e ) {
        }
        return 0.f;
    }

}
