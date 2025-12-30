package edu.travels.travelapp.ui.user.myposts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.travels.travelapp.R;
import edu.travels.travelapp.di.ApiService;
import edu.travels.travelapp.di.RetrofitClient;
import edu.travels.travelapp.model.dto.ImageItemDTO;
import edu.travels.travelapp.model.vo.ResultVO;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends AppCompatActivity {
    private static final String TAG = "MyPostsActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "user_id";
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MyPostsAdapter adapter;
    private List<ImageItemDTO> imageList;
    private ApiService apiService;
    private String currentUserId;
    
    private int currentPage = 1;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMore = true;
    
    private ImageItemDTO currentEditingItem;
    private Uri selectedImageUri;
    private File selectedImageFile;
    private ImageView currentDialogImageView; // 保存当前对话框中的 ImageView 引用
    
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null && currentDialogImageView != null) {
                        // 更新对话框中的图片显示
                        Glide.with(MyPostsActivity.this).load(selectedImageUri).into(currentDialogImageView);
                        // 将图片复制到缓存文件
                        selectedImageFile = copyUriToCache(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        
        // 获取当前用户ID
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = sp.getString(KEY_USER_ID, null);
        
        if (currentUserId == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupRecyclerView();
        loadMyPosts();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("我的文章");
        toolbar.setNavigationOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.rv_my_posts);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        
        apiService = RetrofitClient.getInstance().create(ApiService.class);
        imageList = new ArrayList<>();
        
        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            hasMore = true;
            imageList.clear();
            adapter.notifyDataSetChanged();
            loadMyPosts();
        });
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new MyPostsAdapter(imageList, new MyPostsAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(ImageItemDTO imageItem) {
                showEditDialog(imageItem);
            }
            
            @Override
            public void onDeleteClick(ImageItemDTO imageItem) {
                showDeleteConfirmDialog(imageItem);
            }
        });
        
        recyclerView.setAdapter(adapter);
        
        // 滚动加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    if (!isLoading && hasMore) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount 
                            && firstVisibleItemPosition >= 0) {
                            loadMorePosts();
                        }
                    }
                }
            }
        });
    }
    
    private void loadMyPosts() {
        if (isLoading) return;
        
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        // 注意：这里使用 /api/image/list 接口，然后在前端过滤出当前用户的图片
        // 如果后端有专门的接口（如 /api/image/my），可以替换
        apiService.getImageList(currentPage, pageSize).enqueue(new Callback<ResultVO<List<ImageItemDTO>>>() {
            @Override
            public void onResponse(Call<ResultVO<List<ImageItemDTO>>> call, Response<ResultVO<List<ImageItemDTO>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ResultVO<List<ImageItemDTO>> resultVO = response.body();
                    
                    if (resultVO.getCode() == 200 && resultVO.getData() != null) {
                        List<ImageItemDTO> allImages = resultVO.getData();
                        
                        // 过滤出当前用户的图片
                        List<ImageItemDTO> myImages = new ArrayList<>();
                        for (ImageItemDTO item : allImages) {
                            if (currentUserId != null && currentUserId.equals(item.getUserId())) {
                                // 过滤掉头像图片
                                String description = item.getDescription();
                                if (description != null && !description.trim().toLowerCase().contains("avatar")) {
                                    myImages.add(item);
                                }
                            }
                        }
                        
                        if (!myImages.isEmpty()) {
                            if (currentPage == 1) {
                                imageList.clear();
                            }
                            imageList.addAll(myImages);
                            adapter.notifyDataSetChanged();
                            hasMore = myImages.size() >= pageSize;
                        } else {
                            if (currentPage == 1) {
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                            hasMore = false;
                        }
                    } else {
                        Toast.makeText(MyPostsActivity.this, 
                            resultVO.getMsg() != null ? resultVO.getMsg() : "获取失败", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyPostsActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ResultVO<List<ImageItemDTO>>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "加载失败", t);
                Toast.makeText(MyPostsActivity.this, "加载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadMorePosts() {
        if (isLoading || !hasMore) return;
        currentPage++;
        loadMyPosts();
    }
    
    private void showEditDialog(ImageItemDTO imageItem) {
        currentEditingItem = imageItem;
        selectedImageUri = null;
        selectedImageFile = null;
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_image, null);
        
        ImageView ivImage = dialogView.findViewById(R.id.iv_image);
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        EditText etLocation = dialogView.findViewById(R.id.et_location);
        
        // 保存 ImageView 引用，用于图片选择后更新
        currentDialogImageView = ivImage;
        
        // 加载当前图片
        if (imageItem.getImageUrl() != null && !imageItem.getImageUrl().isEmpty()) {
            Glide.with(this).load(imageItem.getImageUrl()).into(ivImage);
        }
        
        // 解析标题和描述（从 description 字段中分离）
        String[] titleAndDesc = parseTitleAndDescription(imageItem.getDescription());
        etTitle.setText(titleAndDesc[0]);
        etDescription.setText(titleAndDesc[1]);
        etLocation.setText(imageItem.getLocationName() != null ? imageItem.getLocationName() : "");
        
        // 图片点击选择新图片
        ivImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
        
        // 保存原始值，用于比较是否有变化
        String[] originalTitleAndDesc = parseTitleAndDescription(imageItem.getDescription());
        final String originalTitle = originalTitleAndDesc[0];
        final String originalDescription = originalTitleAndDesc[1];
        final String originalLocationName = imageItem.getLocationName() != null ? imageItem.getLocationName() : "";
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("编辑文章")
            .setView(dialogView)
            .setPositiveButton("保存", (d, which) -> {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String locationName = etLocation.getText().toString().trim();
                
                // 验证标题必填
                if (title.isEmpty()) {
                    Toast.makeText(MyPostsActivity.this, "请输入景点标题", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 检查是否有任何字段被修改
                boolean titleChanged = !title.equals(originalTitle);
                boolean descriptionChanged = !description.equals(originalDescription);
                boolean locationChanged = !locationName.equals(originalLocationName);
                boolean imageChanged = selectedImageFile != null;
                
                if (!titleChanged && !descriptionChanged && !locationChanged && !imageChanged) {
                    Toast.makeText(MyPostsActivity.this, "没有修改任何内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 合并标题和描述（使用新值）
                String fullDescription = title;
                if (!description.isEmpty()) {
                    fullDescription = title + ". " + description;
                }
                
                // 如果选择了新图片，先上传新图片，然后删除旧图片
                if (imageChanged) {
                    uploadNewImageAndUpdate(imageItem, fullDescription, locationName);
                } else {
                    // 只更新描述和位置
                    // 传递原始值作为后备，如果新值为空则使用原始值
                    String originalFullDescription = originalTitle;
                    if (!originalDescription.isEmpty()) {
                        originalFullDescription = originalTitle + ". " + originalDescription;
                    }
                    updateImage(imageItem.getImageId(), fullDescription, locationName, originalFullDescription, originalLocationName);
                }
            })
            .setNegativeButton("取消", null)
            .create();
        
        dialog.show();
    }
    
    /**
     * 从 description 字段中解析出标题和描述
     * 格式：标题. 描述 或 标题
     */
    private String[] parseTitleAndDescription(String description) {
        String[] result = new String[]{"", ""};
        if (description == null || description.trim().isEmpty()) {
            return result;
        }
        
        // 查找第一个 ". " 分隔符
        int dotIndex = description.indexOf(". ");
        if (dotIndex > 0) {
            result[0] = description.substring(0, dotIndex).trim();
            result[1] = description.substring(dotIndex + 2).trim();
        } else {
            // 没有分隔符，整个作为标题
            result[0] = description.trim();
        }
        
        return result;
    }
    
    private File copyUriToCache(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File cacheDir = getCacheDir();
            File tempFile = new File(cacheDir, "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "复制图片失败", e);
            return null;
        }
    }
    
    private void uploadNewImageAndUpdate(ImageItemDTO oldImageItem, String description, String locationName) {
        if (selectedImageFile == null) {
            Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建文件部分
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), selectedImageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", selectedImageFile.getName(), requestFile);
        
        // 创建其他参数
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description != null ? description : "");
        RequestBody locationBody = RequestBody.create(MediaType.parse("text/plain"), locationName != null ? locationName : "");
        
        // 使用旧图片的GPS坐标（如果有）
        RequestBody gpsLatBody = null;
        RequestBody gpsLngBody = null;
        if (oldImageItem.getGpsLat() != null && oldImageItem.getGpsLng() != null) {
            gpsLatBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(oldImageItem.getGpsLat()));
            gpsLngBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(oldImageItem.getGpsLng()));
        }
        
        // 上传新图片
        apiService.uploadImage(filePart, descriptionBody, gpsLatBody, gpsLngBody, locationBody)
            .enqueue(new Callback<ResultVO<String>>() {
                @Override
                public void onResponse(Call<ResultVO<String>> call, Response<ResultVO<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ResultVO<String> resultVO = response.body();
                        if (resultVO.getCode() == 200) {
                            // 新图片上传成功，删除旧图片
                            Toast.makeText(MyPostsActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            deleteImage(oldImageItem.getImageId(), true);
                        } else {
                            Toast.makeText(MyPostsActivity.this, 
                                resultVO.getMsg() != null ? resultVO.getMsg() : "上传失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyPostsActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ResultVO<String>> call, Throwable t) {
                    Log.e(TAG, "上传新图片失败", t);
                    Toast.makeText(MyPostsActivity.this, "上传失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void updateImage(String imageId, String description, String locationName, String originalDescription, String originalLocationName) {
        // 如果字段为空，使用原始值（避免清空已有数据）
        String finalDescription = (description != null && !description.isEmpty()) ? description : originalDescription;
        String finalLocationName = (locationName != null && !locationName.isEmpty()) ? locationName : originalLocationName;
        
        apiService.updateImage(imageId, finalDescription, finalLocationName).enqueue(new Callback<ResultVO<String>>() {
            @Override
            public void onResponse(Call<ResultVO<String>> call, Response<ResultVO<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResultVO<String> resultVO = response.body();
                    if (resultVO.getCode() == 200) {
                        Toast.makeText(MyPostsActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        // 刷新列表
                        currentPage = 1;
                        hasMore = true;
                        imageList.clear();
                        adapter.notifyDataSetChanged();
                        loadMyPosts();
                    } else {
                        Toast.makeText(MyPostsActivity.this, 
                            resultVO.getMsg() != null ? resultVO.getMsg() : "更新失败", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyPostsActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ResultVO<String>> call, Throwable t) {
                Log.e(TAG, "更新失败", t);
                Toast.makeText(MyPostsActivity.this, "更新失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDeleteConfirmDialog(ImageItemDTO imageItem) {
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这篇文章吗？")
            .setPositiveButton("删除", (dialog, which) -> deleteImage(imageItem.getImageId()))
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void deleteImage(String imageId) {
        deleteImage(imageId, false);
    }
    
    private void deleteImage(String imageId, boolean silent) {
        apiService.deleteImage(imageId).enqueue(new Callback<ResultVO<String>>() {
            @Override
            public void onResponse(Call<ResultVO<String>> call, Response<ResultVO<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResultVO<String> resultVO = response.body();
                    if (resultVO.getCode() == 200) {
                        if (!silent) {
                            Toast.makeText(MyPostsActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                        // 刷新列表
                        currentPage = 1;
                        hasMore = true;
                        imageList.clear();
                        adapter.notifyDataSetChanged();
                        loadMyPosts();
                    } else {
                        if (!silent) {
                            Toast.makeText(MyPostsActivity.this, 
                                resultVO.getMsg() != null ? resultVO.getMsg() : "删除失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // 即使解析失败，如果HTTP状态码是200，也认为删除成功
                    // 因为后端已经返回了 "删除成功" 的消息
                    if (response.code() == 200) {
                        if (!silent) {
                            Toast.makeText(MyPostsActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                        // 刷新列表
                        currentPage = 1;
                        hasMore = true;
                        imageList.clear();
                        adapter.notifyDataSetChanged();
                        loadMyPosts();
                    } else {
                        if (!silent) {
                            Toast.makeText(MyPostsActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ResultVO<String>> call, Throwable t) {
                Log.e(TAG, "删除失败", t);
                if (!silent) {
                    Toast.makeText(MyPostsActivity.this, "删除失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

