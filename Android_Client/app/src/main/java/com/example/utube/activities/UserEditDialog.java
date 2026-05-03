package com.example.utube.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.utube.MyApplication;
import com.example.utube.R;
import com.example.utube.models.UserDetails;
import com.example.utube.viewmodels.ChannelViewModel;
import com.example.utube.viewmodels.UserViewModel;
import com.google.android.exoplayer2.util.Log;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class UserEditDialog extends DialogFragment {
    private Activity activity;
    private UserViewModel userViewModel;
    private ImageView profileImageView;
    private Uri selectedImageUri;
    UserDetails userDetails = UserDetails.getInstance();
    private Runnable onDismissListener;


    public UserEditDialog(Activity activity, ChannelViewModel channelViewModel) {
        this.activity = activity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);


        EditText firstNameEditText = view.findViewById(R.id.edit_first_name);
        EditText lastNameEditText = view.findViewById(R.id.edit_last_name);
        EditText emailEditText = view.findViewById(R.id.edit_email);
        EditText dateEditText = view.findViewById(R.id.edit_date);
        profileImageView = view.findViewById(R.id.profile_image);
        Button selectProfilePicButton = view.findViewById(R.id.btn_select_profile_pic);
        Button saveButton = view.findViewById(R.id.btn_save);

        // Pre-fill fields with existing data if available
        firstNameEditText.setText(userDetails.getFirstName());
        lastNameEditText.setText(userDetails.getLastName());
        emailEditText.setText(userDetails.getEmail());
        dateEditText.setText(userDetails.getDate());
        // Load existing profile picture if available


        // Load existing profile picture if available
        if (userDetails.getProfilePic() != null && !userDetails.getProfilePic().isEmpty()) {
            // Log user profile pic
            Log.d("UserEditDialog", "User profile pic: " + userDetails.getProfilePic());
            String fullUrl = "http://10.0.2.2:12345" + userDetails.getProfilePic();
            //set the string into the image view
            profileImageView.setImageURI(Uri.parse(fullUrl));


            // Load the profile picture using Picasso
            Picasso.get()
                    .load(fullUrl)
                    .error(R.drawable.ic_profile_placeholder) // Placeholder or error drawable
                    .into(profileImageView);
        }

        selectProfilePicButton.setOnClickListener(v -> openImagePicker());

        saveButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String date = dateEditText.getText().toString();
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || date.isEmpty()) {
                Toast.makeText(MyApplication.getAppContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();

                return;
            }


            userDetails.setFirstName(firstName);
            userDetails.setLastName(lastName);
            userDetails.setEmail(email);
            userDetails.setDate(date);
            if (selectedImageUri != null) {
                try {
                    userDetails.setProfilePicFile(uriToFile(selectedImageUri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            userViewModel.updateUserDetails(userDetails);
            dismiss();
        });

        dateEditText.setOnClickListener(v -> showDatePicker(dateEditText));

        dateEditText.setInputType(InputType.TYPE_NULL); // Prevents keyboard from popping up
        dateEditText.setFocusable(false); // Prevents focus on the EditText
    }

    private void showDatePicker(EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                activity,
                R.style.CustomDatePickerDialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    dateEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                },
                year, month, day);

        datePickerDialog.show();
    }


    private void openImagePicker() {
        Toast.makeText(MyApplication.getAppContext(), "No media? Go to camera, then back. Ensure gallery permissions.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // This limits selection to image files
        startActivityForResult(intent, 1);
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("profile_pic", ".jpg", activity.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
            File profilePicFile = uriToFile(selectedImageUri);
            userDetails.setProfilePicFile(profilePicFile);
            Picasso.get()
                    .load(selectedImageUri)
                    .into(profileImageView);
        }
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.run();
        }
    }
}