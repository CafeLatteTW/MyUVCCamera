package com.eminent.app;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.eminent.common.ThreadHelper;

/**
 * Created by kentlee on 2018/2/3.
 */

public class BaseCompatActivity extends AppCompatActivity {

    private static boolean DEBUG = false;	// FIXME 実働時はfalseにセットすること
    private static final String TAG = BaseCompatActivity.class.getSimpleName();

    private ThreadHelper m_threadHelper = new ThreadHelper( TAG );

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_threadHelper.create();
    }

    @Override
    protected void onPause() {
        clearToast();
        super.onPause();
    }

    @Override
    protected synchronized void onDestroy() {
        m_threadHelper.release();
        super.onDestroy();
    }

    // ================================================================================ //

    public final void runOnUiThread(final Runnable task, final long duration) {
        m_threadHelper.runOnUiThread( task, duration );
    }

    public final void removeFromUiThread(final Runnable task) {
        m_threadHelper.removeFromUiThread( task );
    }

    protected void queueEvent(final Runnable task, final long delayMillis) {
        m_threadHelper.queueEvent( task, delayMillis );
    }

    protected void removeEvent(final Runnable task) {
        m_threadHelper.removeEvent( task );
    }

    // ================================================================================ //

    private Toast mToast;
    /**
     * Toastでメッセージを表示
     * @param msg
     */
    protected void showToast( @StringRes final int msg, final Object... args) {
        removeFromUiThread(mShowToastTask);
        mShowToastTask = new ShowToastTask(msg, args);
        runOnUiThread(mShowToastTask, 0);
    }

    /**
     * Toastが表示されていればキャンセルする
     */
    protected void clearToast() {
        removeFromUiThread(mShowToastTask);
        mShowToastTask = null;
        try {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    private ShowToastTask mShowToastTask;

    private final class ShowToastTask implements Runnable {
        final int msg;
        final Object args;
        private ShowToastTask(@StringRes final int msg, final Object... args) {
            this.msg = msg;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                if (args != null) {
                    final String _msg = getString(msg, args);
                    mToast = Toast.makeText(BaseCompatActivity.this, _msg, Toast.LENGTH_SHORT);
                } else {
                    mToast = Toast.makeText(BaseCompatActivity.this, msg, Toast.LENGTH_SHORT);
                }
                mToast.show();
            } catch (final Exception e) {
                // ignore
            }
        }
    }


}



