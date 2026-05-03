package com.example.utube.activities;

import static com.example.utube.activities.MainActivity.PREFS_NAME;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.utube.MyApplication;
import com.example.utube.R;
import com.example.utube.models.UserDetails;
import com.example.utube.models.Users;
import com.example.utube.viewmodels.UserViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dobEditText;
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ImageView profilePicImageView;
    private Button selectProfilePicButton;
    private Button registerButton;

    private Uri selectedImageUri;
    private UserViewModel userViewModel;
    private final UserDetails userDetails = UserDetails.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("isNightMode", false);
        setTheme(isNightMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        dobEditText = findViewById(R.id.dob);
        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        profilePicImageView = findViewById(R.id.profile_pic);
        selectProfilePicButton = findViewById(R.id.select_profile_pic_button);
        registerButton = findViewById(R.id.register_button);

        selectProfilePicButton.setOnClickListener(v -> openImagePicker());

        registerButton.setOnClickListener(v -> {
            if (validateRegistration()) {
                userDetails.setDate(dobEditText.getText().toString());
                Log.e("dob", dobEditText.getText().toString());
                userDetails.setEmail(emailEditText.getText().toString());
                userDetails.setFirstName(firstNameEditText.getText().toString());
                userDetails.setLastName(lastNameEditText.getText().toString());
                userDetails.setUsername(usernameEditText.getText().toString());
                userDetails.setPassword(passwordEditText.getText().toString());
                userDetails.setPasswordConfirm(confirmPasswordEditText.getText().toString());

                UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
                userViewModel.signUp(userDetails);


                userViewModel.getRegistrationStatus().observe(this, isSuccess -> {
                    if (isSuccess) {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Invalid Registration Details", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(v -> {
            if (validateRegistration()) {
                userDetails.setDate(dobEditText.getText().toString());
                Log.e("dob", dobEditText.getText().toString());
                userDetails.setEmail(emailEditText.getText().toString());
                userDetails.setFirstName(firstNameEditText.getText().toString());
                userDetails.setLastName(lastNameEditText.getText().toString());
                userDetails.setUsername(usernameEditText.getText().toString());
                userDetails.setPassword(passwordEditText.getText().toString());
                userDetails.setPasswordConfirm(confirmPasswordEditText.getText().toString());

                UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
                userViewModel.signUp(userDetails);


                userViewModel.getRegistrationStatus().observe(this, isSuccess -> {
                    if (isSuccess) {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Invalid Registration Details", Toast.LENGTH_SHORT).show();
            }
        });


        dobEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterActivity.this,
                    R.style.CustomDatePickerDialog, // Apply custom style
                    (view, year1, monthOfYear, dayOfMonth) -> dobEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                    year, month, day);

            datePickerDialog.show();
        });
        dobEditText.setInputType(InputType.TYPE_NULL); // Prevents keyboard from popping up
        dobEditText.setFocusable(false); // Prevents focus on the EditText
    }

    private void openImagePicker() {
        Toast.makeText(MyApplication.getAppContext(), "No media? Go to camera, then back. Ensure gallery permissions.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // This limits selection to image files
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profilePicImageView.setImageURI(selectedImageUri);
            File profilePicFile = uriToFile(selectedImageUri);
            userDetails.setProfilePicFile(profilePicFile);
        }
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("profile_pic", ".jpg", getCacheDir());
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

    private boolean validateRegistration() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        //if email address is illegal mark it red and return false
        if (!email.contains("@") || !email.contains(".")) {
            emailEditText.setError("Invalid email address");
            return false;
        }

        //if the password less 8 characters, and not have both letters and numbers mark it red and return false
        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
            passwordEditText.setError("Password must be at least 8 characters long and contain both english letters and numbers");
            return false;
        }

        //if password and confirm password are not the same mark them red and return false
        if (!password.equals(confirmPassword)) {
            passwordEditText.setError("Passwords do not match");
            confirmPasswordEditText.setError("Passwords do not match");
            return false;
        }

        // Check if the username already exists
        if (Users.getInstance().getUser(username) != null) {
            usernameEditText.setError("Username already taken");
            return false;
        }




//check that all fields are filled except profile pic and what not mark in red
        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            return false;
        }
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            return false;
        }
        if (email.isEmpty()) {
            emailEditText.setError("email is required");
            return false;
        }
        if (dobEditText.getText().toString().isEmpty()) {
            dobEditText.setError("Date of birth is required");
            return false;
        }
        if (username.isEmpty()) {
            usernameEditText.setError("username is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("password is required");
            return false;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("confirm password is required");
            return false;
        }
        return true;

    }
}
