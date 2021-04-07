package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ResultsActivity extends AppCompatActivity {
    public static final String EXTRA_SEARCH_VAL = "search_string";
    private String searchVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // When searching by text, get search string from intent
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(EXTRA_SEARCH_VAL))
            searchVal = intent.getStringExtra(EXTRA_SEARCH_VAL);
        TextView t = findViewById(R.id.searchString);
        t.setText(searchVal);
    }
}