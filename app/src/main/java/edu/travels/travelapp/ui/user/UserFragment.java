package edu.travels.travelapp.ui.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFragment extends Fragment {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private LinearLayout layoutLogin, layoutProfile;
    private TextView tvNickname;
    private Button btnLogout;

    private ApiService apiService;
    private SharedPreferences sp;

    private static final String SP_NAME = "travel_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NICKNAME = "nickname";

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
        btnLogout = view.findViewById(R.id.btn_logout);

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        sp = requireActivity().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

        checkLoginStatus();

        btnLogin.setOnClickListener(v -> loginOrRegister());
        btnLogout.setOnClickListener(v -> logout());

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
                            saveLoginSuccess(token, nickname);
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
        String nickname = "游客" + phone.substring(7);
        UserRegisterDTO dto = new UserRegisterDTO(phone, password, nickname);

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
                                            String nick = data.optString("nickname", nickname);
                                            saveLoginSuccess(token, nick);
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

    private void saveLoginSuccess(String token, String nickname) {
        sp.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_NICKNAME, nickname)
                .apply();

        tvNickname.setText(nickname);
        layoutLogin.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "登录成功！欢迎 " + nickname, Toast.LENGTH_LONG).show();
    }

    private void checkLoginStatus() {
        String token = sp.getString(KEY_TOKEN, null);
        String nickname = sp.getString(KEY_NICKNAME, null);
        if (token != null && nickname != null) {
            tvNickname.setText(nickname);
            layoutLogin.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.VISIBLE);
        }
    }

    private void logout() {
        sp.edit().clear().apply();
        layoutLogin.setVisibility(View.VISIBLE);
        layoutProfile.setVisibility(View.GONE);
        etPhone.setText("");
        etPassword.setText("");
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
    }

}
