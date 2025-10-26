package com.example.qhapplicationv3;

import android.os.Bundle;

//Users can search vendors via registration number
public class SearchRegistration extends SearchBase {
    @Override protected String getFieldKey() { return "[* Registration number]"; }
    @Override protected String getScreenTitle() { return "Search by Registration Plate"; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
