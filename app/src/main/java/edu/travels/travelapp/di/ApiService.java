package edu.travels.travelapp.di;

import edu.travels.travelapp.model.dto.ImageItemDTO;
import edu.travels.travelapp.model.dto.PageResponseDTO;
import edu.travels.travelapp.model.dto.UserLoginDTO;
import edu.travels.travelapp.model.dto.UserRegisterDTO;
import edu.travels.travelapp.model.dto.UserUpdateDTO;
import edu.travels.travelapp.model.vo.ResultVO;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    // 用户认证相关
    @POST("/api/auth/login")
    Call<ResponseBody> login(@Body UserLoginDTO dto);

    @POST("/api/auth/register")
    Call<ResponseBody> register(@Body UserRegisterDTO dto);

    @PUT("/api/auth/update-user-info")
    Call<ResponseBody> updateUserInfo(@Body UserUpdateDTO dto);

    // 获取用户信息
    @GET("/api/auth/user-info")
    Call<ResultVO<UserUpdateDTO>> getUserInfo();

    // 图片上传 - 返回ResultVO<String>，data为图片ID
    @Multipart
    @POST("/api/image/upload")
    Call<ResultVO<String>> uploadImage(
            @Part MultipartBody.Part file,
            @Part("description") okhttp3.RequestBody description,
            @Part("gpsLat") okhttp3.RequestBody gpsLat,
            @Part("gpsLng") okhttp3.RequestBody gpsLng,
            @Part("locationName") okhttp3.RequestBody locationName
    );

    // 获取图片列表（分页）- 后端返回ResultVO包装的List数组
    @GET("/api/image/list")
    Call<ResultVO<List<ImageItemDTO>>> getImageList(
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    // 更新图片信息（描述、位置）
    @FormUrlEncoded
    @PUT("/api/image")
    Call<ResultVO<String>> updateImage(
            @Query("imageId") String imageId,
            @Field("description") String description,
            @Field("locationName") String locationName
    );

    // 删除图片
    @DELETE("/api/image")
    Call<ResultVO<Void>> deleteImage(
            @Query("imageId") String imageId
    );

}