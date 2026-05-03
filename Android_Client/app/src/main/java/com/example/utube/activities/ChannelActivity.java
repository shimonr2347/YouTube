package com.example.utube.activities;

import com.example.utube.data.VideoRepository;
import com.example.utube.models.UserDetails;
import com.example.utube.models.Video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.utube.R;
import com.example.utube.activities.VideoManager;
import com.example.utube.utils.VideoResponse;
import com.example.utube.viewmodels.ChannelViewModel;
import com.example.utube.viewmodels.EditVideoViewModel;
import com.example.utube.viewmodels.UserViewModel;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import static com.example.utube.activities.MainActivity.PREFS_NAME;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChannelActivity extends AppCompatActivity {

    private TextView channelTitle;
    private Button editUserButton;
    private Button deleteUserButton;
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String authorName;
    private static final int REQUEST_VIDEO_DETAIL = 1;

    private ChannelViewModel viewModel;

    private VideoRepository videoRepository = new VideoRepository(getApplication());

    private ProgressDialog loadingDialog;

    private String loggedInUser;
    public static final String LOGGED_IN_USER = "logged_in_user";
    private static final String LOGGED_IN_KEY = "logged_in";
    private final UserDetails userDetails = UserDetails.getInstance();
    private UserEditDialog userEditDialog;
    private ChannelViewModel channelViewModel;
    private UserViewModel userViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("isNightMode", false);
        setTheme(isNightMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        viewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        loggedInUser = sharedPreferences.getString(LOGGED_IN_USER, null);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        channelTitle = findViewById(R.id.channel_title);
        editUserButton = findViewById(R.id.edit_user_button);
        deleteUserButton = findViewById(R.id.delete_user_button);
        recyclerView = findViewById(R.id.channel_recycler_view);

        authorName = getIntent().getStringExtra("AUTHOR_NAME");
        channelTitle.setText(authorName + "'s Channel");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoAdapter = new VideoAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(videoAdapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshVideoList);

        setupObservers();

        userEditDialog = new UserEditDialog(this, channelViewModel);
        userEditDialog.setOnDismissListener(() -> refreshVideoList());
        editUserButton.setOnClickListener(v -> {
            // Show UserEditDialog using FragmentManager
            FragmentManager fragmentManager = getSupportFragmentManager();
            userEditDialog.show(fragmentManager, "UserEditDialog");
        });
        checkUserStatus();

        deleteUserButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String userId = UserDetails.getInstance().get_id();
                        String token = UserDetails.getInstance().getToken();
                        userViewModel.deleteUser(userId, token);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        userViewModel.getDeleteUserResult().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                // Delete local data
                userViewModel.deleteUserLocalData(UserDetails.getInstance().getUsername());
                // Clear user data
                UserDetails.getInstance().clear();
                // Return to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        });

        loadVideos();
    }//end onCreate

    private void checkUserStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(LOGGED_IN_KEY, false);
        String visitingUser = getIntent().getStringExtra("AUTHOR_NAME");

        if (isLoggedIn && loggedInUser != null && loggedInUser.equals(visitingUser)) {
            updateUIForLoggedInUser();
        } else {
            updateUIForGuest();
        }
    }

    private void updateUIForGuest() {
        editUserButton.setVisibility(View.GONE);
        deleteUserButton.setVisibility(View.GONE);
    }

    private void updateUIForLoggedInUser() {
        editUserButton.setVisibility(View.VISIBLE);
        deleteUserButton.setVisibility(View.VISIBLE);
    }


    private void setupObservers() {
        viewModel.getVideos().observe(this, videos -> {
            videoAdapter.updateVideos(videos);
            swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_DETAIL && resultCode == RESULT_OK) {
            refreshVideoList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshVideoList();
    }

    private void refreshVideoList() {
        viewModel.loadVideosForAuthor(authorName);
    }

    private void loadVideos() {
        viewModel.loadVideosForAuthor(authorName);
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
        private List<Video> videoList;
        private Context context;
        private EditVideoViewModel editVideoViewModel;

        public VideoAdapter(List<Video> videoList, Context context) {
            this.videoList = videoList != null ? videoList : new ArrayList<>();
            this.context = context;
            this.editVideoViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(EditVideoViewModel.class);
        }

        @Override
        public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new VideoViewHolder(view);
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
        }

        @Override
        public void onBindViewHolder(VideoViewHolder holder, int position) {
            Video video = videoList.get(position);
            holder.bind(video);

            String currentLoggedInUser = UserDetails.getInstance().getUsername();
            boolean isAuthor = currentLoggedInUser != null && currentLoggedInUser.equals(video.getAuthor());

            // Set the visibility of the menu button based on authorship
            holder.menuButton.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            holder.menuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(holder.menuButton.getContext(), holder.menuButton);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.video_item_menu, popupMenu.getMenu());

                if (isAuthor) {
                    // Apply custom styles to menu items
                    for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                        MenuItem menuItem = popupMenu.getMenu().getItem(i);
                        SpannableString spannableTitle = new SpannableString(menuItem.getTitle());
                        spannableTitle.setSpan(new ForegroundColorSpan(Color.parseColor("#D31E1E")), 0, spannableTitle.length(), 0);
                        menuItem.setTitle(spannableTitle);
                    }

                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.edit_video) {
                            if (getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(LOGGED_IN_KEY, false)) {
                                EditVideoDialog dialog = EditVideoDialog.newInstance(video.getId());
                                dialog.setOnDismissListener(dialogInterface -> refreshVideoList());
                                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditVideoDialog");
                            } else {
                                showLoginPromptDialog();
                            }
                            return true;
                        } else if (item.getItemId() == R.id.delete_video) {
                            if (getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(LOGGED_IN_KEY, false)) {
                                editVideoViewModel.deleteVideo(video.getId());
                                editVideoViewModel.getDeleteVideoResult().observe((LifecycleOwner) context, isSuccess -> {
                                    if (isSuccess) {
                                        viewModel.removeVideo(video.getId());
                                        Toast.makeText(context, "Video deleted successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Failed to delete video", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                showLoginPromptDialog();
                            }
                            return true;
                        }
                        return false;
                    });
                }

                popupMenu.show();
            });

            // Video detail page navigation
            holder.itemView.setOnClickListener(v -> {
                // Show loading indicator
                showLoadingDialog();

                // Fetch latest video details from server
                viewModel.fetchVideoDetailsFromServer(video.getId(), new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        hideLoadingDialog();
                        if (response.isSuccessful() && response.body() != null) {
                            VideoResponse updatedVideo = response.body();

                            // Start VideoDetailActivity
                            Intent intent = new Intent(context, VideoDetailActivity.class);
                            intent.putExtra("VIDEO_ID", updatedVideo.getId());
                            intent.putExtra("VIDEO_URL", updatedVideo.getVideoUrl());
                            intent.putExtra("TITLE", updatedVideo.getTitle());
                            intent.putExtra("AUTHOR", updatedVideo.getAuthor());
                            Log.d("ChannelActivity", "Updated video author: " + updatedVideo.getAuthor());
                            intent.putExtra("VIEWS", updatedVideo.getViews());
                            intent.putExtra("UPLOAD_TIME", updatedVideo.getUploadTime());
                            intent.putExtra("AUTHOR_PROFILE_PIC_URL", updatedVideo.getAuthorProfilePic());
                            intent.putExtra("LIKES", updatedVideo.getLikes());
                            ((Activity) context).startActivityForResult(intent, REQUEST_VIDEO_DETAIL);
                        } else {
                            // Show error message
                            Toast.makeText(context, "Failed to fetch latest video details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        hideLoadingDialog();
                        // Show error message
                        Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }//end onBindViewHolder

        @Override
        public int getItemCount() {
            return videoList.size();
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

    private void showLoginPromptDialog() {
        LoginPromptDialog dialog = new LoginPromptDialog();
        dialog.show(getSupportFragmentManager(), "LoginPromptDialog");
    }


}