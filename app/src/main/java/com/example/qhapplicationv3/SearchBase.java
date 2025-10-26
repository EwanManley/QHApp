package com.example.qhapplicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SearchBase extends AppCompatActivity {
    protected abstract String getFieldKey();
    protected abstract String getScreenTitle();
    protected String role = "PUBLIC";
    protected String council = null;
    protected final OkHttpClient client = new OkHttpClient();
    protected final List<JSONObject> all = new ArrayList<>();
    protected final List<JSONObject> filtered = new ArrayList<>();
    protected VendorList adapter;
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final String RPC_EXTERNAL = "/rest/v1/rpc/get_external_register";
    private static final String RPC_INTERNAL = "/rest/v1/rpc/get_internal_register";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int PAGE_LIMIT = 1000;
    private static final int PAGE_OFFSET = 0;

    //Checks if user is public user
    private boolean isPublicUser() {
        String r = UserAccount.get().getRole();
        return r == null || r.equalsIgnoreCase("PUBLIC");
    }

    //Sets up search screen
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list_page);
        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (UserAccount.get().getRole() != null) role = UserAccount.get().getRole();
        council = UserAccount.get().getCouncil();
        boolean isPublic = role == null || role.equalsIgnoreCase("PUBLIC");
        Button add = findViewById(R.id.btnAdd);
        if (add != null) {
            if (isPublic) {
                add.setVisibility(android.view.View.GONE);
            } else {
                add.setVisibility(android.view.View.VISIBLE);
                add.setOnClickListener(v -> startActivity(new Intent(this, AddVendor.class)));
            }
        }
        TextView title = findViewById(R.id.titleField);
        if (title != null) title.setText(getScreenTitle());
        RecyclerView rv = findViewById(R.id.recyclerVendors);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        //Sets up list of vendors, handles row clicks
        adapter = new VendorList(filtered, o -> {
            Intent d = new Intent(this, VendorDetails.class);
            d.putExtra("id", o.optString("id", ""));
            d.putExtra("role", role);
            d.putExtra("lga", o.optString("[LGA Name]", ""));
            d.putExtra("name", o.optString("[* Name/s]", ""));
            d.putExtra("tradingName", o.optString("[* Trading name]", ""));
            d.putExtra("status", o.optString("[Status]", ""));
            d.putExtra("phone", o.optString("[* Phone]", ""));
            d.putExtra("licence", o.optString("[* Licence number]", ""));
            d.putExtra("expiry", o.optString("[* Expiry date]", ""));
            d.putExtra("description", o.optString("[* Description of the food business]", ""));
            d.putExtra("vehicle", o.optString("[Type of vehicle]", ""));
            d.putExtra("make", o.optString("[Make]", ""));
            d.putExtra("model", o.optString("[Model]", ""));
            d.putExtra("colour", o.optString("[Colour]", ""));
            d.putExtra("primaryLocation", o.optString("[Primary location of vending machine]", ""));
            d.putExtra("serial", o.optString("[* Serial number/ identification number/mark]", ""));
            d.putExtra("other1", o.optString("[Other distinguishing features]", ""));
            startActivity(d);
        });
        rv.setAdapter(adapter);
        //Handles active filtering for search bar
        SearchView sv = findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                filter(q);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String q) {
                filter(q);
                return true;
            }
        });
        final boolean publicUser = isPublicUser();
        final String token = UserAccount.get().getAccessToken();
        //Loads data from supabase, depending on user access level
        if (publicUser) {
            fetchExternal();
        } else {
            if (TextUtils.isEmpty(token)) {
                fetchExternal();
            } else {
                fetchInternalWithToken(token);
            }
        }
    }
    //Pulls data from external table if Public User
    private void fetchExternal() {
        postRpc(RPC_EXTERNAL, null);
    }

    //If User has correct token, pulls from internal table
    private void fetchInternalWithToken(String bearerToken) {
        postRpc(RPC_INTERNAL, bearerToken);
    }

    //Sends request to Supabase to get data
    private void postRpc(String rpcPath, String bearerToken) {
        JSONObject body = new JSONObject();
        try {
            body.put("p_limit", PAGE_LIMIT);
            body.put("p_offset", PAGE_OFFSET);
        } catch (Exception ignored) { }
        Request.Builder b = new Request.Builder()
                .url(BASE + rpcPath)
                .addHeader("apikey", ANON)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json");
        if (!TextUtils.isEmpty(bearerToken)) b.addHeader("Authorization", "Bearer " + bearerToken);
        Request req = b.post(RequestBody.create(body.toString(), JSON)).build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    filtered.clear();
                    adapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    return;
                }
                try {
                    JSONArray arr = new JSONArray(raw);
                    all.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o != null) all.add(o);
                    }
                    runOnUiThread(() -> {
                        filter("");
                    });
                } catch (Exception ex) {
                }
            }
        });
    }

    //Filters the list to reflect text in search bar
    protected void filter(String q) {
        String s = q == null ? "" : q.trim().toLowerCase();
        filtered.clear();
        for (JSONObject o : all) {
            boolean match = false;
            if (role == null || role.equalsIgnoreCase("PUBLIC")) {
                String[] cols = {
                        "[LGA Name]",
                        "[* Phone]",
                        "[* Trading name]",
                        "[* Registration number]",
                        "[* Licence number]",
                        "[* Name/s]",
                        "[* Expiry date]"
                };
                for (String col : cols) {
                    String v = o.optString(col, "");
                    if (!TextUtils.isEmpty(v) && v.toLowerCase().contains(s)) {
                        match = true;
                        break;
                    }
                }
            } else {
                JSONArray names = o.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.optString(i, "");
                        String v = o.optString(key, "");
                        if (!TextUtils.isEmpty(v) && v.toLowerCase().contains(s)) {
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (s.isEmpty() || match) {
                filtered.add(o);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
