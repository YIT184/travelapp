package edu.travels.travelapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import edu.travels.travelapp.R;
import edu.travels.travelapp.di.ApiService;
import edu.travels.travelapp.di.RetrofitClient;
import edu.travels.travelapp.model.dto.ImageItemDTO;
import edu.travels.travelapp.model.vo.ResultVO;
import edu.travels.travelapp.ui.publish.PublishFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ImageAdapter.OnImageInteractionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;
    private ImageAdapter imageAdapter;
    private List<ImageItemDTO> imageList;
    
    private int currentPage = 1;
    private int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadImages();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAdd = view.findViewById(R.id.fab_add);
        
        if (recyclerView == null || fabAdd == null) {
            Log.e("HomeFragment", "关键视图组件未找到");
            return;
        }
        
        imageList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        
        imageAdapter = new ImageAdapter(imageList, this);
        recyclerView.setAdapter(imageAdapter);
        
        // 添加滚动监听实现分页加载
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                
                if (!isLoading && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount 
                        && firstVisibleItemPosition >= 0) {
                        loadMoreImages();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            PublishFragment publishFragment = new PublishFragment();
            
            // 设置上传成功监听器
            publishFragment.setOnImageUploadListener(new PublishFragment.OnImageUploadListener() {
                @Override
                public void onImageUploadSuccess() {
                    // 上传成功后刷新图片列表
                    refreshImages();
                }
            });
            
            publishFragment.show(getParentFragmentManager(), "publish");
        });
    }

    private void loadImages() {
        if (isLoading) return;
        
        isLoading = true;
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<ResultVO<List<ImageItemDTO>>> call = apiService.getImageList(currentPage, pageSize);
        
        call.enqueue(new Callback<ResultVO<List<ImageItemDTO>>>() {
            @Override
            public void onResponse(Call<ResultVO<List<ImageItemDTO>>> call, Response<ResultVO<List<ImageItemDTO>>> response) {
                isLoading = false;
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ResultVO<List<ImageItemDTO>> resultVO = response.body();
                    
                    // 检查后端返回的状态码
                    if (resultVO.isSuccess() && resultVO.getData() != null) {
                        // 后端直接返回 List 数组
                        List<ImageItemDTO> newImages = resultVO.getData();
                        
                        // 添加调试日志：打印获取到的图片信息和用户信息
                        Log.d("HomeFragment", "获取到 " + (newImages != null ? newImages.size() : 0) + " 张图片");
                        if (newImages != null && !newImages.isEmpty()) {
                            for (int i = 0; i < newImages.size(); i++) {
                                ImageItemDTO item = newImages.get(i);
                                String imageUrl = item.getImageUrl() != null ? item.getImageUrl() : "null";
                                String imageId = item.getImageId() != null ? item.getImageId() : "null";
                                String userId = item.getUserId() != null ? item.getUserId() : "null";
                                String nickname = item.getNickname() != null ? item.getNickname() : "null";
                                String userAvatarUrl = item.getUserAvatarUrl() != null ? item.getUserAvatarUrl() : "null";
                                Log.d("HomeFragment", String.format("图片[%d]: imageId=%s, userId=%s, nickname=%s, userAvatarUrl=%s, imageUrl=%s, description=%s", 
                                    i, imageId, userId, nickname, userAvatarUrl, imageUrl, 
                                    item.getDescription() != null ? item.getDescription() : "null"));
                                
                                // 检查是否有重复的URL
                                if (i > 0) {
                                    String prevUrl = newImages.get(i - 1).getImageUrl();
                                    if (imageUrl.equals(prevUrl)) {
                                        Log.w("HomeFragment", String.format("警告：图片[%d]和[%d]的URL相同: %s", i - 1, i, imageUrl));
                                    }
                                }
                            }
                        }
                        
                        if (newImages != null && !newImages.isEmpty()) {
                            // 过滤掉头像图片（description包含"avatar"或为空的图片，忽略大小写）
                            List<ImageItemDTO> filteredImages = new ArrayList<>();
                            for (ImageItemDTO item : newImages) {
                                String description = item.getDescription();
                                String descNorm = description != null ? description.trim().toLowerCase() : "";
                                // 过滤掉描述为空或包含 avatar 的图片（视为头像）
                                if (descNorm.isEmpty() || descNorm.contains("avatar")) {
                                    Log.d("HomeFragment", "过滤掉头像图片: imageId=" + item.getImageId() + ", description=" + description);
                                } else {
                                    filteredImages.add(item);
                                }
                            }
                            
                            if (!filteredImages.isEmpty()) {
                                if (currentPage == 1) {
                                    // 第一页：清空并重新填充
                                    imageList.clear();
                                    imageList.addAll(filteredImages);
                                    Log.d("HomeFragment", "刷新列表，共 " + imageList.size() + " 张图片（已过滤头像）");
                                    
                                    // 验证数据：检查是否有重复的URL
                                    java.util.Set<String> urlSet = new java.util.HashSet<>();
                                    for (ImageItemDTO item : imageList) {
                                        String url = item.getImageUrl();
                                        if (url != null) {
                                            if (urlSet.contains(url)) {
                                                Log.w("HomeFragment", "发现重复的图片URL: " + url);
                                            } else {
                                                urlSet.add(url);
                                            }
                                        }
                                    }
                                    
                                    // 使用notifyDataSetChanged确保完全刷新
                                    imageAdapter.updateData(new java.util.ArrayList<>(imageList));
                                } else {
                                    int startPosition = imageList.size();
                                    imageList.addAll(filteredImages);
                                    Log.d("HomeFragment", "加载更多，从位置 " + startPosition + " 开始，新增 " + filteredImages.size() + " 张（已过滤头像）");
                                    imageAdapter.addData(filteredImages);
                                }
                            } else {
                                Log.d("HomeFragment", "当前页所有图片都是头像，已过滤");
                            }
                            
                            // 根据返回的列表大小判断是否还有更多数据
                            hasMore = newImages.size() >= pageSize;
                            Log.d("HomeFragment", "当前页返回 " + newImages.size() + " 张图片, 当前列表: " + imageList.size() + ", 还有更多: " + hasMore);
                        } else {
                            hasMore = false;
                            if (currentPage == 1) {
                                Toast.makeText(getContext(), "暂无图片", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        String errorMsg = resultVO.getMsg() != null ? resultVO.getMsg() : "加载失败";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("HomeFragment", "后端返回错误: code=" + resultVO.getCode() + ", msg=" + errorMsg);
                    }
                } else {
                    String errorMsg = "加载失败";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "解析错误响应失败", e);
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("HomeFragment", "HTTP错误: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResultVO<List<ImageItemDTO>>> call, Throwable t) {
                isLoading = false;
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("HomeFragment", "网络请求失败", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreImages() {
        if (isLoading || !hasMore) return;
        
        currentPage++;
        loadImages();
    }

    public void refreshImages() {
        currentPage = 1;
        hasMore = true;
        loadImages();
    }

}