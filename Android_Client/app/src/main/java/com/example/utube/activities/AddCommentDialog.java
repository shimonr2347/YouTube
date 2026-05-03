package com.example.utube.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.utube.R;
import com.example.utube.viewmodels.AddCommentViewModel;

public class AddCommentDialog extends DialogFragment {
    private static final String ARG_INITIAL_TEXT = "initial_text";
    private AddCommentListener addCommentListener;
    private AddCommentViewModel viewModel;

    public static AddCommentDialog newInstance(String initialText) {
        AddCommentDialog fragment = new AddCommentDialog();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_TEXT, initialText);
        fragment.setArguments(args);
        return fragment;
    }

    public interface AddCommentListener {
        void onAddComment(String text);
    }

    public void setAddCommentListener(AddCommentListener listener) {
        this.addCommentListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_comment);

        viewModel = new ViewModelProvider(this).get(AddCommentViewModel.class);

        EditText commentText = dialog.findViewById(R.id.comment_edit_text);
        Button submitCommentButton = dialog.findViewById(R.id.submit_comment_button);

        // Set initial text if provided
        String initialText = getArguments() != null ? getArguments().getString(ARG_INITIAL_TEXT) : "";
        commentText.setText(initialText);
        commentText.setSelection(commentText.getText().length()); // Move cursor to end

        submitCommentButton.setOnClickListener(v -> {
            String text = commentText.getText().toString().trim();
            if (viewModel.isCommentValid(text)) {
                addCommentListener.onAddComment(text);
                dismiss();
            } else {
                commentText.setError("Comment cannot be empty");
            }
        });

        return dialog;
    }
}