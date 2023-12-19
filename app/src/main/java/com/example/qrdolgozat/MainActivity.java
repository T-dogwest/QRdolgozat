package com.example.qrdolgozat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private Button ButtonScan;
    private Button ButtonList;
    private TextView ScanedQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        ButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.setPrompt("QRCode Scanner by Csontos Tibor");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(false);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.initiateScan();
            }
        });

        ButtonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ScanedQR.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Nincs QR kód beolvasva", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("url", ScanedQR.getText().toString());
                editor.apply();
                Intent intent = new Intent(MainActivity.this, ListaAdatok_Activity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Vissza léptél az alkalmazásban", Toast.LENGTH_SHORT).show();
            } else {
                ScanedQR.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void init() {
        ButtonScan = findViewById(R.id.ButtonScan);
        ButtonList = findViewById(R.id.ButtonList);
        ScanedQR = findViewById(R.id.ScanedQR);
    }
}