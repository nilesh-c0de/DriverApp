package com.example.driverapp.Remote;

import com.example.driverapp.Model.FCMResponse;
import com.example.driverapp.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAtN8S5Bc:APA91bEPG16JfUyzTLeRtCEEH7k9Wr1JTUOVSevXU-Yhl7T8_34OvCvUg2uvJZrZPHjkpm8RxW9q4sjLonjhBLq7bNVMsE6L7G3mzr6ZqyMgbLpRw40_2WiSNpjpk3Lc7REWHi7sroqp"
    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessege(@Body Sender body);
}
