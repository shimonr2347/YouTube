package com.example.utube.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.utube.MyApplication;
import com.example.utube.R;
import com.example.utube.models.UserDetails;
import com.example.utube.viewmodels.EditVideoViewModel;

import java.io.File;

public class EditVideoDialog extends DialogFragment {
    private static final int REQUEST_VIDEO_PICK = 3;
    private static final String ARG_VIDEO_ID = "video_id";

    private EditText titleEditText;
    private Spinner categorySpinner;
    private Button changeVideoButton, saveChangesButton;
    private String videoId;
    private OnDismissListener onDismissListener;
    private EditVideoViewModel viewModel;
    private MutableLiveData<Boolean> editVideoResult;

    public static EditVideoDialog newInstance(String videoId) {
        EditVideoDialog dialog = new EditVideoDialog();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setViewModel(EditVideoViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_video, container, false);

        viewModel = new ViewModelProvider(this).get(EditVideoViewModel.class);

        titleEditText = view.findViewById(R.id.edit_title);
        categorySpinner = view.findViewById(R.id.edit_category_spinner);
        changeVideoButton = view.findViewById(R.id.change_video_button);
        saveChangesButton = view.findViewById(R.id.save_changes_button);

        videoId = getArguments().getString(ARG_VIDEO_ID);
        viewModel.loadVideo(videoId);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
        });

        viewModel.getVideo().observe(getViewLifecycleOwner(), video -> {
            if (video != null) {
                titleEditText.setText(video.getTitle());
                int categoryPosition = ((ArrayAdapter) categorySpinner.getAdapter()).getPosition(video.getCategory());
                categorySpinner.setSelection(categoryPosition);
            }
        });

        changeVideoButton.setOnClickListener(v -> openVideoPicker());
        saveChangesButton.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void openVideoPicker() {
        Toast.makeText(MyApplication.getAppContext(), "No media? Go to camera, then back. Ensure gallery permissions.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            viewModel.setNewVideoUri(data.getData());
        }
    }

    private void saveChanges() {
        String newTitle = titleEditText.getText().toString();
        String newCategory = categorySpinner.getSelectedItem().toString();
        Uri newVideoUri = viewModel.getNewVideoUri().getValue();
        Log.d("EditVideoDialog", "saveChanges video uri: " + newVideoUri);

        String userId = UserDetails.getInstance().get_id();
        String token = UserDetails.getInstance().getToken();

        viewModel.editVideo(videoId, newTitle, newCategory, newVideoUri, userId, token);
        //refresh after edit
        viewModel.getEditVideoResult().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshVideoList();
                }
            }
            dismiss();
        });
    }

    public MutableLiveData<Boolean> getEditVideoResult() {
        return editVideoResult;
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    public interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }


}

