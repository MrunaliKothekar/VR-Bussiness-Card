package com.example.arcard;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.arcard.User;

public class FormActivity extends AppCompatActivity {

    EditText name, role, phone, email, company, website, imageUrl;
    Button btnQR, btnSave;
    ImageView qrImage;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        // Permission (optional now for modern Android)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }

        name = findViewById(R.id.etName);
        role = findViewById(R.id.etRole);
        phone = findViewById(R.id.etPhone);
        email = findViewById(R.id.etEmail);
        company = findViewById(R.id.etCompany);
        website = findViewById(R.id.etWebsite);
        imageUrl = findViewById(R.id.etImageUrl);

        btnQR = findViewById(R.id.btnGenerateQR);
        btnSave = findViewById(R.id.btnSaveQR);
        qrImage = findViewById(R.id.imgQR);

        // 🔥 GENERATE QR + SAVE TO FIREBASE
        btnQR.setOnClickListener(v -> {

            try {
                String userId = name.getText().toString()
                        .trim()
                        .replaceAll("\\s+", "_");

                DatabaseReference db = FirebaseDatabase
                        .getInstance("https://ar-business-card-69559-default-rtdb.firebaseio.com/")
                        .getReference("users");

                User user = new User(
                        name.getText().toString(),
                        role.getText().toString(),
                        phone.getText().toString(),
                        email.getText().toString(),
                        company.getText().toString(),
                        website.getText().toString(),
                        imageUrl.getText().toString()
                );

                db.child(userId).setValue(user);

                // QR generation
                MultiFormatWriter writer = new MultiFormatWriter();
                BitMatrix matrix = writer.encode(userId, BarcodeFormat.QR_CODE, 400, 400);

                BarcodeEncoder encoder = new BarcodeEncoder();
                bitmap = encoder.createBitmap(matrix);

                qrImage.setImageBitmap(bitmap);

                Toast.makeText(this, "QR Generated!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error generating QR", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔥 SAVE QR IMAGE
        btnSave.setOnClickListener(v -> {

            if (bitmap == null) {
                Toast.makeText(this, "Generate QR first!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String filename = "QR_" + System.currentTimeMillis() + ".png";

                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ARCard");

                android.net.Uri uri = getContentResolver().insert(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                );

                if (uri != null) {
                    java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();

                    Toast.makeText(this, "Saved in Gallery ✅", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Save failed ❌", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving QR ❌", Toast.LENGTH_SHORT).show();
            }
        });
    }
}