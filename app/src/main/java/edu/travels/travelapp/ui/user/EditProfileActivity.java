package edu.travels.travelapp.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.travels.travelapp.R;
import edu.travels.travelapp.di.ApiService;
import edu.travels.travelapp.di.RetrofitClient;
import edu.travels.travelapp.model.dto.UserUpdateDTO;
import edu.travels.travelapp.model.vo.ResultVO;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private static final String KEY_SIGNATURE = "signature";

    private CircleImageView ivAvatar;
    private TextInputEditText etNickname;
    private TextInputEditText etPassword;

    private TextInputEditText etSignature;
    private Button btnSave;

    private Uri selectedImageUri;
    private String currentAvatarUrl;
    private String currentSignature;//当前头像的网络URL（用于显示）



    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this).load(selectedImageUri).into(ivAvatar);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupToolbar();
        setupListeners();
        loadCurrentUserInfo(); //自动填充当前昵称和头像
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        etSignature = findViewById(R.id.et_signature);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> submitUpdate());
    }

    /**
     * 从 SharedPreferences 加载当前用户信息
     * 昵称自动填充，密码框留空，头像显示当前网络头像
     */
    private void loadCurrentUserInfo() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        //获取当前昵称（如果没有则为空）
        String nickname = prefs.getString(KEY_NICKNAME, "");
        currentSignature = prefs.getString(KEY_SIGNATURE, "");
        currentAvatarUrl = prefs.getString(KEY_AVATAR_URL, "");

        //昵称填充
        if (!TextUtils.isEmpty(nickname)) {
            etNickname.setText(nickname);
        }

        //填充个性签名
        if (!TextUtils.isEmpty(currentSignature)) {
            etSignature.setText(currentSignature);
        } else {
            etSignature.setText("");  //留空方便用户输入
        }

        //密码框始终留空（表示不修改密码）
        etPassword.setText("");

        //加载头像（优先网络头像，如果没有就用默认）
        if (!TextUtils.isEmpty(currentAvatarUrl)) {
            Glide.with(this)
                    .load(currentAvatarUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(ivAvatar);
        }
    }

    private void submitUpdate() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String nickname = etNickname.getText() != null ? etNickname.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String signature = etSignature.getText() != null ? etSignature.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        //如果用户选择了新头像，先上传头像
        if (selectedImageUri != null) {
            uploadAvatarAndUpdate(nickname, password, signature);
        } else {
            //没有选择新头像，直接更新用户信息
            updateUserInfo(nickname, password, signature, null);
        }
    }

    /**
     * 上传头像并更新用户信息
     * 使用图片上传接口上传头像，然后通过图片列表获取URL
     */
    private void uploadAvatarAndUpdate(String nickname, String password, String signature) {
        try {
            //将 Uri 转换为 File
            File avatarFile = copyUriToCache(selectedImageUri);
            if (avatarFile == null) {
                Toast.makeText(this, "读取图片失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }

            //检查文件大小（10MB限制，使用图片上传接口的限制）
            long fileSize = avatarFile.length();
            Log.d("EditProfile", "头像文件信息: 路径=" + avatarFile.getAbsolutePath() 
                + ", 大小=" + (fileSize / 1024) + "KB, 存在=" + avatarFile.exists());
            
            if (fileSize > 10 * 1024 * 1024) {
                Toast.makeText(this, "头像大小不能超过10MB，请选择较小的图片", Toast.LENGTH_LONG).show();
                return;
            }

            //检查文件类型
            String fileName = avatarFile.getName().toLowerCase();
            if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") 
                && !fileName.endsWith(".png") && !fileName.endsWith(".gif") 
                && !fileName.endsWith(".webp")) {
                Log.w("EditProfile", "文件类型可能不支持: " + fileName);
            }

            //创建 RequestBody
            String mimeType = "image/jpeg";
            if (fileName.endsWith(".png")) {
                mimeType = "image/png";
            } else if (fileName.endsWith(".gif")) {
                mimeType = "image/gif";
            } else if (fileName.endsWith(".webp")) {
                mimeType = "image/webp";
            }
            
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(mimeType),
                    avatarFile
            );
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", avatarFile.getName(), requestFile);

            //使用特殊标记 "avatar" 作为描述，以便在首页过滤掉头像图片
            RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "avatar");
            RequestBody gpsLat = null;
            RequestBody gpsLng = null;
            RequestBody locationName = null;

            Log.d("EditProfile", "开始上传头像（使用图片上传接口），文件大小: " + (fileSize / 1024) + "KB, MIME类型: " + mimeType);

            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            //使用图片上传接口上传头像
            Call<ResultVO<String>> uploadCall = apiService.uploadImage(filePart, description, gpsLat, gpsLng, locationName);

            uploadCall.enqueue(new Callback<ResultVO<String>>() {
                @Override
                public void onResponse(Call<ResultVO<String>> call, Response<ResultVO<String>> response) {
                    Log.d("EditProfile", "头像上传响应: isSuccessful=" + response.isSuccessful() 
                        + ", code=" + response.code() + ", body=" + (response.body() != null));
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ResultVO<String> result = response.body();
                        Log.d("EditProfile", "ResultVO解析: code=" + result.getCode() 
                            + ", msg=" + result.getMsg() + ", data=" + result.getData());
                        
                        if (result.isSuccess() && result.getData() != null) {
                            //图片上传成功，获取图片ID
                            String imageId = result.getData();
                            Log.d("EditProfile", "头像上传成功，图片ID: " + imageId);
                            
                            //通过图片ID获取图片URL
                            getImageUrlByImageId(imageId, nickname, password, signature);
                        } else {
                            String errorMsg = result.getMsg() != null ? result.getMsg() : "头像上传失败";
                            
                            //特殊处理不同错误码
                            if (result.getCode() == 401) {
                                errorMsg = "登录已过期，请重新登录后再试";
                            } else if (result.getCode() == 400) {
                                //参数错误，可能是文件格式或大小问题
                                if (errorMsg.contains("大小") || errorMsg.contains("size")) {
                                    errorMsg = "头像大小不符合要求，请选择小于5MB的图片";
                                } else if (errorMsg.contains("格式") || errorMsg.contains("format")) {
                                    errorMsg = "头像格式不支持，请选择jpg、png、gif或webp格式";
                                }
                            } else if (result.getCode() == 500) {
                                //服务器内部错误，可能是OSS配置问题或接口未实现
                                if (errorMsg.contains("bucket") || errorMsg.contains("OSS") || errorMsg.contains("存储")) {
                                    errorMsg = "服务器存储配置错误，请联系管理员";
                                } else if (errorMsg.contains("No static resource") || errorMsg.contains("NoResourceFound")) {
                                    errorMsg = "头像上传接口未实现，请联系后端开发人员";
                                } else {
                                    errorMsg = "服务器内部错误，请稍后重试";
                                }
                            }
                            
                            Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            Log.e("EditProfile", "头像上传失败 - code: " + result.getCode() + ", msg: " + errorMsg);
                        }
                    } else {
                        String errorMsg = "头像上传失败";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("EditProfile", "错误响应体: " + errorBody);
                                
                                //检查是否是路由未找到的错误
                                if (errorBody.contains("No static resource") || errorBody.contains("NoResourceFound") 
                                    || errorBody.contains("auth/upload-avatar")) {
                                    errorMsg = "头像上传接口未实现\n请确认后端已实现 /api/auth/upload-avatar 接口";
                                } else {
                                    errorMsg = "上传失败: HTTP " + response.code();
                                }
                            } else {
                                errorMsg = "上传失败: HTTP " + response.code();
                            }
                        } catch (Exception e) {
                            Log.e("EditProfile", "解析错误响应失败", e);
                            errorMsg = "上传失败: HTTP " + response.code();
                        }
                        Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("EditProfile", "头像上传失败 - HTTP状态码: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResultVO<String>> call, Throwable t) {
                    Log.e("EditProfile", "头像上传网络请求失败", t);
                    Toast.makeText(EditProfileActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("EditProfile", "上传头像时发生异常", e);
            Toast.makeText(this, "上传头像失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新用户信息
     */
    private void updateUserInfo(String nickname, String password, String signature, String avatarUrl) {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickname(nickname);
        dto.setPassword(password.isEmpty() ? null : password);  // 密码为空时传null
        dto.setSignature(signature.isEmpty() ? null : signature);  // 签名为空时传null
        dto.setAvatarUrl(avatarUrl);  // 传null表示不修改头像

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        Call<ResponseBody> call = apiService.updateUserInfo(dto);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("EditProfile", "API响应状态码: " + response.code());
                Log.d("EditProfile", "API响应消息: " + response.message());
                
                if (response.isSuccessful()) {
                    Log.d("EditProfile", "用户信息更新成功");
                    
                    String finalNickname = etNickname.getText().toString().trim();
                    String finalSignature = etSignature.getText() != null
                            ? etSignature.getText().toString().trim()
                            : "";

                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_NICKNAME, finalNickname);
                    editor.putString(KEY_SIGNATURE, finalSignature);
                    // 如果更新了头像，保存新的头像URL
                    if (avatarUrl != null) {
                        editor.putString(KEY_AVATAR_URL, avatarUrl);
                    }
                    editor.apply();

                    Toast.makeText(EditProfileActivity.this, "资料更新成功！", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMsg = "更新失败";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("EditProfile", "API错误响应: " + errorBody);
                            
                            // 尝试解析为ResultVO格式
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                ResultVO<?> errorResult = gson.fromJson(errorBody, ResultVO.class);
                                if (errorResult != null) {
                                    errorMsg = errorResult.getMsg() != null ? errorResult.getMsg() : errorMsg;
                                    
                                    //特殊处理常见错误
                                    if (errorResult.getCode() == 401) {
                                        errorMsg = "登录已过期，请重新登录后再试";
                                    } else if (errorResult.getCode() == 500) {
                                        if (errorMsg.contains("user") && errorMsg.contains("null")) {
                                            errorMsg = "无法获取用户信息，请重新登录后再试";
                                        } else {
                                            errorMsg = "服务器内部错误，请稍后重试";
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                //如果不是JSON格式，直接使用原始错误信息
                                if (errorBody.length() < 200) {
                                    errorMsg = errorBody;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("EditProfile", "解析错误响应失败", e);
                    }
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("EditProfile", "网络请求失败", t);
                Toast.makeText(EditProfileActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 将 Uri 复制到缓存目录，避免 Scoped Storage 问题
     */
    private File copyUriToCache(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File cacheDir = new File(getCacheDir(), "avatar_temp");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, "avatar_" + System.currentTimeMillis() + ".jpg");

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
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过图片ID获取图片URL，然后更新用户信息
     * 优化：多页查找，确保找到刚上传的图片
     */
    private void getImageUrlByImageId(String imageId, String nickname, String password, String signature) {
        Log.d("EditProfile", "开始查找图片URL，imageId: " + imageId);
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        
        // 先查找第一页（新上传的图片通常在前面）
        findImageUrlInPage(apiService, imageId, nickname, password, signature, 1, 1);
    }
    
    /**
     * 在指定页查找图片URL
     */
    private void findImageUrlInPage(ApiService apiService, String imageId, String nickname, 
                                    String password, String signature, int pageNum, int maxPages) {
        Log.d("EditProfile", "在第" + pageNum + "页查找图片，imageId: " + imageId);
        
        Call<edu.travels.travelapp.model.vo.ResultVO<java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO>>> call = 
            apiService.getImageList(pageNum, 50); //每页50条
        
        call.enqueue(new retrofit2.Callback<edu.travels.travelapp.model.vo.ResultVO<java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO>>>() {
            @Override
            public void onResponse(
                retrofit2.Call<edu.travels.travelapp.model.vo.ResultVO<java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO>>> call,
                retrofit2.Response<edu.travels.travelapp.model.vo.ResultVO<java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO>>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    edu.travels.travelapp.model.vo.ResultVO<java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO>> result = response.body();
                    
                    if (result.isSuccess() && result.getData() != null) {
                        // 后端直接返回 List 数组
                        java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO> imageList = result.getData();
                        
                        Log.d("EditProfile", "第" + pageNum + "页有" + (imageList != null ? imageList.size() : 0) + "张图片");
                        
                        // 查找匹配的图片ID
                        String avatarUrl = null;
                        if (imageList != null) {
                            for (edu.travels.travelapp.model.dto.ImageItemDTO item : imageList) {
                                Log.d("EditProfile", "检查图片: imageId=" + item.getImageId() + ", url=" + item.getImageUrl());
                                if (imageId.equals(item.getImageId())) {
                                    avatarUrl = item.getImageUrl();
                                    Log.d("EditProfile", "✓ 找到匹配的图片URL: " + avatarUrl);
                                    break;
                                }
                            }
                        }
                        
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            //找到图片URL，更新用户信息
                            Log.d("EditProfile", "找到头像URL: " + avatarUrl + "，开始更新用户信息");
                            updateUserInfo(nickname, password, signature, avatarUrl);
                        } else {
                            //当前页没找到，根据返回的列表大小判断是否还有下一页
                            boolean hasMore = imageList != null && imageList.size() >= 50;
                            if (hasMore && pageNum < maxPages) {
                                //继续查找下一页
                                Log.d("EditProfile", "当前页未找到，继续查找第" + (pageNum + 1) + "页");
                                findImageUrlInPage(apiService, imageId, nickname, password, signature, pageNum + 1, maxPages);
                            } else {
                                Log.e("EditProfile", "未找到图片URL，imageId: " + imageId + "，已查找" + pageNum + "页");
                                Toast.makeText(EditProfileActivity.this, "上传成功，但获取图片URL失败，请稍后重试", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Log.e("EditProfile", "获取图片列表失败: " + (result.getMsg() != null ? result.getMsg() : "未知错误"));
                        Toast.makeText(EditProfileActivity.this, "获取图片信息失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("EditProfile", "获取图片列表HTTP错误: " + response.code());
                    Toast.makeText(EditProfileActivity.this, "获取图片信息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                retrofit2.Call<edu.travels.travelapp.model.vo.ResultVO<java.util.List<edu.travels.travelapp.model.dto.ImageItemDTO>>> call,
                Throwable t) {
                Log.e("EditProfile", "获取图片列表网络请求失败", t);
                Toast.makeText(EditProfileActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}