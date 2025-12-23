package com.megaproject.connecto;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class IncorrectPasswordDialog extends DialogFragment {

    public interface ActionListener {
        void onTryAgain();
        void onForgotPassword();
    }

    private ActionListener listener;

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_incorrect_password, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        // Close Button (X)
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        // Try Again Button
        view.findViewById(R.id.btnTryAgain).setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onTryAgain();
        });

        // Forgot Password Button
        view.findViewById(R.id.btnForgotPassword).setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onForgotPassword();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Optional: Dim amount is usually automatic for DialogFragment
        }
    }
}
