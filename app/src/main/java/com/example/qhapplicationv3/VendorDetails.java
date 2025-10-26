package com.example.qhapplicationv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//Class handles showing User all available details for a Vendor of their choosing
public class VendorDetails extends AppCompatActivity {
    private static final String BASE = "https://mpvttjjpwghyydfumqxi.supabase.co";
    private static final String ANON = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1wdnR0ampwd2doeXlkZnVtcXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcwNTAzODcsImV4cCI6MjA3MjYyNjM4N30.IUkEutAeR0fDZswjXXduZu2CyZJ5eNt9KvCaF0ax9DE";
    private String rowId;
    private String role;
    private String council;

    //Sets up the the screen and displays vendor information
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_info_page);
        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        role = UserAccount.get().getRole();
        council = UserAccount.get().getCouncil();
        boolean isPublic = role == null || role.equalsIgnoreCase("PUBLIC");
        Intent intent = getIntent();
        rowId = intent.getStringExtra("id");
        setField(R.id.fieldLGA, "LGA Name:", intent.getStringExtra("lga"), false);
        setField(R.id.fieldName, "Name/s:", intent.getStringExtra("name"), true);
        setField(R.id.fieldTradingName, "Trading Name:", intent.getStringExtra("tradingName"), true);
        setField(R.id.fieldPhone, "Phone:", intent.getStringExtra("phone"), true);
        setField(R.id.fieldLicence, "Licence Number:", intent.getStringExtra("licence"), true);
        setField(R.id.fieldRegistration, "Registration:", intent.getStringExtra("registration"), true);
        setField(R.id.fieldStatus, "Status:", intent.getStringExtra("status"), true);
        setField(R.id.fieldExpiry, "Expiry Date:", intent.getStringExtra("expiry"), true);
        setField(R.id.fieldDescription, "Description:", intent.getStringExtra("description"), true);
        setField(R.id.fieldVehicle, "Type of vehicle:", intent.getStringExtra("vehicle"), true);
        setField(R.id.fieldMake, "Make:", intent.getStringExtra("make"), true);
        setField(R.id.fieldModel, "Model:", intent.getStringExtra("model"), true);
        setField(R.id.fieldColour, "Colour:", intent.getStringExtra("colour"), true);
        setField(R.id.fieldOther1, "Other distinguishing features:", intent.getStringExtra("other1"), true);
        setField(R.id.fieldPrimaryLocation, "Primary location of vending machine:", intent.getStringExtra("primaryLocation"), true);
        setField(R.id.fieldSerial, "Serial number/ identification number/mark:", intent.getStringExtra("serial"), true);
        setField(R.id.fieldOther2, "Other distinguishing features:", intent.getStringExtra("other2"), true);
        Button btnEdit = findViewById(R.id.btnEdit);
        if (isPublic) {
            hide(R.id.fieldStatus);
            show(R.id.fieldExpiry);
            hide(R.id.fieldDescription);
            hide(R.id.fieldVehicle);
            hide(R.id.fieldMake);
            hide(R.id.fieldModel);
            hide(R.id.fieldColour);
            hide(R.id.fieldOther1);
            hide(R.id.fieldPrimaryLocation);
            hide(R.id.fieldSerial);
            hide(R.id.fieldOther2);
            if (btnEdit != null) btnEdit.setVisibility(View.GONE);
        } else {
            show(R.id.fieldStatus);
            show(R.id.fieldExpiry);
            show(R.id.fieldDescription);
            show(R.id.fieldVehicle);
            show(R.id.fieldMake);
            show(R.id.fieldModel);
            show(R.id.fieldColour);
            show(R.id.fieldOther1);
            show(R.id.fieldPrimaryLocation);
            show(R.id.fieldSerial);
            show(R.id.fieldOther2);
            if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
            if (btnEdit != null) btnEdit.setOnClickListener(v -> {
                Intent e = new Intent(this, EditDetails.class);
                e.putExtras(getIntent());
                startActivity(e);
            });
        }
    }

    //Handles deletion of vendors from Supabase
    public static void deleteVendor(Context ctx, String rowId, String recordLga, Runnable onSuccess) {
        if (rowId == null || rowId.isEmpty()) return;
        String token = UserAccount.get().getAccessToken();
        String role = UserAccount.get().getRole();
        String council = UserAccount.get().getCouncil();
        if (TextUtils.isEmpty(token)) return;
        if ("PUBLIC".equalsIgnoreCase(role)) return;
        boolean allowed = true;
        if ("COUNCIL".equalsIgnoreCase(role) && !TextUtils.isEmpty(council)) {
            String rec = recordLga == null ? "" : recordLga;
            boolean okExact = council.equals(rec);
            boolean okSlug = council.equalsIgnoreCase(slug(rec));
            boolean okDisplay = rec.equalsIgnoreCase(council.replace("-", " "));
            allowed = okExact || okSlug || okDisplay;
        }
        if (!allowed) return;
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/qh_register")
                .newBuilder()
                .addQueryParameter("id", "eq." + rowId)
                .build();
        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", ANON)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();
        new OkHttpClient().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {}
            @Override public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && onSuccess != null) {
                    new android.os.Handler(ctx.getMainLooper()).post(onSuccess);
                }
            }
        });
    }

    //Replaces unsafe text with hyphens
    private static String slug(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        t = t.replaceAll("[^a-z0-9]+", "-");
        t = t.replaceAll("^-+|-+$", "");
        return t;
    }

    //Displays vendor field and information attached
    private void setField(int id, String label, String value, boolean optional) {
        TextView tv = findViewById(id);
        if (tv == null) return;
        String v = clean(value);
        if (v.isEmpty() && optional) tv.setText(label + " ");
        else if (v.isEmpty()) tv.setText(label + " -");
        else tv.setText(label + " " + v);
    }

    //Cleans text, removes null spaces
    private String clean(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.equalsIgnoreCase("null") ? "" : t;
    }

    //Hides field from view
    private void hide(int id) {
        View v = findViewById(id);
        if (v != null) v.setVisibility(View.GONE);
    }

    //Makes a field visible
    private void show(int id) {
        View v = findViewById(id);
        if (v != null) v.setVisibility(View.VISIBLE);
    }
}
