package org.hisp.dhis.android.sdk.ui.nhancv;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nhancao on 4/23/17.
 */

public class NTextChange implements TextWatcher {

    private Timer timer = new Timer();
    private TextListener doing;
    private int timeout;

    public NTextChange(TextListener doing) {
        this(doing, 200);
    }

    public NTextChange(TextListener doing, int timeout) {
        this.doing = doing;
        this.timeout = timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void afterTextChanged(final Editable editable) {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (doing != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doing.after(editable);
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }
            }
        }, timeout);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // nothing to do here
        doing.before();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Run on ui thread
     */

    private Handler runOnUiThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
        return handler;
    }

    public interface TextListener {
        void after(Editable editable);

        void before();
    }

    public abstract static class AbsTextListener implements TextListener {
        @Override
        public void before() {

        }
    }
}
