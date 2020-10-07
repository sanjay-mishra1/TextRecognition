package com.example.textrecognition.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.textrecognition.java.ChooserActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_vision_entry_choice);
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(this, LoginActivity.class));
        }else {
            Intent intent = new Intent(MainActivity.this, ChooserActivity.class);
            startActivity(intent);
        }
        finish();

    }
}