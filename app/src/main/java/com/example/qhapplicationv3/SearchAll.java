package com.example.qhapplicationv3;

import android.os.Bundle;

//Shows a list of all vendors, users can also search via any information
public class SearchAll extends SearchBase {
    @Override protected String getFieldKey() { return ""; }
    @Override protected String getScreenTitle() { return "All vendors"; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
