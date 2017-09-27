package org.feedhenry.mcp.mcp_demo;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;

import org.feedhenry.mcp.mobile_core.DefaultServiceConfig;
import org.feedhenry.mcp.mobile_core.MobileCore;
import org.feedhenry.mcp.mobile_core.ServerConfig;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ShowConfig extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_config);
        textView = findViewById(R.id.textView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MobileCore core =new MobileCore(getApplicationContext());
        core.configure(new ServerConfig("https://192.168.37.1:3001", "cf0b4ce1-7aaf-434b-9a13-72c433b0e723","sync-tests-1506437873")).observeOn(AndroidSchedulers.mainThread())
                .subscribe((serviceConfig)-> {
                    Map<String, JsonElement> config = ((DefaultServiceConfig) serviceConfig).config;
                    StringBuilder builder = new StringBuilder();
                    for (String key : config.keySet()) {
                        builder.append(key + ":" + config.get(key) + "\n");
                    }
                    textView.setText(builder.toString());
                });
    }
}
