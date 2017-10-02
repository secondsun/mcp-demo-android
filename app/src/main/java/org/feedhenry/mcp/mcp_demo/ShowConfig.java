package org.feedhenry.mcp.mcp_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.feedhenry.sdk.android.AndroidUtilFactory;
import com.feedhenry.sdk.sync.FHSyncClient;
import com.feedhenry.sdk.sync.FHSyncConfig;
import com.feedhenry.sdk.sync.FHSyncListener;
import com.feedhenry.sdk.sync.NotificationMessage;
import com.feedhenry.sdk.utils.UtilFactory;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.feedhenry.mcp.mobile_core.MobileCore;
import org.feedhenry.mcp.mobile_core.ServerConfig;
import org.json.JSONObject;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class ShowConfig extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_config);
        textView = findViewById(R.id.textView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobileCore core =new MobileCore(getApplicationContext());
        core.configure(this).observeOn(AndroidSchedulers.mainThread())
                .subscribe((serviceConfig)-> {
                    Map<String, JsonElement> syncConfig = serviceConfig.getConfigFor("fh-sync-server");
                    textView.setText(new GsonBuilder().setPrettyPrinting().create().toJson(serviceConfig));
                    //startSync(syncConfig.get("uri").getAsString());
                });
    }

    private void startSync(String uri) {

        FHSyncConfig config = new FHSyncConfig.Builder()
                .notifyLocalUpdateApplied(true)
                .notifySyncComplete(true)
                .build();

        UtilFactory utilFactory = new AndroidUtilFactory(this);
        utilFactory.getNetworkClient().setCloudURL(uri);
        FHSyncClient client = new FHSyncClient(config, utilFactory);

        client.manage("myShoppingList", null, new JSONObject());
        client.setListener(new FHSyncListener() {
            @Override
            public void onSyncStarted(NotificationMessage message) {

            }

            @Override
            public void onSyncCompleted(NotificationMessage message) {
                Log.w("odkdokeok", message.getDataId());
                Log.d("Data", new GsonBuilder().setPrettyPrinting().create().toJson(client.list("myShoppingList")));
            }

            @Override
            public void onUpdateOffline(NotificationMessage message) {

            }

            @Override
            public void onCollisionDetected(NotificationMessage message) {

            }

            @Override
            public void onRemoteUpdateFailed(NotificationMessage message) {

            }

            @Override
            public void onRemoteUpdateApplied(NotificationMessage message) {

            }

            @Override
            public void onLocalUpdateApplied(NotificationMessage message) {

            }

            @Override
            public void onDeltaReceived(NotificationMessage message) {

            }

            @Override
            public void onSyncFailed(NotificationMessage message) {

            }

            @Override
            public void onClientStorageFailed(NotificationMessage message) {

            }
        });
    }
}
