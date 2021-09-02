package com.example.driverapp.Common;

import com.example.driverapp.Remote.FCMClient;
import com.example.driverapp.Remote.IFCMService;
import com.example.driverapp.Remote.IGoogleAPI;
import com.example.driverapp.Remote.RetrofitClient;

public class Global {

    private static final String fcmURL = "https://fcm.googleapis.com/";
    private static final String baseURL = "https://maps.googleapis.com";


    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


}
