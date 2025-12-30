package edu.travels.travelapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

//高德定位所需库
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.Collections;
import java.util.Comparator;


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

    private AMapLocationClient mLocationClient;
    private LatLng myCurrentLocation = null; //用于存储当前经纬度

    //权限请求启动器
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // 1. 获取权限结果
                Boolean fineGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                // 2. 判空处理 (兼容写法)
                boolean isFineLocation = fineGranted != null && fineGranted;
                boolean isCoarseLocation = coarseGranted != null && coarseGranted;

                // 3. 判断逻辑
                if (isFineLocation || isCoarseLocation) {
                    startLocation(); // 权限获取成功，开始定位
                } else {
                    Toast.makeText(getContext(), "无法获取位置，列表将按默认顺序显示", Toast.LENGTH_SHORT).show();
                    loadImages(); // 即使没有权限，也加载数据
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化定位客户端
        initLocation();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        checkPermissionsAndLoad();
        loadImages();

        return view;
    }

    // [新增] 检查权限
    private void checkPermissionsAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    //初始化定位配置
    private void initLocation() {
        try {
            //确保合规
            AMapLocationClient.updatePrivacyShow(requireContext(), true, true);
            AMapLocationClient.updatePrivacyAgree(requireContext(), true);

            mLocationClient = new AMapLocationClient(requireContext());
            AMapLocationClientOption option = new AMapLocationClientOption();

            //设置为高精度定位模式
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取一次定位结果
            option.setOnceLocation(true);

            mLocationClient.setLocationOption(option);
            mLocationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                        //定位成功
                        myCurrentLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        Log.d("HomeFragment", "定位成功: " + myCurrentLocation.toString());
                    } else {
                        Log.e("HomeFragment", "定位失败: " + (aMapLocation != null ? aMapLocation.getErrorInfo() : "未知错误"));
                    }
                    //无论定位是否成功，都开始加载网络数据
                    loadImages();
                }
            });
        } catch (Exception e) {
            Log.e("HomeFragment", "定位初始化失败", e);
            loadImages(); // 兜底
        }
    }

    //开始定位
    private void startLocation() {
        if (mLocationClient != null) {
            mLocationClient.startLocation();
        } else {
            loadImages();
        }
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

                        //调用排序方法
                        if (myCurrentLocation != null && newImages != null && !newImages.isEmpty()) {
                            sortImagesByDistance(newImages);
                        }
                        
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
                            // 注意：如果测试数据 description 为 "avatar"，会被此逻辑过滤掉
                            // 如需测试，请暂时注释掉下面的过滤逻辑
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

    //距离排序核心算法
    private void sortImagesByDistance(List<ImageItemDTO> images) {
        try {
            Collections.sort(images, new Comparator<ImageItemDTO>() {
                @Override
                public int compare(ImageItemDTO o1, ImageItemDTO o2) {
                    // 1. 检查是否有坐标
                    boolean hasLoc1 = o1.getGpsLat() != null && o1.getGpsLng() != null;
                    boolean hasLoc2 = o2.getGpsLat() != null && o2.getGpsLng() != null;

                    // 2. 没坐标的放最后
                    if (hasLoc1 && !hasLoc2) return -1; // o1 有坐标，排前面
                    if (!hasLoc1 && hasLoc2) return 1;  // o2 有坐标，排前面
                    if (!hasLoc1 && !hasLoc2) return 0; // 都没坐标，保持原样

                    // 3. 计算距离 (使用高德自带的 AMapUtils)
                    LatLng loc1 = new LatLng(o1.getGpsLat(), o1.getGpsLng());
                    LatLng loc2 = new LatLng(o2.getGpsLat(), o2.getGpsLng());

                    float distance1 = AMapUtils.calculateLineDistance(myCurrentLocation, loc1);
                    float distance2 = AMapUtils.calculateLineDistance(myCurrentLocation, loc2);

                    // 4. 按距离升序排列 (近的在前)
                    return Float.compare(distance1, distance2);
                }
            });
            Log.d("HomeFragment", "已按距离排序完成");
        } catch (Exception e) {
            Log.e("HomeFragment", "排序出错", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //销毁定位客户端
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
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