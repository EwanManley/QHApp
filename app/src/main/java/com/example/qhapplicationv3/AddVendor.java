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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class AddVendor extends AppCompatActivity {

    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient http = new OkHttpClient();
    private EditText etLga, etName, etTrading, etPhone, etLicence, etExpiry, etReg, etDesc, etVehicle, etMake, etModel, etColour, etPrimary, etSerial, etOther1, etStatus;

    //Loads the screen for adding vendors.
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
        if (tvFormTitle != null) tvFormTitle.setText("Add vendor");
        String contextTxt = "QH".equalsIgnoreCase(role) ? "Adding to: Choose council" : "Adding to: " + CouncilLookup.toDisplay(council);
        if (tvLgaContext != null) tvLgaContext.setText(contextTxt);
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
        btnSave.setOnClickListener(v -> beginCreate());
    }

    //Validates that necessary fields are filled.
    private void beginCreate() {
        String role = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        String bearer = UserAccount.get().getAccessToken();
        if (TextUtils.isEmpty(bearer)) return;
        if ("PUBLIC".equalsIgnoreCase(role)) return;
        String lga = nz(etLga.getText().toString());
        String name = nz(etName.getText().toString());
        String trading = nz(etTrading.getText().toString());
        String phone = nz(etPhone.getText().toString());
        String licence = nz(etLicence.getText().toString());
        String expiry = nz(etExpiry.getText().toString());
        String reg = nz(etReg.getText().toString());
        if (TextUtils.isEmpty(lga) || TextUtils.isEmpty(name) || TextUtils.isEmpty(trading)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(licence)
                || TextUtils.isEmpty(expiry) || TextUtils.isEmpty(reg)) {
            return;
        }
        if ("COUNCIL".equalsIgnoreCase(role) && !TextUtils.isEmpty(council)) {
            boolean okExact = council.equals(lga);
            boolean okSlug = council.equalsIgnoreCase(slug(lga));
            boolean okDisplay = lga.equalsIgnoreCase(council.replace("-", " "));
            if (!(okExact || okSlug || okDisplay)) return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("[LGA Name]", lga);
            body.put("[* Name/s]", name);
            body.put("[* Trading name]", trading);
            String status = nz(etStatus.getText().toString());
            if (!status.isEmpty()) body.put("[Status]", status);
            body.put("[* Phone]", phone);
            body.put("[* Licence number]", licence);
            body.put("[* Expiry date]", expiry);
            body.put("[* Registration number]", reg);
            String desc = nz(etDesc.getText().toString());
            if (!desc.isEmpty()) body.put("[* Description of the food business]", desc);
            String vehicle = nz(etVehicle.getText().toString());
            if (!vehicle.isEmpty()) body.put("[Type of vehicle]", vehicle);
            String make = nz(etMake.getText().toString());
            if (!make.isEmpty()) body.put("[Make]", make);
            String model = nz(etModel.getText().toString());
            if (!model.isEmpty()) body.put("[Model]", model);
            String colour = nz(etColour.getText().toString());
            if (!colour.isEmpty()) body.put("[Colour]", colour);
            String primary = nz(etPrimary.getText().toString());
            if (!primary.isEmpty()) body.put("[Primary location of vending machine]", primary);
            String serial = nz(etSerial.getText().toString());
            if (!serial.isEmpty()) body.put("[* Serial number/ identification number/mark]", serial);
            String other1 = nz(etOther1.getText().toString());
            if (!other1.isEmpty()) body.put("[Other distinguishing features]", other1);
        } catch (Exception e) {
            return;
        }
        doCreate(bearer, body);
    }

    //Send create request to Supabase.
    private void doCreate(String bearer, JSONObject body) {
        Request req = new Request.Builder()
                .url(BASE + "/rest/v1/qh_register")
                .addHeader("apikey", ANON)
                .addHeader("Authorization", "Bearer " + bearer)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String resp = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful()) {
                        return;
                    }
                    runOnUiThread(() -> finish());
                } catch (Exception ex) {
                }
            }
        });
    }

    //Replaces unsafe text with hyphens.
    private static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }

    //Cleans up text input.
    private String nz(String s) { return s == null ? "" : s.trim(); }
}
