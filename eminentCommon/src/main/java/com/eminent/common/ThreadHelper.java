package com.eminent.common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by kentlee on 2018/2/5.
 */

public class ThreadHelper {

    private String TAG;

    /**
     * ワーカースレッド上で処理するためのHandler
     */
    private Handler mWorkerHandler;
    private long mWorkerThreadID = -1;

    public ThreadHelper( String tag ) {
        TAG = tag;
    }

    public void create() {
        // ワーカースレッドを生成
        if ( mWorkerHandler == null ) {
            mWorkerHandler = HandlerThreadHandler.createHandler( TAG );
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
        }
    }

    public void release() {
        // ワーカースレッドを破棄
        if ( mWorkerHandler != null ) {
            try {
                mWorkerHandler.getLooper().quit();
            } catch ( final Exception e ) {
                Log.w( TAG, e );
            }
            mWorkerHandler = null;
        }
    }

    /**
     * ワーカースレッド上で指定したRunnableを実行する
     * 未実行の同じRunnableがあればキャンセルされる(後から指定した方のみ実行される)
     *
     * @param task
     * @param delayMillis
     */
    public final synchronized void queueEvent( final Runnable task, final long delayMillis ) {
        if ( ( task == null ) || ( mWorkerHandler == null ) ) return;
        try {
            mWorkerHandler.removeCallbacks( task );
            if ( delayMillis > 0 ) {
                mWorkerHandler.postDelayed( task, delayMillis );
            } else if ( mWorkerThreadID == Thread.currentThread().getId() ) {
                task.run();
            } else {
                mWorkerHandler.post( task );
            }
        } catch ( final Exception e ) {
            Log.w( TAG, e );
        }
    }

    /**
     * 指定したRunnableをワーカースレッド上で実行予定であればキャンセルする
     *
     * @param task
     */
    public final synchronized void removeEvent( final Runnable task ) {
        if ( task == null ) return;
        try {
            mWorkerHandler.removeCallbacks( task );
        } catch ( final Exception e ) {
            Log.w( TAG, e );
        }
    }

    // ================================================================================ //

    /**
     * UI操作のためのHandler
     */
    private static final Handler mUIHandler = new Handler( Looper.getMainLooper() );
    private static final Thread mUiThread = mUIHandler.getLooper().getThread();

    /**
     * UIスレッドでRunnableを実行するためのヘルパーメソッド
     *
     * @param task
     * @param duration
     */
    public static final void runOnUiThread( final Runnable task, final long duration ) {
        if ( task == null ) return;
        mUIHandler.removeCallbacks( task );
        if ( ( duration > 0 ) || Thread.currentThread() != mUiThread ) {
            mUIHandler.postDelayed( task, duration );
        } else {
            try {
                task.run();
            } catch ( final Exception e ) {
                Log.w( "ThreadHelper", e );
            }
        }
    }

    /**
     * UIスレッド上で指定したRunnableが実行待ちしていれば実行待ちを解除する
     *
     * @param task
     */
    public static final void removeFromUiThread( final Runnable task ) {
        if ( task == null ) return;
        mUIHandler.removeCallbacks( task );
    }

}
