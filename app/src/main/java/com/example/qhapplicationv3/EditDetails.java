package com.example.qhapplicationv3;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class EditDetails extends AppCompatActivity {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient http = new OkHttpClient();
    private String rowId;
    private EditText etTrading, etName, etPhone, etLicence, etReg, etExpiry, etStatus, etDesc, etVehicle, etMake, etModel, etColour, etPrimary, etSerial, etOther1, etLga;

    //Loads the screen for editing vendors
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ("QH".equalsIgnoreCase(UserAccount.get().getRole())) {
            setContentView(R.layout.edit_qh_page);
        } else {
            setContentView(R.layout.edit_page);
        }
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        TextView tvLgaContext = findViewById(R.id.tvLgaContext);
        rowId = getIntent().getStringExtra("id");
        etLga = findViewById(R.id.etLga);
        etName = findViewById(R.id.etName);
        etTrading = findViewById(R.id.etTrading);
        etPhone = findViewById(R.id.etPhone);
        etLicence = findViewById(R.id.etLicence);
        etExpiry = findViewById(R.id.etExpiry);
        etReg = findViewById(R.id.etReg);
        etDesc = findViewById(R.id.etDesc);
        etVehicle = findViewById(R.id.etVehicle);
        etMake = findViewById(R.id.etMake);
        etModel = findViewById(R.id.etModel);
        etColour = findViewById(R.id.etColour);
        etPrimary = findViewById(R.id.etPrimary);
        etSerial = findViewById(R.id.etSerial);
        etOther1 = findViewById(R.id.etOther1);
        etStatus = findViewById(R.id.etStatus);
        Spinner spLga = findViewById(R.id.spLga);
        if ("QH".equalsIgnoreCase(UserAccount.get().getRole())) {
            if (spLga != null && etLga != null) {
                java.util.List<String> councils = CouncilLookup.all();
                ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, councils);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spLga.setAdapter(a);
                spLga.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        etLga.setText(councils.get(pos));
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) { }
                });
            }
        }
        String role = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        if (tvFormTitle != null) tvFormTitle.setText("Edit vendor");
        String contextTxt = "QH".equalsIgnoreCase(role)
                ? "Editing in: Choose council"
                : "Editing in: " + CouncilLookup.toDisplay(council);
        if (tvLgaContext != null) tvLgaContext.setText(contextTxt);
        etLga.setText(nz(getIntent().getStringExtra("lga")));
        etName.setText(nz(getIntent().getStringExtra("name")));
        etTrading.setText(nz(getIntent().getStringExtra("tradingName")));
        etPhone.setText(nz(getIntent().getStringExtra("phone")));
        etLicence.setText(nz(getIntent().getStringExtra("licence")));
        etExpiry.setText(nz(getIntent().getStringExtra("expiry")));
        etReg.setText(nz(getIntent().getStringExtra("registration")));
        etDesc.setText(nz(getIntent().getStringExtra("description")));
        etVehicle.setText(nz(getIntent().getStringExtra("vehicle")));
        etMake.setText(nz(getIntent().getStringExtra("make")));
        etModel.setText(nz(getIntent().getStringExtra("model")));
        etColour.setText(nz(getIntent().getStringExtra("colour")));
        etPrimary.setText(nz(getIntent().getStringExtra("primaryLocation")));
        etSerial.setText(nz(getIntent().getStringExtra("serial")));
        etOther1.setText(nz(getIntent().getStringExtra("other1")));
        etStatus.setText(nz(getIntent().getStringExtra("status")));
        if ("COUNCIL".equalsIgnoreCase(role) && !TextUtils.isEmpty(council)) {
            etLga.setText(CouncilLookup.toDisplay(council));
            etLga.setFocusable(false);
            etLga.setEnabled(false);
            etLga.setClickable(false);
            if (spLga != null) spLga.setVisibility(View.GONE);
        }
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());
    }

    //Checks that input is valid, and saves
    private void save() {
        if (TextUtils.isEmpty(rowId)) { return; }
        String role = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        String bearer = UserAccount.get().getAccessToken();
        if (TextUtils.isEmpty(bearer)) { return; }
        if ("PUBLIC".equalsIgnoreCase(role)) { return; }
        if ("COUNCIL".equalsIgnoreCase(role) && !TextUtils.isEmpty(council)) {
            String currentLga = nz(etLga.getText().toString());
            boolean okExact = council.equals(currentLga);
            boolean okSlugged = council.equalsIgnoreCase(slug(currentLga));
            boolean okDisplay = currentLga.equalsIgnoreCase(council.replace("-", " "));
            if (!(okExact || okSlugged || okDisplay)) { return; }
        }

        JSONObject patch = new JSONObject();
        try {
            putReq(patch, "[LGA Name]", etLga);
            putReq(patch, "[* Name/s]", etName);
            putReq(patch, "[* Trading name]", etTrading);
            String status = nz(etStatus.getText().toString());
            if (!status.isEmpty()) patch.put("[Status]", status);
            putReq(patch, "[* Phone]", etPhone);
            putReq(patch, "[* Licence number]", etLicence);
            putReq(patch, "[* Expiry date]", etExpiry);
            putReq(patch, "[* Registration number]", etReg);
            putOpt(patch, "[* Description of the food business]", etDesc);
            putOpt(patch, "[Type of vehicle]", etVehicle);
            putOpt(patch, "[Make]", etMake);
            putOpt(patch, "[Model]", etModel);
            putOpt(patch, "[Colour]", etColour);
            putOpt(patch, "[Primary location of vending machine]", etPrimary);
            putOpt(patch, "[* Serial number/ identification number/mark]", etSerial);
            putOpt(patch, "[Other distinguishing features]", etOther1);
        } catch (Exception e) {
            return;
        }
        doPatch(bearer, patch);
    }

    //Updates the database with the new/altered information
    private void doPatch(String bearer, JSONObject patch) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/qh_register").newBuilder()
                .addQueryParameter("id", "eq." + rowId)
                .build();
        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON)
                .addHeader("Authorization", "Bearer " + bearer)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(RequestBody.create(patch.toString(), JSON))
                .build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) { }
            @Override public void onResponse(Call call, Response response) {
                try {
                    String resp = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful()) { return; }
                    boolean hasBody = !resp.trim().isEmpty();
                    boolean updated = hasBody && looksLikeUpdated(resp);
                    if (!hasBody) {
                        String cr = response.header("Content-Range", "");
                        if (cr != null && (cr.endsWith("/0") || cr.contains("*/0"))) { return; }
                    }
                    runOnUiThread(() -> finish());
                } catch (Exception ex) { }
            }
        });
    }

    //Adds a required field
    private void putReq(JSONObject o, String key, EditText src) throws Exception { o.put(key, nz(src.getText().toString())); }

    //Adds an optional field
    private void putOpt(JSONObject o, String key, EditText src) throws Exception { String v = nz(src.getText().toString()); if (!v.isEmpty()) o.put(key, v); }

    //Check if the server has updated the record
    private boolean looksLikeUpdated(String body) {
        try { return new JSONArray(body).length() > 0; }
        catch (Exception ignore) { return body.trim().startsWith("{") || body.trim().startsWith("["); }
    }

    //Replaces unsafe text with hyphens
    private static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }

    //Cleans up text input
    private String nz(String s) { return s == null ? "" : s.trim(); }
}
