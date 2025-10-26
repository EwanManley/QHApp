package com.example.qhapplicationv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//Class handles user login, registration and saves user details.
public class UserAccount {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient http = new OkHttpClient();
    private static UserAccount instance;
    private UserAccount() {}
    public static UserAccount get() {
        if (instance == null) instance = new UserAccount();
        return instance;
    }

    private String email;
    private String role;
    private String council;
    private String accessToken;

    //Sends back login or register results.
    public interface AuthCallback {
        void onResult(boolean ok, String message, JSONObject profile);
    }

    //Registers a new user account and saves user details.
    public static void register(Context ctx, String email, String password, String role, String council, AuthCallback cb) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            Request req = new Request.Builder()
                    .url(BASE + "/auth/v1/signup")
                    .addHeader("apikey", ANON)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) { cb.onResult(false, "Network error: " + safe(e), null); }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String raw = response.body() == null ? "" : response.body().string();
                        if (!response.isSuccessful()) { cb.onResult(false, "Sign-up HTTP " + response.code() + " " + trim(raw, 160), null); return; }
                        JSONObject obj = new JSONObject(raw);
                        String token = obj.optJSONObject("session") != null
                                ? obj.optJSONObject("session").optString("access_token", null)
                                : obj.optString("access_token", null);
                        if (TextUtils.isEmpty(token)) { cb.onResult(false, "Please verify your email to activate the account.", null); return; }
                        upsertProfile(ctx, email, role, council, token, (ok, msg, profile) -> {
                            if (!ok) { cb.onResult(false, msg, null); return; }
                            UserAccount ua = UserAccount.get();
                            ua.email = email;
                            ua.role = role;
                            ua.council = council;
                            ua.accessToken = token;
                            ua.saveSession(ctx);
                            cb.onResult(true, null, profile);
                        });
                    } catch (Exception ex) { cb.onResult(false, "Sign-up parse error", null); }
                }
            });
        } catch (Exception e) { cb.onResult(false, "Sign-up build error", null); }
    }

    //Logs in the user and loads their stored information.
    public static void login(Context ctx, String email, String password, AuthCallback cb) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            Request req = new Request.Builder()
                    .url(BASE + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", ANON)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) { cb.onResult(false, "Auth network error: " + safe(e), null); }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String raw = response.body() == null ? "" : response.body().string();
                        if (!response.isSuccessful()) { cb.onResult(false, "Auth HTTP " + response.code() + " " + trim(raw, 160), null); return; }
                        JSONObject obj = new JSONObject(raw);
                        String token = obj.optString("access_token", null);
                        if (TextUtils.isEmpty(token)) { cb.onResult(false, "No token in response", null); return; }
                        fetchProfile(ctx, email, token, (ok, msg, profile) -> {
                            if (!ok) { cb.onResult(false, msg, null); return; }
                            String role = "PUBLIC";
                            String council = null;
                            if (profile != null) {
                                role = profile.optString("role", role);
                                council = profile.optString("council", null);
                            }
                            UserAccount ua = UserAccount.get();
                            ua.email = email;
                            ua.role = TextUtils.isEmpty(role) ? "PUBLIC" : role;
                            ua.council = council;
                            ua.accessToken = token;
                            ua.saveSession(ctx);
                            cb.onResult(true, null, profile);
                        });
                    } catch (Exception ex) { cb.onResult(false, "Auth parse error", null); }
                }
            });
        } catch (Exception e) { cb.onResult(false, "Auth build error", null); }
    }

    //Gets the users saved role, email and/or council.
    private static void fetchProfile(Context ctx, String email, String token, AuthCallback cb) {
        try {
            String url = BASE + "/rest/v1/profile?select=role,council&email=eq." + URLEncoder.encode(email, StandardCharsets.UTF_8.name()) + "&limit=1";
            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", ANON)
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) { cb.onResult(false, "Profile network error: " + safe(e), null); }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String raw = response.body() == null ? "" : response.body().string();
                        if (!response.isSuccessful()) { cb.onResult(false, "Profile HTTP " + response.code() + " " + trim(raw, 160), null); return; }
                        JSONArray arr = new JSONArray(raw);
                        JSONObject profile = arr.length() > 0 ? arr.getJSONObject(0) : null;
                        cb.onResult(true, null, profile);
                    } catch (Exception ex) { cb.onResult(false, "Profile parse error", null); }
                }
            });
        } catch (Exception e) { cb.onResult(false, "Profile build error", null); }
    }

    //Saves or updates the user details stored in supabase.
    private static void upsertProfile(Context ctx, String email, String role, String council, String token, AuthCallback cb) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("role", role);
            if (!TextUtils.isEmpty(council)) body.put("council", council);
            Request req = new Request.Builder()
                    .url(BASE + "/rest/v1/profile")
                    .addHeader("apikey", ANON)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) { cb.onResult(false, "Profile upsert network error: " + safe(e), null); }
                @Override public void onResponse(Call call, Response response) {
                    try {
                        String raw = response.body() == null ? "" : response.body().string();
                        if (!response.isSuccessful()) { cb.onResult(false, "Profile upsert HTTP " + response.code() + " " + trim(raw, 160), null); return; }
                        JSONObject profile = new JSONObject();
                        try { profile.put("role", role); } catch (Exception ignored) {}
                        try { if (!TextUtils.isEmpty(council)) profile.put("council", council); } catch (Exception ignored) {}
                        cb.onResult(true, null, profile);
                    } catch (Exception ex) { cb.onResult(false, "Profile upsert parse error", null); }
                }
            });
        } catch (Exception e) { cb.onResult(false, "Profile upsert build error", null); }
    }

    //Saves user information.
    public void saveSession(Context ctx) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
            JSONObject obj = new JSONObject();
            obj.put("email", email == null ? JSONObject.NULL : email);
            obj.put("role", role == null ? JSONObject.NULL : role);
            obj.put("council", council == null ? JSONObject.NULL : council);
            obj.put("accessToken", accessToken == null ? JSONObject.NULL : accessToken);
            prefs.edit().putString("session_json", obj.toString()).apply();
        } catch (Exception ignored) {}
    }

    //Loads saved login information.
    public void loadSavedSession(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
        String raw = prefs.getString("session_json", null);
        if (raw == null) return;
        try {
            JSONObject obj = new JSONObject(raw);
            this.email = obj.optString("email", null);
            this.role = obj.optString("role", null);
            this.council = obj.optString("council", null);
            this.accessToken = obj.optString("accessToken", null);
        } catch (Exception ignored) {}
    }

    //Clears session data.
    public void clearSession(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
        prefs.edit().remove("session_json").apply();
        email = role = council = accessToken = null;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getCouncil() { return council; }
    public void setCouncil(String council) { this.council = council; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    private static String trim(String s, int n) { return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…"); }
    private static String safe(Throwable t) { return t == null || t.getMessage() == null ? "" : t.getMessage(); }
}
