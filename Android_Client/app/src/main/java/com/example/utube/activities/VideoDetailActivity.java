package com.example.utube.activities;

import static com.example.utube.activities.MainActivity.LOGGED_IN_USER;
import static com.example.utube.activities.MainActivity.PREFS_NAME;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utube.MyApplication;
import com.example.utube.R;
import com.example.utube.data.CommentRepository;
import com.example.utube.models.CommentEntity;
import com.example.utube.models.UserDetails;
import com.example.utube.models.Users;
import com.example.utube.models.Video;
import com.example.utube.utils.CommentResponse;
import com.example.utube.utils.VideoResponse;
import com.example.utube.viewmodels.UserViewModel;
import com.example.utube.viewmodels.VideoDetailViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class VideoDetailActivity extends AppCompatActivity {

    private boolean isFullScreen = false;
    private Button btnFullScreen;
    private VideoView videoView;
    private TextView titleTextView, authorTextView, viewsTextView, uploadTimeTextView, likesTextView, commentsCountTextView;
    private ImageView authorProfilePic;
    private LinearLayout commentsHeaderContainer;

    private RecyclerView commentsRecyclerView;
    private ImageView expandCollapseButton;
    private boolean isCommentsExpanded = false;
    private boolean isLiked = false;
    private String videoId;
    private int likes;
    private int views;
    private List<Video.Comment> comments;
    private CommentsAdapter commentsAdapter;

    // Static HashMaps to keep track of comments and likes state for each video within the session
    private static HashMap<String, List<Video.Comment>> commentsMap = new HashMap<>();
    private static HashMap<String, HashMap<String, Boolean>> likedCommentsStateMap = new HashMap<>();
    private int idCounter = 0;
    private CommentRepository commentRepository;
    private SharedPreferences sharedPreferences;
    public static final String PREFS_NAME = "theme_prefs";
    private VideoDetailViewModel viewModel;
    private ProgressBar likeProgressBar;
    private ProgressBar commentProgressBar;
    private RecyclerView recommendedVideosRecyclerView;
    private VideoAdapter recommendedVideosAdapter;
    private ProgressDialog loadingDialog;
    private UserViewModel userViewModel;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme from shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("isNightMode", false);
        setTheme(isNightMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        videoView = findViewById(R.id.video_view);
        titleTextView = findViewById(R.id.video_title);
        authorTextView = findViewById(R.id.video_author);
        viewsTextView = findViewById(R.id.video_views);
        uploadTimeTextView = findViewById(R.id.video_upload_time);
        authorProfilePic = findViewById(R.id.author_profile_pic);
        likesTextView = findViewById(R.id.likes_count);
        commentsCountTextView = findViewById(R.id.comments_count);
        commentsHeaderContainer = findViewById(R.id.comments_header_container);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        expandCollapseButton = findViewById(R.id.expand_collapse_button);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnFullScreen = findViewById(R.id.btn_full_screen);
        commentProgressBar = findViewById(R.id.comment_progress_bar);

        viewModel = new ViewModelProvider(this).get(VideoDetailViewModel.class); //mvvm-change
        recommendedVideosRecyclerView = findViewById(R.id.recommended_videos_recycler_view);
//        recommendedVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendedVideosAdapter = new VideoAdapter(new ArrayList<>(), sharedPreferences);
        recommendedVideosRecyclerView.setAdapter(recommendedVideosAdapter);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);


        commentsAdapter = new CommentsAdapter(new ArrayList<>());
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsHeaderContainer.setOnClickListener(v -> {
            if (isCommentsExpanded) {
                // Collapse the comments section
                commentsRecyclerView.setVisibility(View.GONE);
                expandCollapseButton.setImageResource(R.drawable.baseline_expand_more_24);
            } else {
                // Expand the comments section
                commentsRecyclerView.setVisibility(View.VISIBLE);
                expandCollapseButton.setImageResource(R.drawable.baseline_expand_less_24);
            }
            isCommentsExpanded = !isCommentsExpanded;
        });
        // Initialize the recommended videos list
         videoId = getIntent().getStringExtra("VIDEO_ID");


        String token1 = UserDetails.getInstance().getToken();
        Log.d("VideoDetailActivity", "Initial token: " + token1);
        if (token1 == null || token1.isEmpty()) {
            token1 = "guest";
            Log.d("VideoDetailActivity", "Token was null or empty, set to guest");
        }
        Log.d("VideoDetailActivity", "Final token used: " + token1);
        Log.d("VideoDetailActivity", "VideoId: " + videoId);
        viewModel.fetchRecommendedVideos(token1, videoId);

        viewModel.getRecommendedVideos().observe(this, videos -> {
            recommendedVideosAdapter.updateVideos(videos);
        });

        viewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        //if the user is logged in, get the token from the user details
        String tokenFornotify = UserDetails.getInstance().getToken();
        if (tokenFornotify != null && !tokenFornotify.isEmpty()) {
            if (videoId != null) {
                userViewModel.notifyVideoWatch(videoId, tokenFornotify);
                Log.d("VideoDetailActivity", "Notified video watch: " + videoId);
            } else {
                Log.e("VideoDetailActivity", "VideoId is null, cannot notify video watch");
            }
        } else {
            Log.d("VideoDetailActivity", "User not logged in, skipping notify video watch");
        }



        // Initialize media controller
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Get video details from intent
        String videoUrl = getIntent().getStringExtra("VIDEO_URL");
        String title = getIntent().getStringExtra("TITLE");
        String author = getIntent().getStringExtra("AUTHOR");
        views = getIntent().getIntExtra("VIEWS", 0);
        String uploadTime = getIntent().getStringExtra("UPLOAD_TIME");
        String authorProfilePicUrl = getIntent().getStringExtra("AUTHOR_PROFILE_PIC_URL");



        // Load likes state from memory
        isLiked = VideoManager.getInstance(getApplication()).getLikedStateMap().getOrDefault(videoId, false);


        viewModel.refreshComments(videoId);
        // MVVM changes
        viewModel.loadVideo(videoId);
        viewModel.loadComments(videoId);

        // Observe only the changing parts of the video data
        viewModel.getVideo().observe(this, video -> { //mvvm-change
            if (video != null) {
                viewsTextView.setText(video.getViews() - 1 + " views");
                likesTextView.setText(video.getLikes() + " likes");
                titleTextView.setText(video.getTitle());
                authorTextView.setText(video.getAuthor());
                uploadTimeTextView.setText(video.getUploadTime());
                loadAuthorProfilePic(video.getAuthorProfilePicUrl());
                // setupVideoPlayer(video.getVideoUrl());
            }
        });

        // Log the URL for debugging
        Log.d("VideoDetailActivity", "Video URL: " + videoUrl);
        // Set video details
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Uri videoUri;
            if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
                videoUri = Uri.parse(videoUrl);
            } else if (videoUrl.startsWith("http")) {
                videoUri = Uri.parse(videoUrl);
            } else if (videoUrl.startsWith("/media/")) {
                // This is the new case for server videos
                videoUri = Uri.parse("http://10.0.2.2:12345" + videoUrl);
            } else if (videoUrl.startsWith("raw/") || videoUrl.startsWith("drawable/")) {
                int videoResId = getResources().getIdentifier(videoUrl, "raw", getPackageName());
                if (videoResId == 0) {
                    videoResId = getResources().getIdentifier(videoUrl, "drawable", getPackageName());
                }
                if (videoResId != 0) {
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
                } else {
                    Toast.makeText(this, "Can't play this video. Resource not found.", Toast.LENGTH_SHORT).show();
                    Log.e("VideoDetailActivity", "Error: Resource not found");
                    return;
                }
            } else {
                File videoFile = new File(videoUrl);
                if (videoFile.exists()) {
                    videoUri = Uri.fromFile(videoFile);
                } else {
                    Toast.makeText(this, "Can't play this video. File not found.", Toast.LENGTH_SHORT).show();
                    Log.e("VideoDetailActivity", "Error: video file not found");
                    return;
                }
            }

            try {
                videoView.setVideoURI(videoUri);
                videoView.setOnPreparedListener(mp -> mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                    if (width == 0 || height == 0) {
                        Toast.makeText(this, "Can't play this video. Invalid video resolution.", Toast.LENGTH_SHORT).show();
                    }
                }));
                videoView.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(VideoDetailActivity.this, "Can't play this video. Error code: " + what, Toast.LENGTH_SHORT).show();
                    return true;
                });
                videoView.start();
            } catch (Exception e) {
                Toast.makeText(this, "Can't play this video. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("VideoDetailActivity", "Error setting video URI: " + e.getMessage(), e);
            }
        } else {
            Log.e("VideoDetailActivity", "Error: videoUrl is null or empty");
            Toast.makeText(this, "Can't play this video", Toast.LENGTH_SHORT).show();
        }

        titleTextView.setText(title);
        authorTextView.setText(author);
        uploadTimeTextView.setText(uploadTime);

        // Load author's profile picture with Picasso, set placeholder and error image
        authorProfilePicUrl = getIntent().getStringExtra("AUTHOR_PROFILE_PIC_URL"); // Make sure this is correctly passed in the intent

        // Attempt to load the image from a URL first if it's not null
        if (authorProfilePicUrl != null && !authorProfilePicUrl.isEmpty()) {
            if (authorProfilePicUrl.startsWith("drawable/")) {
                // Handle drawable resources
                int authorProfilePicResId = getResources().getIdentifier(authorProfilePicUrl, "drawable", getPackageName());
                if (authorProfilePicResId != 0) {
                    authorProfilePic.setImageResource(authorProfilePicResId);
                } else {
                    authorProfilePic.setImageResource(R.drawable.policy);
                }
            } else {
                // Handle remote images
                String fullUrl = "http://10.0.2.2:12345" + authorProfilePicUrl; // Adjust the base URL as needed
                Picasso.get()
                        .load(fullUrl)
                        .placeholder(R.drawable.policy) // Use a placeholder image while loading
                        .error(R.drawable.policy) // Fallback to error image if loading fails
                        .into(authorProfilePic, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("Picasso", "Image loaded successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("Picasso", "Error loading image", e);
                                authorProfilePic.setImageResource(R.drawable.policy); // Set error image directly in case of failure
                            }
                        });
            }
        } else {
            // If URL is null or empty, set to default error image
            authorProfilePic.setImageResource(R.drawable.policy);
        }


        // Set initial like button state
        updateLikeButton();

        commentRepository = ((MyApplication) getApplicationContext()).getCommentRepository();

        viewModel.getComments().observe(this, commentsList -> { //mvvm-change
            if (commentsList != null) {
                //  comments = commentsList;
                comments = new ArrayList<>(commentsList); //create a new list to avoid modifying the original list
                commentsAdapter.updateComments(comments);
                updateCommentsCount();
            }
        });

        // Add comment button click listener
        String finalAuthorProfilePicUrl = authorProfilePicUrl;

        findViewById(R.id.add_comment_button).setOnClickListener(v -> {
            if (sharedPreferences.getBoolean("logged_in", false)) {
                AddCommentDialog dialog = new AddCommentDialog();
                dialog.setAddCommentListener(text -> {
                    if (!text.trim().isEmpty()) {
                        String currentLoggedInUser = sharedPreferences.getString(LOGGED_IN_USER, "");
                        String token = UserDetails.getInstance().getToken();
                        String profilePicUrl = UserDetails.getInstance().getProfilePic();
                        Log.d("VideoDetailActivity", "Adding comment: " + text + ", User: " + currentLoggedInUser + ", Profile pic: " + profilePicUrl);

                        showCommentProgressBar();

                        viewModel.addCommentToServer(videoId, currentLoggedInUser, text, token, new VideoDetailViewModel.AddCommentCallback() {
                            @Override
                            public void onSuccess(CommentResponse commentResponse) {
                                hideCommentProgressBar();
                                CommentEntity newComment = convertResponseToEntity(commentResponse);
                                //make the username be the current logged in user
                                String username = UserDetails.getInstance().getUsername();
                                viewModel.addComment(newComment.videoId, username, newComment.text, profilePicUrl, newComment.uploadTime, newComment.serverId);
                                Log.d("VideoDetailActivity", "Comment added successfully: newComment.ProfilePic " + newComment.profilePicUrl);
                            }

                            @Override
                            public void onError(String message) {
                                hideCommentProgressBar();
                                Toast.makeText(VideoDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                dialog.show(getSupportFragmentManager(), "AddCommentDialog");
            } else {
                showLoginPromptDialog();
            }
        });

        // Share button click listener
        findViewById(R.id.share_button).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, videoUrl);
            startActivity(Intent.createChooser(shareIntent, "Share video via"));
        });

        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {
                    exitFullScreen();
                } else {
                    enterFullScreen();
                }
            }
        });

        likeProgressBar = findViewById(R.id.like_progress_bar);

    }//end onCreate


    private void loadAuthorProfilePic(String authorProfilePicUrl) {
        if (authorProfilePicUrl != null && !authorProfilePicUrl.isEmpty()) {
            if (authorProfilePicUrl.startsWith("drawable/")) {
                int authorProfilePicResId = getResources().getIdentifier(authorProfilePicUrl, "drawable", getPackageName());
                if (authorProfilePicResId != 0) {
                    authorProfilePic.setImageResource(authorProfilePicResId);
                } else {
                    authorProfilePic.setImageResource(R.drawable.policy);
                }
            } else {
                String fullUrl = "http://10.0.2.2:12345" + authorProfilePicUrl;
                Picasso.get()
                        .load(fullUrl)
                        .placeholder(R.drawable.policy)
                        .error(R.drawable.policy)
                        .into(authorProfilePic);
            }
        } else {
            authorProfilePic.setImageResource(R.drawable.policy);
        }
    }

    private void setupVideoPlayer(String videoUrl) {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Uri videoUri;
            if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
                videoUri = Uri.parse(videoUrl);
            } else if (videoUrl.startsWith("http")) {
                videoUri = Uri.parse(videoUrl);
            } else if (videoUrl.startsWith("/media/")) {
                videoUri = Uri.parse("http://10.0.2.2:12345" + videoUrl);
            } else if (videoUrl.startsWith("raw/") || videoUrl.startsWith("drawable/")) {
                int videoResId = getResources().getIdentifier(videoUrl, "raw", getPackageName());
                if (videoResId == 0) {
                    videoResId = getResources().getIdentifier(videoUrl, "drawable", getPackageName());
                }
                if (videoResId != 0) {
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
                } else {
                    Toast.makeText(this, "Can't play this video. Resource not found.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                File videoFile = new File(videoUrl);
                if (videoFile.exists()) {
                    videoUri = Uri.fromFile(videoFile);
                } else {
                    Toast.makeText(this, "Can't play this video. File not found.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            try {
                videoView.setVideoURI(videoUri);
                videoView.start();
            } catch (Exception e) {
                Toast.makeText(this, "Can't play this video. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Can't play this video. No video URL provided.", Toast.LENGTH_SHORT).show();
        }
    }


    private void enterFullScreen() {
        isFullScreen = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        btnFullScreen.setText("Exit Full Screen");

        // Set VideoView to full screen
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoView.setLayoutParams(layoutParams);

        // Hide other UI elements
        titleTextView.setVisibility(View.GONE);
        authorTextView.setVisibility(View.GONE);
        viewsTextView.setVisibility(View.GONE);
        uploadTimeTextView.setVisibility(View.GONE);
        authorProfilePic.setVisibility(View.GONE);
        likesTextView.setVisibility(View.GONE);
        commentsCountTextView.setVisibility(View.GONE);
        commentsRecyclerView.setVisibility(View.GONE);
        findViewById(R.id.add_comment_button).setVisibility(View.GONE);
        findViewById(R.id.share_button).setVisibility(View.GONE);
        findViewById(R.id.like_button).setVisibility(View.GONE);
        //make toast to exit full screen scroll down to see the button
        Toast.makeText(this, "To exit full screen, scroll down and press the Exit Full Screen button", Toast.LENGTH_LONG).show();
    }

    private void exitFullScreen() {
        isFullScreen = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        btnFullScreen.setText("Full Screen");

        // Reset VideoView layout parameters
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        videoView.setLayoutParams(params);

        // Show other UI elements
        titleTextView.setVisibility(View.VISIBLE);
        authorTextView.setVisibility(View.VISIBLE);
        viewsTextView.setVisibility(View.VISIBLE);
        uploadTimeTextView.setVisibility(View.VISIBLE);
        authorProfilePic.setVisibility(View.VISIBLE);
        likesTextView.setVisibility(View.VISIBLE);
        commentsCountTextView.setVisibility(View.VISIBLE);
        commentsRecyclerView.setVisibility(View.VISIBLE);
        findViewById(R.id.add_comment_button).setVisibility(View.VISIBLE);
        findViewById(R.id.share_button).setVisibility(View.VISIBLE);
        findViewById(R.id.like_button).setVisibility(View.VISIBLE);
    }

    private void updateLikeButton() {
        String currentLoggedInUser = sharedPreferences.getString(LOGGED_IN_USER, "");
        isLiked = viewModel.isVideoLiked(videoId, currentLoggedInUser);

        viewModel.getIsLiked().observe(this, liked -> {
            isLiked = liked;
            ((TextView) findViewById(R.id.like_button)).setText(isLiked ? "Unlike" : "Like");
        });

        findViewById(R.id.like_button).setOnClickListener(v -> {
            if (sharedPreferences.getBoolean("logged_in", false)) {
                String userId = sharedPreferences.getString(LOGGED_IN_USER, "");
                String token = UserDetails.getInstance().getToken();
                boolean newLikeStatus = !isLiked;

                likeProgressBar.setVisibility(View.VISIBLE); // Show loading indicator

                viewModel.toggleLikeOnServer(videoId, userId, token, newLikeStatus, new VideoDetailViewModel.ToggleLikeCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            likeProgressBar.setVisibility(View.GONE); // Hide loading indicator
                            viewModel.updateLikeStatus(videoId, userId, newLikeStatus);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            likeProgressBar.setVisibility(View.GONE); // Hide loading indicator
                            Toast.makeText(VideoDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                showLoginPromptDialog();
            }
        });
        ((TextView) findViewById(R.id.like_button)).setText(isLiked ? "Unlike" : "Like");
    }


    private void updateCommentsCount() {
        commentsCountTextView.setText("(" + comments.size() + ")");
    }

    @Override
    public void onBackPressed() {
        Video video = VideoManager.getInstance(getApplication()).getVideoById(videoId);
        if (video != null) {
            video.setViews(views);
            VideoManager.getInstance(getApplication()).updateVideo(video);
        }
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshVideoDetailsOnBack();
        refreshCommentLikesOnResume();
    }

    private void showLoginPromptDialog() {
        LoginPromptDialog dialog = new LoginPromptDialog();
        dialog.show(getSupportFragmentManager(), "LoginPromptDialog");
    }


    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private List<Video.Comment> commentList;

        public CommentsAdapter(List<Video.Comment> commentList) {
            this.commentList = commentList;
        }

        public void updateComments(List<Video.Comment> newComments) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentDiffCallback(this.commentList, newComments));
            this.commentList = new ArrayList<>(newComments);
            diffResult.dispatchUpdatesTo(this);
            Log.d("VideoDetailActivity", "Updated comments list. Size: " + commentList.size());
            for (Video.Comment comment : commentList) {
                Log.d("VideoDetailActivity", "Comment in list: ID=" + comment.getId() + ", Text=" + comment.getText());
                //log the server id
                Log.d("VideoDetailActivity", "Comment in list: Server ID=" + comment.getServerId());
            }
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Video.Comment comment = commentList.get(position);
            holder.bind(comment);
        }

        public int findCommentPosition(int commentId) {
            for (int i = 0; i < commentList.size(); i++) {
                if (commentList.get(i).getId() == commentId) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTextView, commentTextView, uploadTimeTextView, commentLikesTextView;
            ImageView profilePicImageView, expandCollapseIcon;
            Button likeCommentButton, editCommentButton, deleteCommentButton;
            private boolean isCommentLiked = false;

            public CommentViewHolder(View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.comment_username);
                commentTextView = itemView.findViewById(R.id.comment_text);
                uploadTimeTextView = itemView.findViewById(R.id.comment_upload_time);
                commentLikesTextView = itemView.findViewById(R.id.comment_likes_count);
                profilePicImageView = itemView.findViewById(R.id.comment_profile_pic);
                likeCommentButton = itemView.findViewById(R.id.like_comment_button);
                editCommentButton = itemView.findViewById(R.id.edit_comment_button);
                deleteCommentButton = itemView.findViewById(R.id.delete_comment_button);
            }

            public void bind(Video.Comment comment) {
                usernameTextView.setText(comment.getUsername());
                commentTextView.setText(comment.getText());
                uploadTimeTextView.setText(comment.getUploadTime());
                commentLikesTextView.setText(comment.getLikes() + " likes");

                //log the profile pic url
                Log.d("VideoDetailActivity", "Comment.profile pic URL: " + comment.getProfilePicUrl());
                loadImageView(profilePicImageView, comment.getProfilePicUrl());
                Log.d("VideoDetailActivity", "Comment.profile pic URL: " + comment.getProfilePicUrl());

                String currentLoggedInUser = sharedPreferences.getString(LOGGED_IN_USER, "");
                isCommentLiked = viewModel.isCommentLiked(videoId, comment.getId(), currentLoggedInUser);

                boolean isAuthor = currentLoggedInUser.equals(comment.getUsername());
                Log.d("VideoDetailActivity", "Current user: " + currentLoggedInUser + ", Comment author: " + comment.getUsername() + ", Is author: " + isAuthor);
                boolean isLoggedIn = sharedPreferences.getBoolean("logged_in", false);

                if (isLoggedIn && isAuthor) {
                    editCommentButton.setVisibility(View.VISIBLE);
                    deleteCommentButton.setVisibility(View.VISIBLE);
                } else {
                    editCommentButton.setVisibility(View.GONE);
                    deleteCommentButton.setVisibility(View.GONE);
                }

                updateLikeCommentButton();


                likeCommentButton.setOnClickListener(v -> {
                    if (sharedPreferences.getBoolean("logged_in", false)) {
                        String userId = sharedPreferences.getString(LOGGED_IN_USER, "");
                        String token = UserDetails.getInstance().getToken();
                        boolean newLikeStatus = !isCommentLiked;

                        showCommentProgressBar();

                        viewModel.toggleCommentLikeOnServer(videoId, comment.getServerId(), userId, token, newLikeStatus, new VideoDetailViewModel.ToggleCommentLikeCallback() {
                            @Override
                            public void onSuccess(CommentResponse updatedComment) {
                                hideCommentProgressBar();
                                isCommentLiked = newLikeStatus;
                                viewModel.updateCommentLikeStatus(videoId, comment.getId(), userId, isCommentLiked);
                                updateLikeCommentButton();
                                updateLikeCount(convertResponseToComment(updatedComment));
                            }

                            @Override
                            public void onError(String message) {
                                hideCommentProgressBar();
                                Toast.makeText(VideoDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        showLoginPromptDialog();
                    }
                });

                editCommentButton.setOnClickListener(v -> {
                    if (sharedPreferences.getBoolean("logged_in", false)) {
                        AddCommentDialog dialog = AddCommentDialog.newInstance(comment.getText());
                        dialog.setAddCommentListener(newText -> {
                            if (!newText.trim().isEmpty()) {
                                String userId = sharedPreferences.getString(LOGGED_IN_USER, "");
                                String token = UserDetails.getInstance().getToken();

                                showCommentProgressBar();

                                viewModel.updateCommentOnServer(videoId, comment.getServerId(), userId, newText, token, new VideoDetailViewModel.UpdateCommentCallback() {
                                    @Override
                                    public void onSuccess(CommentResponse updatedComment) {
                                        hideCommentProgressBar();
                                        CommentEntity updatedCommentEntity = new CommentEntity(
                                                videoId,
                                                comment.getUsername(),
                                                newText,  // Use the new text from the edit dialog
                                                comment.getUploadTime(),
                                                comment.getLikes(),
                                                comment.getProfilePicUrl(),
                                                comment.getServerId()
                                        );
                                        updatedCommentEntity.setId(comment.getId());
                                        viewModel.updateComment(updatedCommentEntity);
                                        Toast.makeText(VideoDetailActivity.this, "Comment updated successfully", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        hideCommentProgressBar();
                                        Toast.makeText(VideoDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        dialog.show(getSupportFragmentManager(), "EditCommentDialog");
                    } else {
                        showLoginPromptDialog();
                    }
                });


                deleteCommentButton.setOnClickListener(v -> {
                    if (sharedPreferences.getBoolean("logged_in", false)) {
                        String userId = sharedPreferences.getString(LOGGED_IN_USER, "");
                        String token = UserDetails.getInstance().getToken();

                        showCommentProgressBar();

                        viewModel.deleteCommentOnServer(videoId, comment.getServerId(), userId, token, new VideoDetailViewModel.DeleteCommentCallback() {
                            @Override
                            public void onSuccess() {
                                hideCommentProgressBar();
                                CommentEntity commentEntity = convertToCommentEntity(comment);
                                viewModel.deleteComment(commentEntity);
                                Toast.makeText(VideoDetailActivity.this, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                hideCommentProgressBar();
                                Toast.makeText(VideoDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        showLoginPromptDialog();
                    }
                });


            }

            private CommentEntity convertToCommentEntity(Video.Comment comment) {
                CommentEntity entity = new CommentEntity(videoId, comment.getUsername(), comment.getText(),
                        comment.getUploadTime(), comment.getLikes(),
                        comment.getProfilePicUrl(), comment.getServerId());
                entity.setId(comment.getId());
                return entity;
            }



            private void loadImageView(ImageView imageView, String imageUrl) {
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageView.setImageResource(R.drawable.policy);
                } else if (imageUrl.startsWith("drawable/")) {
                    // Handle drawable resources
                    int imageResId = getResources().getIdentifier(imageUrl, null, getPackageName());
                    if (imageResId != 0) {
                        imageView.setImageResource(imageResId);
                    } else {
                        imageView.setImageResource(R.drawable.policy);
                    }
                } else {
                    // Handle remote images
                    String fullUrl = "http://10.0.2.2:12345" + imageUrl; // Adjust the base URL as needed
                    Picasso.get().load(fullUrl).error(R.drawable.policy).into(imageView);
                }
            }


            private void updateLikeCommentButton() {
                likeCommentButton.setText(isCommentLiked ? "Unlike" : "Like");
            }

            private void updateLikeCount(Video.Comment comment) {
                commentLikesTextView.setText(comment.getLikes() + " likes");
            }
        }

        private class CommentDiffCallback extends DiffUtil.Callback {
            private final List<Video.Comment> oldList;
            private final List<Video.Comment> newList;

            public CommentDiffCallback(List<Video.Comment> oldList, List<Video.Comment> newList) {
                this.oldList = oldList;
                this.newList = newList;
            }

            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Video.Comment oldComment = oldList.get(oldItemPosition);
                Video.Comment newComment = newList.get(newItemPosition);
                return oldComment.getText().equals(newComment.getText());
            }
        }

    }//end CommentsAdapter

    private void showCommentProgressBar() {
        commentProgressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.add_comment_button).setEnabled(false);
    }

    private void hideCommentProgressBar() {
        commentProgressBar.setVisibility(View.GONE);
        findViewById(R.id.add_comment_button).setEnabled(true);
    }

    private CommentEntity convertResponseToEntity(CommentResponse response) {
        return new CommentEntity(
                videoId,
                response.getUsername(),
                response.getText(),
                response.getUploadTime(),
                response.getLikes(),
                response.getProfilePicUrl(),
                response.getServerId()
        );
    }

    private Video.Comment convertResponseToComment(CommentResponse response) {
        return new Video.Comment(
                response.getId(),
                response.getUsername(),
                response.getText(),
                response.getUploadTime(),
                response.getLikes(),
                response.getProfilePicUrl(),
                response.getServerId()
        );
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
        private List<Video> videoList;
        private SharedPreferences sharedPreferences;

        public VideoAdapter(List<Video> videoList, SharedPreferences sharedPreferences) {
            this.videoList = videoList != null ? videoList : new ArrayList<>();
            this.sharedPreferences = sharedPreferences;
            Log.d("VideoAdapter", "Number of videos passed to adapter: " + (this.videoList != null ? this.videoList.size() : "null"));
        }

        public void updateVideos(List<Video> newVideos) {
            if (this.videoList == null) {
                this.videoList = new ArrayList<>();
            }
            this.videoList.clear();
            if (newVideos != null) {
                this.videoList.addAll(newVideos);
            }
            notifyDataSetChanged();
            Log.d("VideoAdapter", "Videos updated, new size: " + (newVideos != null ? newVideos.size() : 0));
        }

        @Override
        public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(VideoViewHolder holder, int position) {
            Video video = videoList.get(position);
            holder.bind(video);
            String currentLoggedInUser = UserDetails.getInstance().getUsername();
            boolean isAuthor = currentLoggedInUser != null && currentLoggedInUser.equals(video.getAuthor());

            //set the visibility of the menu button gone to all
            holder.menuButton.setVisibility(View.GONE);


            holder.itemView.setOnClickListener(v -> {
                // Show loading indicator
                showLoadingDialog();

                // Fetch latest video details from server
                viewModel.fetchVideoDetailsFromServer(video.getId(), new retrofit2.Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        hideLoadingDialog();
                        if (response.isSuccessful() && response.body() != null) {
                            VideoResponse updatedVideo = response.body();

                            // Start VideoDetailActivity
                            Intent intent = new Intent(VideoDetailActivity.this, VideoDetailActivity.class);
                            intent.putExtra("VIDEO_ID", updatedVideo.getId());
                            intent.putExtra("VIDEO_URL", updatedVideo.getVideoUrl());
                            intent.putExtra("TITLE", updatedVideo.getTitle());
                            intent.putExtra("AUTHOR", updatedVideo.getAuthor());
                            Log.d("MainActivity", "Updated video author: " + updatedVideo.getAuthor());
                            intent.putExtra("VIEWS", updatedVideo.getViews());
                            intent.putExtra("UPLOAD_TIME", updatedVideo.getUploadTime());
                            intent.putExtra("AUTHOR_PROFILE_PIC_URL", updatedVideo.getAuthorProfilePic());
                            intent.putExtra("LIKES", updatedVideo.getLikes());
                            startActivityForResult(intent, 1);
                        } else {
                            // Show error message
                            Toast.makeText(VideoDetailActivity.this, "Failed to fetch latest video details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        hideLoadingDialog();
                        // Show error message
                        Toast.makeText(VideoDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });


        }

        @Override
        public int getItemCount() {
            //return videoList.size();
            return videoList != null ? videoList.size() : 0;
        }

        class VideoViewHolder extends RecyclerView.ViewHolder {
            TextView title, author, views, uploadTime;
            ImageView thumbnail, authorProfilePic;
            Button menuButton;

            public VideoViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.video_title);
                author = itemView.findViewById(R.id.video_author);
                views = itemView.findViewById(R.id.video_views);
                uploadTime = itemView.findViewById(R.id.video_upload_time);
                thumbnail = itemView.findViewById(R.id.video_thumbnail);
                authorProfilePic = itemView.findViewById(R.id.author_profile_pic);
                menuButton = itemView.findViewById(R.id.menu_button);
            }

            public void bind(Video video) {
                title.setText(video.getTitle());
                author.setText(video.getAuthor());
                views.setText(video.getViews() + " views");
                uploadTime.setText(video.getUploadTime());

                loadImageView(thumbnail, video.getThumbnailUrl());
                loadImageView(authorProfilePic, video.getAuthorProfilePicUrl());
                authorProfilePic.setOnClickListener(v -> {
                    Intent intent = new Intent(VideoDetailActivity.this, ChannelActivity.class);
                    intent.putExtra("AUTHOR_NAME", video.getAuthor());
                    startActivity(intent);
                });

            }

            private void loadImageView(ImageView imageView, String imageUrl) {
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageView.setImageResource(R.drawable.policy);
                } else if (imageUrl.startsWith("drawable/")) {
                    // Handle drawable resources
                    int imageResId = getResources().getIdentifier(imageUrl, null, getPackageName());
                    if (imageResId != 0) {
                        imageView.setImageResource(imageResId);
                    } else {
                        imageView.setImageResource(R.drawable.policy);
                    }
                } else {
                    // Handle remote images
                    String fullUrl = "http://10.0.2.2:12345" + imageUrl; // Adjust the base URL as needed
                    Picasso.get().load(fullUrl).error(R.drawable.policy).into(imageView);
                }
            }

        }
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("Loading...");
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void refreshVideoDetailsOnBack() {
        Video videoForUpdate = VideoManager.getInstance(getApplication()).getVideoById(videoId);
        int updatedLikes = videoForUpdate.getLikes();
        likesTextView.setText(updatedLikes + " likes");

        String currentLoggedInUser = sharedPreferences.getString(LOGGED_IN_USER, "");
        isLiked = viewModel.isVideoLiked(videoId, currentLoggedInUser);
        ((TextView) findViewById(R.id.like_button)).setText(isLiked ? "Unlike" : "Like");
    }

    private void refreshCommentLikesOnResume() {
        List<Video.Comment> currentComments = viewModel.getComments().getValue();
        if (currentComments != null && !currentComments.isEmpty()) {
            for (int i = 0; i < currentComments.size(); i++) {
                Video.Comment comment = currentComments.get(i);
                CommentEntity updatedComment = commentRepository.getCommentById(comment.getId());
                if (updatedComment != null) {
                    comment.setLikes(updatedComment.getLikes());
                }
            }
            if (commentsAdapter != null) {
                commentsAdapter.notifyDataSetChanged();
            }
        }
    }
}
