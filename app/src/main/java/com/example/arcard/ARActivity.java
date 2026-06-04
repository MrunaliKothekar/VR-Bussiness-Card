package com.example.arcard;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.database.*;

import java.util.Locale;

public class ARActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private TextToSpeech tts;

    private final String[] name = {"Loading..."};
    private final String[] phone = {"Loading..."};
    private final String[] email = {"Loading..."};
    private final String[] company = {"Loading..."};
    private final String[] website = {"Loading..."};
    private final String[] imageUrl = {""};

    private boolean dataLoaded = false;
    private boolean isCardPlaced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        // 🔊 TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arFragment);

        if (arFragment == null) {
            Toast.makeText(this, "AR Fragment error", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = getIntent().getStringExtra("id");

        if (userId == null) {
            Toast.makeText(this, "No ID received", Toast.LENGTH_LONG).show();
            return;
        }

        // 🔥 Firebase
        DatabaseReference db = FirebaseDatabase
                .getInstance("https://ar-business-card-69559-default-rtdb.firebaseio.com/")
                .getReference("users");

        db.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(ARActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                name[0] = safe(snapshot.child("name").getValue(String.class));
                phone[0] = safe(snapshot.child("phone").getValue(String.class));
                email[0] = safe(snapshot.child("email").getValue(String.class));
                company[0] = safe(snapshot.child("company").getValue(String.class));
                website[0] = safe(snapshot.child("website").getValue(String.class));
                imageUrl[0] = safe(snapshot.child("imageUrl").getValue(String.class));

                dataLoaded = true;

                Toast.makeText(ARActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ARActivity.this,
                        "DB Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        arFragment.setOnTapArPlaneListener(this::onTapPlane);
    }

    private void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        if (!dataLoaded) {
            Toast.makeText(this, "Loading data...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isCardPlaced) return;

        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // 🪪 CARD VIEW
        View cardView = getLayoutInflater().inflate(R.layout.ar_card_layout, null);

        TextView nameView = cardView.findViewById(R.id.name);
        TextView companyView = cardView.findViewById(R.id.company);
        TextView phoneView = cardView.findViewById(R.id.phone);
        TextView emailView = cardView.findViewById(R.id.email);
        TextView websiteView = cardView.findViewById(R.id.etWebsite);
        ImageView imageView = cardView.findViewById(R.id.profileImage);

        nameView.setText(name[0]);
        companyView.setText(company[0]);
        phoneView.setText(phone[0]);
        emailView.setText(email[0]);
        websiteView.setText(website[0]);

        // 🖼 Load image from URL
        Glide.with(this)
                .load(imageUrl[0])
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(imageView);

        ViewRenderable.builder()
                .setView(this, cardView)
                .build()
                .thenAccept(renderable -> {

                    Node node = new Node();
                    node.setParent(anchorNode);
                    node.setRenderable(renderable);
                    node.setLocalScale(new Vector3(0.8f, 0.8f, 0.8f));

                    isCardPlaced = true;

                    speakIntro(name[0], company[0], phone[0], website[0]);
                })
                .exceptionally(e -> {
                    Toast.makeText(this,
                            "Render error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void speakIntro(String name, String company, String phone, String website) {
        String intro = "Hello, I am " + name +
                ". I work at " + company +
                ". You can contact me at " + phone +
                ". My website is " + website;

        tts.speak(intro, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private String safe(String value) {
        return (value == null) ? "Not Available" : value;
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}