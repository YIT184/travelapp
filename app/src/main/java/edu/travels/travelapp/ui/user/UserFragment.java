package edu.travels.travelapp.ui.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.travels.travelapp.R;
import edu.travels.travelapp.di.ApiService;
import edu.travels.travelapp.di.RetrofitClient;
import edu.travels.travelapp.model.dto.UserLoginDTO;
import edu.travels.travelapp.model.dto.UserRegisterDTO;
import edu.travels.travelapp.model.dto.UserUpdateDTO;
import edu.travels.travelapp.model.vo.ResultVO;
import android.util.Log;


import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class UserFragment extends Fragment {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private LinearLayout layoutLogin, layoutProfile;
    private TextView tvNickname;
    private Button btnLogout;

    private ApiService apiService;
    private SharedPreferences sp;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_USER_ID = "user_id";
    private TextView tvSignature;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        etPhone = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        layoutLogin = view.findViewById(R.id.layout_login);
        layoutProfile = view.findViewById(R.id.layout_profile);
        tvNickname = view.findViewById(R.id.tv_username_display);
        tvSignature = view.findViewById(R.id.tv_signature);
        btnLogout = view.findViewById(R.id.btn_logout);

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        sp = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        checkLoginStatus(view);

        btnLogin.setOnClickListener(v -> loginOrRegister());
        btnLogout.setOnClickListener(v -> logout());

        // 编辑资料
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EditProfileActivity.class));
        });
        
        // 文章管理
        view.findViewById(R.id.btn_my_posts).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), edu.travels.travelapp.ui.user.myposts.MyPostsActivity.class));
        });


        checkLoginStatus(view);

        return view;
    }

    private void loginOrRegister() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "请输入手机号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            Toast.makeText(getContext(), "手机号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        UserLoginDTO loginDTO = new UserLoginDTO(phone, password);
        apiService.login(loginDTO).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        if (obj.getInt("code") == 200) {
                            JSONObject data = obj.getJSONObject("data");
                            String token = data.getString("token");
                            String nickname = data.optString("nickname", "游客" + phone.substring(7));
                            String userId = data.optString("userId", "");

                            // 1. 先保存 Token (非常重要，后续请求需要它)
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", token);
                            editor.apply();

                            // 2. 尝试解析头像（签名从 getUserInfo 接口获取）
                            String loginAvatar = data.optString("avatarUrl", null);
                            if (loginAvatar == null) loginAvatar = data.optString("avatar", null); // 防御性代码

                            // ✅ 关键修改：个性签名总是从 getUserInfo 接口获取，而不是登录接口
                            // 如果登录接口返回了头像，可以使用；但签名必须从 getUserInfo 获取
                            Log.d("UserFragment", "========== 准备调用 fetchUserInfoAndSave ==========");
                            Log.d("UserFragment", "登录返回的头像: " + loginAvatar);
                            if (loginAvatar != null && !loginAvatar.isEmpty()) {
                                // 有头像，但签名需要从 getUserInfo 获取
                                Log.d("UserFragment", "✅ 登录接口返回了头像，签名将从 getUserInfo 获取");
                                fetchUserInfoAndSave(token, nickname, userId, null, loginAvatar);
                            } else {
                                // 没有头像，也需要从 getUserInfo 获取完整信息
                                Log.d("UserFragment", "✅ 登录接口数据不全，从 getUserInfo 获取完整信息");
                                fetchUserInfoAndSave(token, nickname, userId, null, null);
                            }
                            return;
                        }
                    }
                } catch (Exception e) {
                    // 解析失败或登录失败，都尝试注册
                }
                // 登录失败 → 自动注册
                autoRegister(phone, password);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void autoRegister(String phone, String password) {
        String defaultNickname = "游客" + phone.substring(7);
        UserRegisterDTO dto = new UserRegisterDTO(phone, password, defaultNickname);

        apiService.register(dto).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "注册成功，正在登录...", Toast.LENGTH_SHORT).show();
                    // 注册成功后延迟一下再登录（避免太快）
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        UserLoginDTO loginDTO = new UserLoginDTO(phone, password);
                        apiService.login(loginDTO).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String json = response.body().string();
                                        JSONObject obj = new JSONObject(json);
                                        if (obj.getInt("code") == 200) {
                                            JSONObject data = obj.getJSONObject("data");
                                            String token = data.getString("token");
                                            String nick = data.optString("nickname", defaultNickname);
                                            String userId = data.optString("userId", "");

                                            // 1. 先保存 Token
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("token", token);
                                            editor.apply();

                                            // 2. 尝试解析头像（签名从 getUserInfo 接口获取）
                                            String loginAvatar = data.optString("avatarUrl", null);
                                            if (loginAvatar == null) loginAvatar = data.optString("avatar", null); // 防御性代码

                                            // ✅ 关键修改：个性签名总是从 getUserInfo 接口获取，而不是登录接口
                                            Log.d("UserFragment", "========== 准备调用 fetchUserInfoAndSave (注册后) ==========");
                                            Log.d("UserFragment", "注册后登录返回的头像: " + loginAvatar);
                                            if (loginAvatar != null && !loginAvatar.isEmpty()) {
                                                // 有头像，但签名需要从 getUserInfo 获取
                                                Log.d("UserFragment", "✅ 注册后登录返回了头像，签名将从 getUserInfo 获取");
                                                fetchUserInfoAndSave(token, nick, userId, null, loginAvatar);
                                            } else {
                                                // 没有头像，也需要从 getUserInfo 获取完整信息
                                                Log.d("UserFragment", "✅ 注册后登录数据不全，从 getUserInfo 获取完整信息");
                                                fetchUserInfoAndSave(token, nick, userId, null, null);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "登录异常，但已注册成功", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {}
                        });
                    }, 600);
                } else {
                    Toast.makeText(getContext(), "注册失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "注册失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 保存登录成功后的用户信息
     * @param token 用户token
     * @param nickname 用户昵称
     * @param userId 用户ID
     * @param signature 个性签名（可能为null或空字符串）
     * @param avatarUrl 头像URL（可能为null或空字符串）
     */
    private void saveLoginSuccess(String token, String nickname, String userId, String signature, String avatarUrl) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", token);
        editor.putString("nickname", nickname);
        
        // ✅ 关键修复：只有当传入的签名不为空时，才覆盖本地签名；否则保留本地已有的签名
        String finalSignature = signature;
        if (signature != null && !signature.isEmpty()) {
            editor.putString("signature", signature);
        } else {
            // 如果传入的签名为空，尝试保留本地已有的签名
            String existingSignature = sp.getString(KEY_SIGNATURE, "");
            if (!existingSignature.isEmpty()) {
                finalSignature = existingSignature; // 用于UI显示和保存
                editor.putString("signature", existingSignature); // 保存本地已有的签名
            } else {
                // 如果本地也没有签名，保存空字符串
        editor.putString("signature", "");
            }
        }
        
        // ✅ 关键修复：只有当传入的头像不为空时，才覆盖本地头像；否则保留本地已有的头像
        String finalAvatar = avatarUrl;
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            editor.putString("avatar_url", avatarUrl);
        } else {
            // 如果传入的头像为空，尝试保留本地已有的头像
            String existingAvatar = sp.getString(KEY_AVATAR_URL, "");
            if (!existingAvatar.isEmpty()) {
                finalAvatar = existingAvatar; // 用于UI显示和保存
                editor.putString("avatar_url", existingAvatar); // 保存本地已有的头像
            } else {
                // 如果本地也没有头像，保存空字符串
        editor.putString("avatar_url", "");
            }
        }
        
        if (userId != null) {
            editor.putString(KEY_USER_ID, userId);
        }
        editor.apply();

        // 更新UI显示
        tvNickname.setText(nickname);
        
        // 显示签名（使用最终确定的值）
        if (!TextUtils.isEmpty(finalSignature)) {
            tvSignature.setText(finalSignature);
        } else {
            tvSignature.setText("这个人很懒，没有留下个性签名");
        }
        
        // 显示头像（使用最终确定的值）
        ImageView ivAvatar = getView() != null ? getView().findViewById(R.id.iv_avatar) : null;
        if (ivAvatar != null) {
            if (!TextUtils.isEmpty(finalAvatar)) {
                Glide.with(this).load(finalAvatar).into(ivAvatar);
            } else {
                // 如果没有头像URL，显示默认头像
                ivAvatar.setImageResource(R.drawable.default_avatar);
            }
        }

        layoutLogin.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), "登录成功！欢迎 " + nickname, Toast.LENGTH_LONG).show();
    }


    private void checkLoginStatus(View view) {
        String token = sp.getString(KEY_TOKEN, null);
        String nickname = sp.getString(KEY_NICKNAME, null);
        String signature = sp.getString(KEY_SIGNATURE, "");

        if (token != null && nickname != null) {
            tvNickname.setText(nickname);

            if (!TextUtils.isEmpty(signature)) {
                tvSignature.setText(signature);
            } else {
                tvSignature.setText("这个人很懒，没有留下个性签名");
            }

            layoutLogin.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.VISIBLE);

            // 显示头像（如果有网络 URL）
            String avatarUrl = sp.getString(KEY_AVATAR_URL, "");
            if (!TextUtils.isEmpty(avatarUrl)) {
                ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
                Glide.with(this).load(avatarUrl).into(ivAvatar);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus(getView());  //重新检查并刷新显示
    }

    /**
     * 获取详细用户信息并保存
     * 个性签名总是从 getUserInfo 接口获取，而不是从登录接口
     * @param token 用户token
     * @param nickname 用户昵称
     * @param userId 用户ID
     * @param currentSign 登录接口返回的签名（已废弃，签名总是从 getUserInfo 获取）
     * @param currentAvatar 登录接口返回的头像（如果有值则使用，否则从 getUserInfo 获取）
     */
    private void fetchUserInfoAndSave(String token, String nickname, String userId, String currentSign, String currentAvatar) {
        // 从本地读取已有的签名和头像（作为备用）
        String localSignature = sp.getString(KEY_SIGNATURE, "");
        String localAvatar = sp.getString(KEY_AVATAR_URL, "");
        
        Log.d("UserFragment", "========== 开始调用 getUserInfo ==========");
        Log.d("UserFragment", "当前本地签名: " + localSignature);
        Log.d("UserFragment", "当前本地头像: " + localAvatar);
        
        apiService.getUserInfo().enqueue(new Callback<ResultVO<UserUpdateDTO>>() {
            @Override
            public void onResponse(Call<ResultVO<UserUpdateDTO>> call, Response<ResultVO<UserUpdateDTO>> response) {
                Log.d("UserFragment", "========== getUserInfo 响应收到 ==========");
                Log.d("UserFragment", "响应状态码: " + response.code());
                Log.d("UserFragment", "响应是否成功: " + response.isSuccessful());
                Log.d("UserFragment", "响应体是否为null: " + (response.body() == null));
                
                // 默认使用传入的值（防止接口返回null）
                String finalNick = nickname;
                // 优先使用登录接口返回的头像，如果没有则使用本地已有的头像
                String finalAvatar = currentAvatar != null && !currentAvatar.isEmpty() 
                    ? currentAvatar 
                    : (localAvatar != null && !localAvatar.isEmpty() ? localAvatar : "");
                // ✅ 关键修改：个性签名优先从 getUserInfo 接口获取，而不是登录接口
                // 初始值使用本地已有的签名（作为备用）
                String finalSign = localSignature != null && !localSignature.isEmpty() ? localSignature : "";

                if (response.isSuccessful() && response.body() != null) {
                    ResultVO<UserUpdateDTO> resultVO = response.body();
                    Log.d("UserFragment", "ResultVO code: " + resultVO.getCode());
                    Log.d("UserFragment", "ResultVO msg: " + resultVO.getMsg());
                    Log.d("UserFragment", "ResultVO data是否为null: " + (resultVO.getData() == null));
                    
                    // ✅ 关键修复：检查业务状态码，只有 code == 200 才处理数据
                    if (resultVO.getCode() == 200 && resultVO.getData() != null) {
                        UserUpdateDTO userInfo = resultVO.getData();
                        Log.d("UserFragment", "========== UserUpdateDTO 所有字段 ==========");
                        Log.d("UserFragment", "UserUpdateDTO nickname: " + (userInfo.getNickname() != null ? userInfo.getNickname() : "null"));
                        Log.d("UserFragment", "UserUpdateDTO password: " + (userInfo.getPassword() != null ? "***" : "null"));
                        Log.d("UserFragment", "UserUpdateDTO signature: " + (userInfo.getSignature() != null ? userInfo.getSignature() : "null"));
                        Log.d("UserFragment", "UserUpdateDTO avatarUrl: " + (userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : "null"));
                        Log.d("UserFragment", "UserUpdateDTO signature是否为null: " + (userInfo.getSignature() == null));
                        Log.d("UserFragment", "UserUpdateDTO signature是否为空字符串: " + (userInfo.getSignature() != null && userInfo.getSignature().isEmpty()));
                        Log.d("UserFragment", "UserUpdateDTO signature长度: " + (userInfo.getSignature() != null ? userInfo.getSignature().length() : "N/A"));
                        
                        // 如果获取到了新数据，就覆盖旧的
                        if (userInfo.getNickname() != null && !userInfo.getNickname().isEmpty()) 
                            finalNick = userInfo.getNickname();
                        
                        if (userInfo.getAvatarUrl() != null && !userInfo.getAvatarUrl().isEmpty()) 
                            finalAvatar = userInfo.getAvatarUrl();
                        
                        // ✅ 关键修改：个性签名优先从 getUserInfo 接口获取
                        if (userInfo.getSignature() != null && !userInfo.getSignature().isEmpty()) {
                            finalSign = userInfo.getSignature();
                            Log.d("UserFragment", "✅ 从 getUserInfo 接口获取到签名: " + finalSign);
                        } else {
                            // 如果接口返回的签名为空，使用本地已有的签名（不覆盖）
                            Log.w("UserFragment", "⚠️ getUserInfo 接口返回的签名为空或null，使用本地签名: " + finalSign);
                        }
                    } else {
                        // ✅ 关键修复：业务状态码不是 200 或 data 为 null，视为失败，保留本地签名
                        Log.w("UserFragment", "⚠️ getUserInfo 接口返回业务错误，code: " + resultVO.getCode() + ", msg: " + resultVO.getMsg());
                        Log.w("UserFragment", "⚠️ 将保留本地已有的签名，不覆盖");
                        // finalSign 已经初始化为本地签名，不需要修改
                    }
                    
                    Log.d("UserFragment", "最终保存的数据 - 昵称: " + finalNick + ", 头像: " + finalAvatar + ", 签名: " + finalSign);
                } else {
                    Log.w("UserFragment", "⚠️ 获取用户信息接口返回异常");
                    if (response.body() == null) {
                        Log.w("UserFragment", "响应体为 null");
                    } else {
                        Log.w("UserFragment", "响应不成功，状态码: " + response.code());
                    }
                }

                // 保存最终数据
                Log.d("UserFragment", "========== 调用 saveLoginSuccess ==========");
                saveLoginSuccess(token, finalNick, userId, finalSign, finalAvatar);
            }

            @Override
            public void onFailure(Call<ResultVO<UserUpdateDTO>> call, Throwable t) {
                Log.e("UserFragment", "========== getUserInfo 网络错误 ==========");
                Log.e("UserFragment", "错误信息: " + t.getMessage());
                Log.e("UserFragment", "错误堆栈: ", t);
                
                // ✅ 关键修复：网络失败时，使用本地缓存的数据（不依赖登录接口返回的签名）
                String safeAvatar = currentAvatar != null && !currentAvatar.isEmpty() 
                    ? currentAvatar 
                    : (localAvatar != null && !localAvatar.isEmpty() ? localAvatar : "");
                // 签名优先使用本地已有的签名
                String safeSign = localSignature != null && !localSignature.isEmpty() ? localSignature : "";
                
                Log.d("UserFragment", "getUserInfo 接口失败，使用本地缓存数据，头像: " + safeAvatar + ", 签名: " + safeSign);
                saveLoginSuccess(token, nickname, userId, safeSign, safeAvatar);
            }
        });
    }

    private void logout() {
        sp.edit().clear().apply();  // 彻底清除所有用户信息
        layoutLogin.setVisibility(View.VISIBLE);
        layoutProfile.setVisibility(View.GONE);
        etPhone.setText("");
        etPassword.setText("");
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
    }

}
