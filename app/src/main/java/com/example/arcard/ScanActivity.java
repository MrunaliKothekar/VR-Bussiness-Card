package com.example.arcard;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.*;

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startQRScanner();
    }

    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan your card");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);

        ScanContract contract = new ScanContract();
        registerForActivityResult(contract, result -> {
            if (result.getContents() != null) {

                String userId = result.getContents(); // ✅ ONLY ID

                Intent intent = new Intent(this, ARActivity.class);
                intent.putExtra("id", userId);
                startActivity(intent);
                finish();

            } else {
                finish();
            }
        }).launch(options);
    }
}