package edu.travels.travelapp.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import edu.travels.travelapp.MyApplication;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.117.64.183:8080/";
    
    // 调试模式，设置为true时显示详细日志
    private static final boolean DEBUG_MODE = true;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_TOKEN = "token";

    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        // 强制重新创建实例（用于调试，确保BASE_URL生效）
        // 生产环境可以恢复 if (retrofit == null) 检查
        if (retrofit == null || DEBUG_MODE) {
            if (DEBUG_MODE && retrofit != null) {
                Log.d("RetrofitClient", "重新创建Retrofit实例，BASE_URL: " + BASE_URL);
            }
            retrofit = null; // 清除旧实例
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        Context ctx = MyApplication.getContext();
                        String token = null;
                        if (ctx != null) {
                            try {
                                SharedPreferences sp = ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                token = sp.getString("token", null);
                            } catch (Exception e) {
                                Log.e("RetrofitInterceptor", "获取token失败", e);
                            }
                        }

                        Request.Builder builder = original.newBuilder();
                        
                        // 根据接口路径选择不同的token传递方式
                        String url = original.url().toString();
                        if (url.contains("/auth/update-user-info") || url.contains("/auth/upload-avatar")) {
                            // 使用Authorization头
                            if (token != null) {
                                builder.header("Authorization", "Bearer " + token);
                                if (DEBUG_MODE) {
                                    Log.d("RetrofitInterceptor", "使用Authorization头: Bearer " + token.substring(0, Math.min(20, token.length())) + "...");
                                }
                            }
                        } else if (url.contains("/image/upload") || url.contains("/image/like") || url.contains("/image/collect")) {
                            // 图片上传、点赞、收藏接口使用token头
                            if (token != null) {
                                builder.header("token", token);
                                if (DEBUG_MODE) {
                                    Log.d("RetrofitInterceptor", "使用token头: " + token.substring(0, Math.min(20, token.length())) + "...");
                                }
                            }
                        }

                        Request request = builder.build();
                        
                        // 记录请求信息
                        if (DEBUG_MODE) {
                            Log.d("RetrofitInterceptor", "请求URL: " + request.url());
                            Log.d("RetrofitInterceptor", "请求方法: " + request.method());
                            // 记录所有请求头
                            Log.d("RetrofitInterceptor", "请求头数量: " + request.headers().size());
                            for (String name : request.headers().names()) {
                                String value = request.header(name);
                                // 对于token，只显示前20个字符
                                if ("token".equalsIgnoreCase(name) || "Authorization".equalsIgnoreCase(name)) {
                                    if (value != null && value.length() > 20) {
                                        value = value.substring(0, 20) + "...";
                                    }
                                }
                                Log.d("RetrofitInterceptor", "  " + name + ": " + value);
                            }
                            // 特别检查token是否存在
                            if (url.contains("/image/upload") || url.contains("/image/like") || url.contains("/image/collect")) {
                                String tokenHeader = request.header("token");
                                if (tokenHeader == null) {
                                    Log.e("RetrofitInterceptor", "警告: 接口缺少token头！token值: " + (token != null ? "存在" : "null"));
                                } else {
                                    Log.d("RetrofitInterceptor", "✓ token头已添加，长度: " + tokenHeader.length());
                                }
                            }
                        }
                        
                        return chain.proceed(request);
                    });

            // 添加响应日志拦截器
            if (DEBUG_MODE) {
                clientBuilder.addInterceptor(chain -> {
                    Request request = chain.request();
                    long startTime = System.nanoTime();
                    
                    Log.d("RetrofitInterceptor", "发送请求: " + request.method() + " " + request.url());
                    
                    Response response = chain.proceed(request);
                    long endTime = System.nanoTime();
                    
                    Log.d("RetrofitInterceptor", "收到响应: " + response.code() + " " + response.message() + " (耗时: " + (endTime - startTime)/1000000 + "ms)");
                    
                    // 记录响应体内容预览（使用peekBody不会消耗响应流）
                    try {
                        okhttp3.ResponseBody responseBody = response.body();
                        Log.d("RetrofitInterceptor", "响应体检查 - responseBody == null: " + (responseBody == null));
                        if (responseBody != null) {
                            okhttp3.ResponseBody peekBody = response.peekBody(10240); // 最多读取10KB用于日志
                            String bodyString = peekBody.string();
                            Log.d("RetrofitInterceptor", "响应体长度: " + bodyString.length());
                            if (bodyString.length() > 500) {
                                bodyString = bodyString.substring(0, 500) + "...(已截断)";
                            }
                            Log.e("RetrofitInterceptor", "响应体预览: " + bodyString); // 使用E级别确保显示
                        } else {
                            Log.e("RetrofitInterceptor", "响应体为null，响应码: " + response.code()); // 使用E级别确保显示
                        }
                    } catch (Exception e) {
                        Log.e("RetrofitInterceptor", "读取响应体预览失败", e);
                        e.printStackTrace();
                    }
                    
                    return response;
                });
            }

            OkHttpClient client = clientBuilder.build();

            // 配置 Gson 以支持多种日期格式
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}