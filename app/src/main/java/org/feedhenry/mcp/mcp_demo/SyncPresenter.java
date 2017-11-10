package org.feedhenry.mcp.mcp_demo;

import android.content.Context;
import android.util.Log;

import com.feedhenry.sdk.android.AndroidUtilFactory;
import com.feedhenry.sdk.android.NetworkClientImpl;
import com.feedhenry.sdk.sync.FHSyncClient;
import com.feedhenry.sdk.sync.FHSyncConfig;
import com.feedhenry.sdk.sync.FHSyncListener;
import com.feedhenry.sdk.sync.NotificationMessage;
import com.feedhenry.sdk.utils.UtilFactory;

import org.feedhenry.mcp.mcp_demo.model.ShoppingItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Headers;

/**
 * Created by summers on 10/17/17.
 */

public class SyncPresenter implements FHSyncListener {

    private static final String DATASET_ID = "myShoppingList";
    private static final String TAG = "SyncPresenter";
    private FHSyncClient client;

    public interface View<DOCUMENT_TYPE> {
        void syncDocumentsUpdated(Set<DOCUMENT_TYPE> documents);

        Context getApplicationContext();
    }

    private View syncActivity;

    public void connect(View syncActivity, String uri, String bearerToken) {
        this.syncActivity = syncActivity;
        FHSyncConfig config = new FHSyncConfig.Builder()
                .notifyLocalUpdateApplied(true)
                .notifySyncComplete(true)
                .build();

        UtilFactory utilFactory = new AndroidUtilFactory(syncActivity.getApplicationContext());
        utilFactory.getNetworkClient().setCloudURL(uri + "/sync/");
        utilFactory.getNetworkClient().registerNetworkListener();
        if (bearerToken != null) {
            Headers.Builder builder = new Headers.Builder().add("Authorization", bearerToken);
            ((NetworkClientImpl) utilFactory.getNetworkClient()).setHeaders(builder.build());
        }
        client = new FHSyncClient(config, utilFactory);
        client.manage(DATASET_ID, null, new JSONObject());
        client.setListener(this);
    }

    public FHSyncClient getClient() {
        return client;
    }

    public void disconnect() {
        syncActivity = null;
    }

    @Override
    public void onSyncStarted(NotificationMessage message) {

    }

    @Override
    public void onSyncCompleted(NotificationMessage message) {

        Log.d(TAG, "syncClient - onSyncCompleted");
        Log.d(TAG, "Sync message: " + message.getMessage());

        JSONObject allData = client.list(DATASET_ID);
        Iterator<String> it = allData.keys();
        TreeSet<ShoppingItem> itemsToSync = new TreeSet<ShoppingItem>();

        while (it.hasNext()) {
            String key = it.next();
            JSONObject data = null;
            try {
                data = allData.getJSONObject(key);

                JSONObject dataObj = data.getJSONObject("data");
                String name = dataObj.optString("name", "NO name");
                if (name.startsWith("N")) {
                    Log.d(TAG, "Sync Complete Name : " + name);
                }
                String created = dataObj.optString("created", "no date");
                ShoppingItem item = new ShoppingItem(key, name, created);
                itemsToSync.add(item);
            } catch (JSONException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }

        syncActivity.syncDocumentsUpdated(itemsToSync);

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
        Log.d(TAG, "syncClient - onLocalUpdateApplied");

        JSONObject allData = client.list(DATASET_ID);

        Iterator<String> it = allData.keys();
        TreeSet<ShoppingItem> itemsToSync = new TreeSet<ShoppingItem>();

        while (it.hasNext()) {
            try {
                String key = it.next();
                JSONObject data = allData.getJSONObject(key);
                JSONObject dataObj = data.getJSONObject("data");
                String name = dataObj.optString("name", "NO name");
                if (name.startsWith("N")) {
                    Log.d(TAG, "Local Name : " + name);
                }
                String created = dataObj.optString("created", "no date");
                ShoppingItem item = new ShoppingItem(key, name, created);
                itemsToSync.add(item);
            } catch(Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }

        syncActivity.syncDocumentsUpdated(itemsToSync);
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
}
