package com.github.jksiezni.xpra.ssh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;

import com.github.jksiezni.xpra.UiTask;

class YesNoAskTask extends UiTask<String, Boolean> {

    private final Context context;

    public YesNoAskTask(Context context) {
        this.context = context;
    }

    @Override
    protected void doOnUIThread(String... params) {
        new YesNoDialog(context, params[0]).show();
    }

    public class YesNoDialog extends AlertDialog.Builder {

        public YesNoDialog(Context ctx, String message) {
            super(ctx);
            setMessage(message);
            setPositiveButton("YES", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(true);
                }
            });
            setNegativeButton("NO", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(false);
                }
            });
            setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    postResult(false);
                }
            });
        }

    }
}

