package com.example.tuke_aid;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Map extends AppCompatActivity {
    private ImageView image;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        image = findViewById(R.id.map);

        back = findViewById(R.id.btback);
        back.setOnClickListener(v -> {
            startActivity(new Intent(Map.this, Home.class));
            finish();
        });
    }
}
