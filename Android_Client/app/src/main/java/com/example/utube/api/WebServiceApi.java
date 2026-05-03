package com.example.utube.api;

import com.example.utube.models.UserDetails;
import com.example.utube.models.Video;
import com.example.utube.utils.CommentRequest;
import com.example.utube.utils.CommentResponse;
import com.example.utube.utils.LoginRequest;
import com.example.utube.utils.LoginResponse;
import com.example.utube.utils.VideoResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebServiceApi {
    @POST("tokens")
    Call<LoginResponse> checkUserNameAndPassword(@Body LoginRequest loginRequest);

    @GET("users/{id}")
    Call<UserDetails> getUser(@Path("id") String id, @Header("Authorization") String token);

    @Multipart
    @POST("users")
    Call<UserDetails> signUp(
            @Part("firstName") RequestBody firstName,
            @Part("lastName") RequestBody lastName,
            @Part("date") RequestBody date,
            @Part("email") RequestBody email,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part MultipartBody.Part profilePic
    );


    @GET("videos")
    Call<List<VideoResponse>> getVideos();


    @FormUrlEncoded
    @POST("cpp/get-recommendations")
    Call<List<VideoResponse>> getRecommendedVideos(
            @Field("videoId") String videoId,
            @Field("token") String token);


    @GET("users/{userId}/videos/{videoId}")
    Call<VideoResponse> getVideo(@Path("videoId") String videoId);

    @PUT("users/{userId}/videos/{videoId}/likes")
    Call<VideoResponse> likeVideo(@Path("userId") String userId, @Path("videoId") String videoId, @Header("Authorization") String token);

    @PUT("users/{userId}/videos/{videoId}/unlikes")
    Call<VideoResponse> unlikeVideo(@Path("userId") String userId, @Path("videoId") String videoId, @Header("Authorization") String token);

    @POST("users/{userId}/videos/{videoId}/comments")
    Call<CommentResponse> addComment(
            @Path("userId") String userId,
            @Path("videoId") String videoId,
            @Body CommentRequest commentRequest,
            @Header("Authorization") String token
    );

    @PUT("users/{userId}/videos/{videoId}/comments/{commentId}/like")
    Call<CommentResponse> likeComment(@Path("userId") String userId, @Path("videoId") String videoId, @Path("commentId") String commentId, @Header("Authorization") String token);

    @PUT("users/{userId}/videos/{videoId}/comments/{commentId}/unLike")
    Call<CommentResponse> unlikeComment(@Path("userId") String userId, @Path("videoId") String videoId, @Path("commentId") String commentId, @Header("Authorization") String token);

    @PUT("users/{userId}/videos/{videoId}/comments/{commentId}")
    Call<CommentResponse> updateComment(
            @Path("userId") String userId,
            @Path("videoId") String videoId,
            @Path("commentId") String commentId,
            @Body CommentRequest commentRequest,
            @Header("Authorization") String token
    );

    @DELETE("users/{userId}/videos/{videoId}/comments/{commentId}")
    Call<Void> deleteComment(
            @Path("userId") String userId,
            @Path("videoId") String videoId,
            @Path("commentId") String commentId,
            @Header("Authorization") String token
    );

    @GET("users/name/{username}/videos")
    Call<List<VideoResponse>> getVideosByUsername(@Path("username") String username);

    @GET("users/{id}/page")
    Call<UserDetails> getUserPage(@Path("id") String id);

    @GET("users/{id}/videos")
    Call<List<VideoResponse>> getUserVideos(@Path("id") String id);

    @Multipart
    @PUT("users/{id}")
    Call<UserDetails> updateUserDetails(
            @Path("id") String id,
            @Part("firstName") RequestBody firstName,
            @Part("lastName") RequestBody lastName,
            @Part("date") RequestBody date,
            @Part("email") RequestBody email,
            @Part MultipartBody.Part profilePic,
            @Header("Authorization") String token
    );

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") String id, @Header("Authorization") String token);

    @GET("videos/{videoId}/comments")
    Call<List<CommentResponse>> getComments(@Path("videoId") String videoId);

    @DELETE("users/{userId}/videos/{videoId}")
    Call<Void> deleteVideo(
            @Header("Authorization") String token,
            @Path("userId") String userId,
            @Path("videoId") String videoId
    );

    @Multipart
    @PUT("users/{userId}/videos/{videoId}")
    Call<Void> editVideo(
            @Header("Authorization") String token,
            @Path("userId") String userId,
            @Path("videoId") String videoId,
            @Part("title") RequestBody title,
            @Part("category") RequestBody category,
            @Part MultipartBody.Part video
    );

    @Multipart
    @POST("users/{userId}/videos")
    Call<Void> uploadVideo(
            @Header("Authorization") String token,
            @Path("userId") String userId,
            @Part("title") RequestBody title,
            @Part("category") RequestBody category,
            @Part("authorId") RequestBody authorId,
            @Part("authorName") RequestBody authorName,
            @Part("views") RequestBody views,
            @Part("likes") RequestBody likes,
            @Part MultipartBody.Part video,
            @Part MultipartBody.Part thumbnail
    );

    @POST("cpp/create-thread")
    Call<Void> createUserThread(@Header("Authorization") String token);

    @POST("cpp/close-thread")
    Call<Void> closeUserThread(@Header("Authorization") String token);

    @POST("cpp/notify-watch")
    Call<Void> notifyVideoWatch(@Body RequestBody body, @Header("Authorization") String token);
}
