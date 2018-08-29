package com.gotako.govoz.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.*;

public class ImgurModule {
    public static ImgurService getImgurService() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("https://api.imgur.com/3/")
                .build();

        return retrofit.create(ImgurService.class);
    }
}
