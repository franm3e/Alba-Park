package com.example.javi.easypark;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Introduccion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduccion);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                salir();
            }
        },3500);

    }

    private void salir() {
        Intent i = new Intent(this, MapsActivity.class );
        startActivity(i);
        this.finish();
    }
}
