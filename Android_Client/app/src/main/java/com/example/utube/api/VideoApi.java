package com.example.utube.api;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.utube.MyApplication;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class VideoApi {
    private MutableLiveData<Boolean> editResult;
    private MutableLiveData<Boolean> uploadResult;
    private MutableLiveData<Boolean> deleteResult;
    private Retrofit retrofit;
    private WebServiceApi webApi;

    public VideoApi(MutableLiveData<Boolean> uploadResult, MutableLiveData<Boolean> editResult, MutableLiveData<Boolean> deleteResult) {
        this.uploadResult = uploadResult;
        this.editResult = editResult;
        this.deleteResult = deleteResult;
        this.retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:12345/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.webApi = retrofit.create(WebServiceApi.class);
    }

    public void deleteVideo(String videoId, String userId, String token) {
        String bearerToken = "Bearer " + token;
        Call<Void> call = webApi.deleteVideo(bearerToken, userId, videoId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                deleteResult.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                deleteResult.postValue(false);
            }
        });
    }

    public void uploadVideo(String title, String category, File videoFile, File thumbnailFile, String userId, String authorName, String token) {
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody categoryBody = RequestBody.create(MediaType.parse("text/plain"), category);
        RequestBody authorIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody authorNameBody = RequestBody.create(MediaType.parse("text/plain"), authorName);
        RequestBody viewsBody = RequestBody.create(MediaType.parse("text/plain"), "0");
        RequestBody likesBody = RequestBody.create(MediaType.parse("text/plain"), "0");

        RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
        MultipartBody.Part videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestBody);

        RequestBody thumbnailRequestBody = RequestBody.create(MediaType.parse("image/*"), thumbnailFile);
        MultipartBody.Part thumbnailPart = MultipartBody.Part.createFormData("thumbnail", thumbnailFile.getName(), thumbnailRequestBody);

        String bearerToken = "Bearer " + token;

        Call<Void> call = webApi.uploadVideo(bearerToken, userId, titleBody, categoryBody, authorIdBody, authorNameBody, viewsBody, likesBody, videoPart, thumbnailPart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                uploadResult.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                uploadResult.postValue(false);
            }
        });
    }

    public void editVideo(String videoId, String title, String category, Uri videoUri, String userId, String token) {
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody categoryBody = RequestBody.create(MediaType.parse("text/plain"), category);

        MultipartBody.Part videoPart = null;
        if (videoUri != null) {
            try {
                ContentResolver contentResolver = MyApplication.context.getContentResolver();
                String mimeType = contentResolver.getType(videoUri);
                String fileName = getFileName(contentResolver, videoUri);

                InputStream inputStream = contentResolver.openInputStream(videoUri);
                if (inputStream != null) {
                    File videoFile = createTempFileFromInputStream(inputStream, fileName);
                    RequestBody videoRequestBody = RequestBody.create(MediaType.parse(mimeType), videoFile);
                    videoPart = MultipartBody.Part.createFormData("video", fileName, videoRequestBody);
                }
            } catch (IOException e) {
                Log.e("VideoApi", "Error reading video file: ", e);
                editResult.postValue(false);
                return;
            }
        }

        String bearerToken = "Bearer " + token;

        Call<Void> call = webApi.editVideo(bearerToken, userId, videoId, titleBody, categoryBody, videoPart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                editResult.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("VideoApi", "Edit video request failed", t);
                editResult.postValue(false);
            }
        });
    }

    private File createTempFileFromInputStream(InputStream inputStream, String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, null, MyApplication.context.getCacheDir());
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        out.close();
        return tempFile;
    }

    private String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public MutableLiveData<Boolean> getUploadResult() {
        return uploadResult;
    }
}