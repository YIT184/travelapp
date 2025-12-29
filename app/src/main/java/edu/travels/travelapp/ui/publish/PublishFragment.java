package edu.travels.travelapp.ui.publish;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.MapsInitializer;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import edu.travels.travelapp.R;
import edu.travels.travelapp.di.ApiService;
import edu.travels.travelapp.di.RetrofitClient;
import edu.travels.travelapp.model.vo.ResultVO;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishFragment extends DialogFragment {

    public interface OnImageUploadListener {
        void onImageUploadSuccess();
    }

    private OnImageUploadListener uploadListener;

    public void setOnImageUploadListener(OnImageUploadListener listener) {
        this.uploadListener = listener;
    }

    private ImageView ivSelectedImage;
    private TextInputEditText etTitle;
    private TextInputEditText etLocation;
    private TextInputEditText etDescription;
    private TextView tvAuthor;
    private Button btnSubmit;
    private Button btnSelectImage;
    private ImageView ivUserAvatar;
    private View layoutImagePlaceholder;

    private Uri selectedImageUri;
    private File selectedImageFile;
    
    // 定位相关
    private AMapLocationClient locationClient;
    private Double currentLat;
    private Double currentLng;
    private String currentLocationName;
    private boolean isLocationRequested = false;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (getActivity() != null && result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null && ivSelectedImage != null) {
                        // 显示选中的图片
                        Glide.with(this)
                                .load(selectedImageUri)
                                .into(ivSelectedImage);
                        
                        // 隐藏占位符和选择按钮
                        if (layoutImagePlaceholder != null) {
                            layoutImagePlaceholder.setVisibility(View.GONE);
                        }
                        if (btnSelectImage != null) {
                            btnSelectImage.setVisibility(View.GONE);
                        }
                        
                        // 将图片复制到缓存文件
                        selectedImageFile = copyUriToCache(selectedImageUri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 初始化高德地图隐私合规
        try {
            MapsInitializer.updatePrivacyShow(requireContext(), true, true);
            MapsInitializer.updatePrivacyAgree(requireContext(), true);
        } catch (Exception e) {
            Log.e("PublishFragment", "高德地图隐私合规初始化失败", e);
        }
        
        View view = inflater.inflate(R.layout.publish, container, false);

        initViews(view);
        setupListeners();
        initLocation(); // 初始化定位

        return view;
    }

    private void initViews(View view) {
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        etTitle = view.findViewById(R.id.et_title);
        etLocation = view.findViewById(R.id.et_location);
        etDescription = view.findViewById(R.id.et_description);
        tvAuthor = view.findViewById(R.id.tv_author);
        ivUserAvatar = view.findViewById(R.id.iv_user_avatar);
        btnSubmit = view.findViewById(R.id.btn_submit);
        layoutImagePlaceholder = view.findViewById(R.id.layout_image_placeholder);
        
        // 设置作者信息 - 从SharedPreferences获取当前用户的昵称和头像
        loadCurrentUserInfo();
    }
    
    /**
     * 加载当前用户信息（昵称和头像）
     */
    private void loadCurrentUserInfo() {
        if (getContext() == null) return;
        
        try {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
            
            // 获取昵称
            String nickname = prefs.getString("nickname", null);
            if (nickname != null && !nickname.trim().isEmpty()) {
                tvAuthor.setText(nickname);
            } else {
                tvAuthor.setText("当前用户");
            }
            
            // 获取头像
            String avatarUrl = prefs.getString("avatar_url", null);
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(ivUserAvatar);
            } else {
                // 使用默认头像
                Glide.with(this)
                        .load(R.drawable.default_avatar)
                        .circleCrop()
                        .into(ivUserAvatar);
            }
        } catch (Exception e) {
            Log.e("PublishFragment", "加载用户信息失败", e);
            tvAuthor.setText("当前用户");
            Glide.with(this)
                    .load(R.drawable.default_avatar)
                    .circleCrop()
                    .into(ivUserAvatar);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次显示时重新加载用户信息，确保显示最新的头像和昵称
        loadCurrentUserInfo();
        // 自动获取定位
        if (!isLocationRequested) {
            requestLocation();
            isLocationRequested = true;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // 停止定位以节省资源
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放定位资源
        if (locationClient != null) {
            locationClient.onDestroy();
            locationClient = null;
        }
    }

    private void setupListeners() {
        // 图片选择点击事件
        ivSelectedImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
        
        // 图片选择按钮点击事件
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // 发布按钮点击事件
        btnSubmit.setOnClickListener(v -> uploadImage());
        
        // 定位输入框点击事件 - 可以手动触发重新定位
        etLocation.setOnClickListener(v -> {
            if (etLocation.getText() == null || etLocation.getText().toString().trim().isEmpty()) {
                requestLocation();
            }
        });
    }
    
    /**
     * 初始化定位服务
     */
    private void initLocation() {
        try {
            if (getActivity() == null) return;
            
            locationClient = new AMapLocationClient(getActivity().getApplicationContext());
            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation location) {
                    if (location != null && location.getErrorCode() == 0) {
                        // 定位成功
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        
                        // 获取地址信息
                        String address = location.getAddress();
                        String poiName = location.getPoiName();
                        String district = location.getDistrict();
                        String city = location.getCity();
                        
                        // 优先使用POI名称，其次使用地址，最后使用区+市
                        if (poiName != null && !poiName.trim().isEmpty()) {
                            currentLocationName = poiName;
                        } else if (address != null && !address.trim().isEmpty()) {
                            currentLocationName = address;
                        } else if (district != null && city != null) {
                            currentLocationName = district + ", " + city;
                        } else {
                            currentLocationName = city != null ? city : "未知位置";
                        }
                        
                        // 自动填充到输入框
                        if (etLocation != null) {
                            etLocation.setText(currentLocationName);
                        }
                        
                        Log.d("PublishFragment", "定位成功: " + currentLocationName + " (" + currentLat + ", " + currentLng + ")");
                    } else {
                        // 定位失败
                        String errorInfo = location != null ? location.getErrorInfo() : "定位失败";
                        Log.e("PublishFragment", "定位失败: " + (location != null ? location.getErrorCode() : "未知") + " - " + errorInfo);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "定位失败: " + errorInfo, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            
            // 配置定位选项
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy); // 高精度模式
            option.setOnceLocation(true); // 只定位一次
            option.setNeedAddress(true); // 需要地址信息
            // 注意：setNeedPoi方法可能不存在，使用setOnceLocationLatest(true)来获取最新位置
            option.setOnceLocationLatest(true); // 获取最新3秒内的缓存位置
            locationClient.setLocationOption(option);
            
        } catch (Exception e) {
            Log.e("PublishFragment", "初始化定位服务失败", e);
        }
    }
    
    /**
     * 请求定位
     */
    private void requestLocation() {
        if (locationClient == null) {
            Log.e("PublishFragment", "定位服务未初始化");
            return;
        }
        
        if (getContext() == null) return;
        
        // 检查定位权限
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 请求定位权限
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1001);
            return;
        }
        
        // 开始定位
        try {
            locationClient.startLocation();
            if (getContext() != null) {
                Toast.makeText(getContext(), "正在获取位置...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PublishFragment", "启动定位失败", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "定位服务启动失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予，开始定位
                requestLocation();
            } else {
                // 权限被拒绝
                if (getContext() != null) {
                    Toast.makeText(getContext(), "需要定位权限才能自动获取位置", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void uploadImage() {
        // 验证必填字段
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        
        if (selectedImageFile == null) {
            Toast.makeText(getContext(), "请选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "请输入景点标题", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取可选字段
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        // 如果输入框为空但已获取到定位信息，使用定位信息
        if (location.isEmpty() && currentLocationName != null && !currentLocationName.trim().isEmpty()) {
            location = currentLocationName;
        }
        
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        
        // 如果有详细描述，将标题和描述合并
        String fullDescription = title;
        if (!description.isEmpty()) {
            fullDescription = title + ". " + description;
        }

        // 创建文件部分
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), selectedImageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", selectedImageFile.getName(), requestFile);

        // 创建其他参数
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), fullDescription != null ? fullDescription : "");
        RequestBody locationBody = RequestBody.create(MediaType.parse("text/plain"), location != null ? location : "");
        
        // 使用获取到的GPS坐标
        RequestBody gpsLatBody = null;
        RequestBody gpsLngBody = null;
        if (currentLat != null && currentLng != null) {
            gpsLatBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLat));
            gpsLngBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLng));
            Log.d("PublishFragment", "上传GPS坐标: (" + currentLat + ", " + currentLng + ")");
        }

        // 调用API
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<ResultVO<String>> call = apiService.uploadImage(
                filePart,
                descriptionBody,
                gpsLatBody,
                gpsLngBody,
                locationBody
        );

        call.enqueue(new Callback<ResultVO<String>>() {
            @Override
            public void onResponse(Call<ResultVO<String>> call, Response<ResultVO<String>> response) {
                Log.e("PublishFragment", "响应状态码: " + response.code()); // 使用E级别确保显示
                Log.e("PublishFragment", "response.isSuccessful(): " + response.isSuccessful());
                Log.e("PublishFragment", "response.body() == null: " + (response.body() == null));
                
                if (response.isSuccessful()) {
                    ResultVO<String> resultVO = response.body();
                    if (resultVO != null) {
                        Log.e("PublishFragment", "ResultVO解析成功 - code: " + resultVO.getCode() + ", msg: " + resultVO.getMsg() + ", data: " + resultVO.getData()); // 使用E级别确保显示
                        
                        if (resultVO.isSuccess()) {
                            String imageId = resultVO.getData();
                            Log.d("PublishFragment", "上传成功，图片ID: " + imageId);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), resultVO.getMsg() != null ? resultVO.getMsg() : "图片上传成功！", Toast.LENGTH_LONG).show();
                            }
                            
                            // 通知监听器上传成功
                            if (uploadListener != null) {
                                uploadListener.onImageUploadSuccess();
                            }
                            
                            dismiss();
                        } else {
                            String errorMsg = resultVO.getMsg() != null ? resultVO.getMsg() : "上传失败";
                            Log.e("PublishFragment", "上传失败 - code: " + resultVO.getCode() + ", msg: " + errorMsg);
                            
                            // 特殊处理401错误（token失效）
                            if (resultVO.getCode() == 401) {
                                errorMsg = "登录已过期，请重新登录后再试";
                                Log.e("PublishFragment", "Token失效，提示用户重新登录");
                            } 
                            // 特殊处理OSS相关错误（500错误，但包含OSS错误信息）
                            else if (resultVO.getCode() == 500 && errorMsg != null) {
                                if (errorMsg.contains("文件上传到存储服务失败") || errorMsg.contains("NoSuchBucket") || errorMsg.contains("OSS")) {
                                    errorMsg = "服务器存储配置错误，请联系管理员";
                                    Log.e("PublishFragment", "OSS配置错误，提示用户联系管理员");
                                } else if (errorMsg.contains("服务器内部错误")) {
                                    errorMsg = "服务器内部错误，请稍后重试";
                                }
                            }
                            
                            if (getContext() != null) {
                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // response.body()为null，说明Gson解析失败，尝试读取原始响应
                        Log.e("PublishFragment", "response.body()为null，Gson解析可能失败");
                        String errorMsg = "服务器响应格式错误";
                        try {
                            // 尝试读取原始响应体
                            okhttp3.ResponseBody rawBody = response.raw().body();
                            if (rawBody != null) {
                                String rawBodyStr = rawBody.string();
                                Log.e("PublishFragment", "原始响应体: " + rawBodyStr);
                                errorMsg = "解析失败，原始响应: " + (rawBodyStr.length() > 200 ? rawBodyStr.substring(0, 200) + "..." : rawBodyStr);
                            } else {
                                Log.e("PublishFragment", "原始响应体也为null");
                            }
                        } catch (Exception e) {
                            Log.e("PublishFragment", "读取原始响应体失败", e);
                            errorMsg = "解析响应失败: " + e.getMessage();
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    String errorMsg = "HTTP错误: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyStr = response.errorBody().string();
                            Log.e("PublishFragment", "HTTP错误响应: " + errorBodyStr);
                            errorMsg = errorBodyStr;
                        }
                    } catch (Exception e) {
                        Log.e("PublishFragment", "解析错误响应失败", e);
                    }
                    if (getContext() != null) {
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultVO<String>> call, Throwable t) {
                Log.e("PublishFragment", "网络请求失败", t);
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 将 Uri 复制到缓存目录，避免 Scoped Storage 问题
     */
    private File copyUriToCache(Uri uri) {
        try {
            if (getActivity() == null) {
                Log.e("PublishFragment", "Activity为null，无法复制文件");
                return null;
            }
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File cacheDir = new File(getActivity().getCacheDir(), "upload_temp");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, "upload_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return file;
        } catch (Exception e) {
            Log.e("PublishFragment", "复制文件失败", e);
            return null;
        }
    }
}