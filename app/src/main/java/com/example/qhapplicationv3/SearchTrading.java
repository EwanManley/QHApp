package com.example.qhapplicationv3;

import android.os.Bundle;

//Users can search vendors via Trading Name.
public class SearchTrading extends SearchBase {
    @Override protected String getFieldKey() { return "[* Trading name]"; }
    @Override protected String getScreenTitle() { return "Search by Trading Name"; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
