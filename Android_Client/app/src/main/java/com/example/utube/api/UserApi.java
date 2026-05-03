package com.example.utube.api;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.example.utube.MyApplication;
import com.example.utube.activities.RegisterActivity;
import com.example.utube.models.UserDetails;
import com.example.utube.utils.LoginRequest;
import com.example.utube.utils.LoginResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserApi {
    private MutableLiveData<Boolean> authenticateResult;
    private MutableLiveData<Boolean> registrationResult; // New field
    private MutableLiveData<UserDetails> userData;
    private MutableLiveData<Boolean> deleteUserResult;

    public Retrofit retrofit;
    public WebServiceApi webApi;

    public UserApi(MutableLiveData<Boolean> authenticateResult, MutableLiveData<UserDetails> userData, MutableLiveData<Boolean> registrationResult,
                   MutableLiveData<Boolean> deleteUserResult) {
        this.authenticateResult = authenticateResult;
        this.userData = userData;
        this.registrationResult = registrationResult;
        this.deleteUserResult = deleteUserResult;

        this.retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:12345/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.webApi = retrofit.create(WebServiceApi.class);
    }

    public void authenticate(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = webApi.checkUserNameAndPassword(loginRequest);
        Log.d("UserApi", "Authenticating user: " + username);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!response.isSuccessful()) {
                    Log.d("UserApi", "Authentication failed: " + response.code());
                    Toast.makeText(MyApplication.context, "User Not Found!", Toast.LENGTH_SHORT).show();
                    authenticateResult.setValue(false);
                } else {
                    Log.d("UserApi", "Authentication successful" + response.body());
                    LoginResponse loginResponse = response.body();
                    UserDetails userDetails = UserDetails.getInstance();
                    userDetails.setUsername(username);
                    userDetails.setToken(loginResponse.getToken());
                    userDetails.set_id(loginResponse.getUserId());
                    authenticateResult.setValue(true);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("UserApi", "Authentication request failed", t);
                Toast.makeText(MyApplication.context, "Unable to connect to the server.", Toast.LENGTH_SHORT).show();
                authenticateResult.setValue(false);
            }
        });
    }

    public void fetchUserDetails(UserDetails user) {
        Log.e("UserApi", "Fetching user details for: " + user.getToken());
        //Log.e("UserApi","Fetching user details for: " + user.get_id());

        String bearerToken = "Bearer " + user.getToken();
        Call<UserDetails> call = webApi.getUser(user.get_id(), bearerToken);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful()) {
                    UserDetails userDetails = UserDetails.getInstance();
                    UserDetails fetchedUserDetails = response.body();
                    if (fetchedUserDetails != null) {
                        userDetails.setFirstName(fetchedUserDetails.getFirstName());
                        userDetails.setLastName(fetchedUserDetails.getLastName());
                        userDetails.setProfilePic(fetchedUserDetails.getProfilePic());
                        userDetails.setUsername(fetchedUserDetails.getUsername());
                        userDetails.setEmail(fetchedUserDetails.getEmail());
                        userDetails.setDate(fetchedUserDetails.getDate());
                        userDetails.set_id(fetchedUserDetails.get_id());
                    }

                    userData.setValue(fetchedUserDetails);
                } else {
                    Log.e("UserApi", "Failed to fetch user details: " + response.code());
                    userData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e("UserApi", "User details request failed", t);
                userData.setValue(null);
            }
        });
    }

    public void signUp(UserDetails user) {
        RequestBody firstName = RequestBody.create(MediaType.parse("text/plain"), user.getFirstName());
        RequestBody lastName = RequestBody.create(MediaType.parse("text/plain"), user.getLastName());
        RequestBody date = RequestBody.create(MediaType.parse("text/plain"), user.getDate());
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), user.getEmail());
        RequestBody username = RequestBody.create(MediaType.parse("text/plain"), user.getUsername());
        RequestBody password = RequestBody.create(MediaType.parse("text/plain"), user.getPassword());
        MultipartBody.Part profilePicPart = null;
        if (user.getProfilePicFile() != null) {
            RequestBody profilePicBody = RequestBody.create(MediaType.parse("image/*"), user.getProfilePicFile());
            profilePicPart = MultipartBody.Part.createFormData("profilePic", user.getProfilePicFile().getName(), profilePicBody);
        }

        Call<UserDetails> call = webApi.signUp(firstName, lastName, date, email, username, password, profilePicPart);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful()) {
                    Log.d("UserApi", "Sign up successful: " + response.body());
                    Toast.makeText(MyApplication.context, "Registration Successful", Toast.LENGTH_SHORT).show();
                    registrationResult.setValue(true); // Update the registration result
                } else {
                    Toast.makeText(MyApplication.context, "SignUp failed, try replace user name", Toast.LENGTH_LONG).show();
                    registrationResult.setValue(false); // Update the registration result
                    Log.d("UserApi", "Sign up failed: " + response.code());
                    userData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                //TOAST WITH THE REASON
                Toast.makeText(MyApplication.context, "can't connect to the server", Toast.LENGTH_LONG).show();
                Log.e("UserApi", "Sign up request failed", t);
                registrationResult.setValue(false); // Update the registration result
                userData.setValue(null);
            }
        });
    }

    public void fetchUserPage(UserDetails user) {
        Log.e("UserApi", "Fetching user page for: " + user.getToken());
        Call<UserDetails> call = webApi.getUserPage(user.get_id());
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful()) {
                    UserDetails userDetails = UserDetails.getInstance();
                    UserDetails fetchedUserDetails = response.body();
                    if (fetchedUserDetails != null) {
                        userDetails.setFirstName(fetchedUserDetails.getFirstName());
                        userDetails.setLastName(fetchedUserDetails.getLastName());
                        userDetails.setProfilePic(fetchedUserDetails.getProfilePic());
                        userDetails.setUsername(fetchedUserDetails.getUsername());
                        userDetails.setEmail(fetchedUserDetails.getEmail());
                        userDetails.setDate(fetchedUserDetails.getDate());
                        userDetails.set_id(fetchedUserDetails.get_id());
                    }

                    userData.setValue(fetchedUserDetails);
                } else {
                    Log.e("UserApi", "Failed to fetch user details: " + response.code());
                    userData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e("UserApi", "User details request failed", t);
                userData.setValue(null);
            }
        });
    }

    public void deleteUser(String userId, String token) {
        String bearerToken = "Bearer " + token;
        Call<Void> call = webApi.deleteUser(userId, bearerToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("UserApi", "User deleted successfully");
                    deleteUserResult.setValue(true);
                } else {
                    Log.d("UserApi", "Failed to delete user: " + response.code());
                    deleteUserResult.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("UserApi", "Delete user request failed", t);
                deleteUserResult.setValue(false);
            }
        });
    }

    public void updateUserDetails(UserDetails user) {
        RequestBody firstName = RequestBody.create(MediaType.parse("text/plain"), user.getFirstName());
        RequestBody lastName = RequestBody.create(MediaType.parse("text/plain"), user.getLastName());
        RequestBody date = RequestBody.create(MediaType.parse("text/plain"), user.getDate());
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), user.getEmail());

        MultipartBody.Part profilePicPart = null;
        if (user.getProfilePicFile() != null) {
            RequestBody profilePicBody = RequestBody.create(MediaType.parse("image/*"), user.getProfilePicFile());
            profilePicPart = MultipartBody.Part.createFormData("profilePic", user.getProfilePicFile().getName(), profilePicBody);
        }
        String bearerToken = "Bearer " + user.getToken(); // Assuming token is stored in UserDetails

        Call<UserDetails> call = webApi.updateUserDetails(user.get_id(), firstName, lastName, date, email, profilePicPart, bearerToken);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful()) {
                    Log.d("UserApi", "Update successful: " + response.body());
                    Toast.makeText(MyApplication.context, "Update Successful", Toast.LENGTH_SHORT).show();
                    // Handle the response here if needed
                } else {
                    Toast.makeText(MyApplication.context, "Update failed", Toast.LENGTH_LONG).show();
                    Log.d("UserApi", "Update failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Toast.makeText(MyApplication.context, "Cannot connect to the server", Toast.LENGTH_LONG).show();
                Log.e("UserApi", "Update request failed", t);
            }
        });
    }

    public void createUserThread(String token, MutableLiveData<Boolean> threadCreationStatus) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        Call<Void> call = api.createUserThread("Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                threadCreationStatus.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                threadCreationStatus.postValue(false);
            }
        });
    }

    public void closeUserThread(String token, MutableLiveData<Boolean> threadClosureStatus) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        Call<Void> call = api.closeUserThread("Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                threadClosureStatus.postValue(response.isSuccessful());
                if (!response.isSuccessful()) {
                    Log.e("UserApi", "Failed to close user thread. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                threadClosureStatus.postValue(false);
                Log.e("UserApi", "Error closing user thread", t);
            }
        });
    }

    public void notifyVideoWatch(String videoId, String token) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
        } catch (JSONException e) {
            Log.e("UserApi", "Error creating JSON object", e);
            return;
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Call<Void> call = api.notifyVideoWatch(body, "Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("UserApi", "Successfully notified video watch");
                } else {
                    Log.e("UserApi", "Failed to notify video watch. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("UserApi", "Error notifying video watch", t);
            }
        });
    }

}
