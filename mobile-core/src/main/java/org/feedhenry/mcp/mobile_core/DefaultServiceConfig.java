package org.feedhenry.mcp.mobile_core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * Created by summers on 9/26/17.
 */

class DefaultServiceConfig implements ServiceConfig {

    private final Map<String, String> config;

    DefaultServiceConfig(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public Map<String, String> getConfigFor(String serviceName) {
        String serviceConfig = config.get(serviceName);
        if (serviceConfig == null) {
            return null;
        }

        //TODO this looks silly, find a better way to handle the nested JSON
        JsonElement jsonConfig = new JsonParser().parse(serviceConfig);
        Map<String, String> map = new Gson().fromJson(jsonConfig, new TypeToken<Map<String, String>>() {
        }.getType());

        if (map.containsKey("config")) {
            return map;
        }
        return null;
    }
}
