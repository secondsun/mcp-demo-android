package org.feedhenry.mcp.mcp_demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.feedhenry.sdk.sync.FHSyncClient;
import com.google.gson.JsonElement;

import org.feedhenry.mcp.mcp_demo.adapter.ShoppingItemAdapter;
import org.feedhenry.mcp.mcp_demo.helper.KeycloakHelper;
import org.feedhenry.mcp.mcp_demo.helper.SwipeTouchHelper;
import org.feedhenry.mcp.mcp_demo.listener.RecyclerItemClickListener;
import org.feedhenry.mcp.mcp_demo.model.ShoppingItem;
import org.feedhenry.mcp.mobile_core.MobileCore;
import org.jboss.aerogear.android.core.Callback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * This activity displays a splash screen while the configuration is loaded in the background.
 */
public class SyncActivity extends AppCompatActivity implements SyncPresenter.View<ShoppingItem> {

    private static final String TAG = "SyncActivity";

    private SyncPresenter presenter;
    private ShoppingItemAdapter adapter = new ShoppingItemAdapter();
    private RecyclerView list;
    private FHSyncClient client;
    private static final String DATASET_ID = "myShoppingList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        presenter = new SyncPresenter();

        list = (RecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        list.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                (view, position) -> showPopup(adapter.getItem(position))
        ));

        SwipeTouchHelper callback = new SwipeTouchHelper(new SwipeTouchHelper.OnItemSwipeListener() {
            @Override
            public void onItemSwipe(ShoppingItem item) {
                deleteItem(item);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(new ShoppingItem());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.disconnect();
        try {
            client.stop(DATASET_ID);
        } catch (Exception ignore) {}
    }

    @Override
    protected void onResume() {
        super.onResume();


        //Connect to mobile core
        MobileCore core =new MobileCore(getApplicationContext());
        core.configure(this).observeOn(AndroidSchedulers.mainThread())
                .subscribe((serviceConfig)-> {
                    //setup sync
                    Map<String, JsonElement> syncConfig = serviceConfig.getConfigFor("fh-sync-server");
                    Map<String, JsonElement> keycloakConfig = serviceConfig.getConfigFor("keycloak");
                    if (keycloakConfig != null) {
                        KeycloakHelper.Builder builder = new KeycloakHelper.Builder();
                        builder.setAuthzUrl(keycloakConfig.get("auth-server-url").getAsString());
                        builder.setClientId(keycloakConfig.get("clientId").getAsString());
                        builder.setRealm(keycloakConfig.get("realm").getAsString());
                        KeycloakHelper helper = builder.build();

                            helper.connect(this, new Callback() {
                                @Override
                                public void onSuccess(Object data) {
                                    startSync(syncConfig.get("uri").getAsString(), helper.getBearerToken());
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("ERROR", e.getMessage(), e);
                                }
                            });

                    } else {
                        startSync(syncConfig.get("uri").getAsString(), null);
                    }
                });
    }

    private void startSync(String uri, String bearerToken) {
        presenter.connect(this, uri, bearerToken);
        client = presenter.getClient();
    }

    @Override
    public void syncDocumentsUpdated(Set<ShoppingItem> documents) {

        adapter.removeMissingItemsFrom(documents);
        adapter.addNewItemsFrom(documents);

        adapter.notifyDataSetChanged();
    }

    private void showPopup(final ShoppingItem item) {
        final View customView = View.inflate(getApplicationContext(), R.layout.form_item_dialog, null);
        final EditText name = (EditText) customView.findViewById(R.id.name);
        name.setText(item.getName());

        new MaterialDialog.Builder(this)
                .title((item.getId() == null) ? getString(R.string.new_item)
                        : getString(R.string.edit_item) + ": " + item.getName())
                .customView(customView, false)
                .positiveText(R.string.save)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        item.setName(name.getText().toString());
                        saveItem(item);
                    }
                })
                .negativeText(R.string.cancel)
                .show();
    }

    private void saveItem(ShoppingItem item) {

        JSONObject data = new JSONObject();
        try {
            data.put("name", item.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            if (item.getId() == null) {
                data.put("created", String.valueOf(new Date().getTime()));
                client.create(DATASET_ID, data);
            } else {
                data.put("created", item.getCreated());
                client.update(DATASET_ID, item.getId(), data);
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to data data: " + data.toString(), e);
        }

    }

    private void deleteItem(ShoppingItem item) {
        try {
            client.delete(DATASET_ID, item.getId());
        } catch (Exception e) {
            Log.e(TAG, "failed to delete data: " + item.getId(), e);
        }
    }

}
