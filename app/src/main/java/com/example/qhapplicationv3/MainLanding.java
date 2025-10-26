package com.example.qhapplicationv3;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainLanding extends AppCompatActivity {

    //Starts app and sets up screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_landing_page);
        //Loads saved user session if exists
        UserAccount.get().loadSavedSession(this);
        //Sends user to login if start button is pressed
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            Intent i = new Intent(this, Authentication.class);
            startActivity(i);
        });
        //Skip log in if user already signed in
        if (!isFinishing()) {
            String role = UserAccount.get().getRole();
            if (role != null) {
                startActivity(new Intent(this, SearchFilters.class));
                finish();
            }
        }
    }
}
