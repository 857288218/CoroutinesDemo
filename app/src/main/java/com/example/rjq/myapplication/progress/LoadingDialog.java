package com.example.rjq.myapplication.progress;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.example.rjq.myapplication.R;

public class LoadingDialog extends AlertDialog {
    private Context context;

    public LoadingDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog_view, null);
        this.setView(view);
    }
}
