package com.ssindher11.chirptut.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ssindher11.chirptut.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        (findViewById(R.id.btn_send)).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SendActivity.class)));
        (findViewById(R.id.btn_receive)).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReceiveActivity.class)));
    }
}
