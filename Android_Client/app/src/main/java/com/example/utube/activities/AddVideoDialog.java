package com.example.utube.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.utube.MyApplication;
import com.example.utube.R;
import com.example.utube.viewmodels.AddVideoViewModel;

public class AddVideoDialog extends DialogFragment {
    private static final int REQUEST_IMAGE_PICK = 1;

    private AddVideoListener addVideoListener;
    private EditText titleEditText;
    private Spinner categorySpinner;
    private ImageView previewImageView;
    private AddVideoViewModel viewModel;

    public interface AddVideoListener {
        void onAddVideo(String title, String category, String previewImageUrl);
    }

    public void setAddVideoListener(AddVideoListener listener) {
        this.addVideoListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_video);

        viewModel = new ViewModelProvider(this).get(AddVideoViewModel.class);

        titleEditText = dialog.findViewById(R.id.video_title_edit_text);
        categorySpinner = dialog.findViewById(R.id.category_spinner);
        previewImageView = dialog.findViewById(R.id.preview_image_view);
        Button selectPreviewImageButton = dialog.findViewById(R.id.select_preview_image_button);
        Button addVideoButton = dialog.findViewById(R.id.add_video_button);

        viewModel.getCategories().observe(this, categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
        });

        viewModel.getSelectedImageUri().observe(this, uri -> {
            if (uri != null) {
                previewImageView.setImageURI(uri);
            }
        });

        selectPreviewImageButton.setOnClickListener(v -> openImagePicker());

        addVideoButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString().trim();
            if (viewModel.isInputValid(title, category)) {
                addVideoListener.onAddVideo(title, category, viewModel.getSelectedImageUri().getValue().toString());
                dismiss();
            } else {
                Toast.makeText(getContext(), "Please fill all fields and select a preview image", Toast.LENGTH_SHORT).show();
            }
        });

        return dialog;
    }

    private void openImagePicker() {
        Toast.makeText(MyApplication.getAppContext(), "No media? Go to camera, then back. Ensure gallery permissions.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedThumbnailUri = data.getData();
            previewImageView.setImageURI(selectedThumbnailUri);
            viewModel.setSelectedImageUri(selectedThumbnailUri);
        }
    }
}

