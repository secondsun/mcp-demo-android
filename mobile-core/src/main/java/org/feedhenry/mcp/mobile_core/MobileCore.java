package org.feedhenry.mcp.mobile_core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by summers on 9/26/17.
 */

public class MobileCore {

    private Retrofit retrofit;

    /*
    * This creates a mapping from ServiceConfig -> DefaultServiceConfig.  This is here so that tests
    * can inject their own gson.
    *
    * TODO: Make this better.  Ideas, extract gson to a utility class that tests can manipulate
    *
    * */
    private Gson gson = new GsonBuilder().registerTypeAdapter(ServiceConfig.class, new JsonDeserializer<ServiceConfig>() {
        @Override
        public ServiceConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<String, String> configuration = new Gson().fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
            return new DefaultServiceConfig(configuration);
        }
    }).create();

    public Single<ServiceConfig> configure(final ServerConfig configuration) {


        retrofit = new Retrofit.Builder()
                .baseUrl(configuration.host)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        //TODO turn into lambda, while coding IntelliJ gave an error.
        return Single.fromCallable(
                new Callable<ServiceConfig>() {
                    @Override
                    public ServiceConfig call() throws Exception {
                        return retrofit.create(ConfigService.class)
                                .getConfig(configuration.appID, configuration.apiKey)
                                .execute()
                                .body();
                    }
                }
        ).subscribeOn(Schedulers.newThread());


    }

}
