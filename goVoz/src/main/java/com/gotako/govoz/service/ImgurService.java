package com.gotako.govoz.service;


import com.gotako.govoz.data.ImgurImage;
import com.gotako.govoz.data.ImgurImageAlbum;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ImgurService {
    String CLIENT_ID = "e93a28d86f5587b";

    @Headers("Authorization: Client-ID e93a28d86f5587b")
    @GET("album/{albumHash}/images")
    Call<ImgurImageAlbum> getAlbumImages(@Path("albumHash") String albumHash);

}
