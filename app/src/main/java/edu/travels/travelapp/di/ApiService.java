package edu.travels.travelapp.di;

import edu.travels.travelapp.model.dto.UserLoginDTO;
import edu.travels.travelapp.model.dto.UserRegisterDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<ResponseBody> login(@Body UserLoginDTO dto);

    @POST("/api/auth/register")
    Call<ResponseBody> register(@Body UserRegisterDTO dto);
}