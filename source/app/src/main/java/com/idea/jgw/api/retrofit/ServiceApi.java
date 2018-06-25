package com.idea.jgw.api.retrofit;


import com.idea.jgw.api.Api;

/**
 * Created by Ganlin.Wu on 2016/9/21.
 */
public class ServiceApi extends Api<SongApiService> {

    private static ServiceApi sInstance;

    private ServiceApi() {
        super(SongApiService.BASE_URL);
    }

    public static ServiceApi getInstance() {
        if (sInstance == null) {
            synchronized (ServiceApi.class) {
                if (sInstance == null) {
                    sInstance = new ServiceApi();
                }
            }
        }
        return sInstance;
    }


}
