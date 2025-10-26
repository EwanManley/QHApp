package com.example.qhapplicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class SearchFilters extends AppCompatActivity {
    private String role;
    private String council;
    private String email;

    //Sets up filter screen and buttons
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_filters_page);
        role = UserAccount.get().getRole();
        council = UserAccount.get().getCouncil();
        email = UserAccount.get().getEmail();
        Button btnGoTrading = findViewById(R.id.btnGoTrading);
        Button btnGoReg = findViewById(R.id.btnGoReg);
        Button btnGoLicence = findViewById(R.id.btnGoLicence);
        Button btnGoAll = findViewById(R.id.btnGoAll);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnGoHome = findViewById(R.id.btnGoHome);
        btnGoTrading.setOnClickListener(v -> startWithSession(SearchTrading.class));
        btnGoReg.setOnClickListener(v -> startWithSession(SearchRegistration.class));
        btnGoLicence.setOnClickListener(v -> startWithSession(SearchLicence.class));
        btnGoAll.setOnClickListener(v -> startWithSession(SearchAll.class));
        btnBack.setOnClickListener(v -> navigateToAuth());
        btnGoHome.setOnClickListener(v -> navigateToSplash());
        //Handles press of back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToAuth();
            }
        });
    }

    //Sends User back to log in screen
    private void navigateToAuth() {
        Intent i = new Intent(this, Authentication.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    //Sends user back to splash screen
    private void navigateToSplash() {
        Intent i = new Intent(this, MainLanding.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    //Opens search screen while retaining user info
    private void startWithSession(Class<?> target) {
        Intent i = new Intent(this, target);
        i.putExtra("role", role);
        i.putExtra("council", council);
        i.putExtra("email", email);
        startActivity(i);
    }
}
