package com.example.utube.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.utube.R;
import com.example.utube.activities.LoginActivity;

public class LoginPromptDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login_prompt, null);

        builder.setView(view);

        Button loginButton = view.findViewById(R.id.login_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return builder.create();
    }
}


