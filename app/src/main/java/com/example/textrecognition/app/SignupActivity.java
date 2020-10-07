package com.example.textrecognition.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.textrecognition.R;
import com.example.textrecognition.java.ChooserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private EditText useridEdit;
    private EditText nameEdit;
    private EditText passwordEdit;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        useridEdit=findViewById(R.id.userid);
        nameEdit=findViewById(R.id.user_name);
        passwordEdit=findViewById(R.id.password);
        button=findViewById(R.id.buttonLogin);
    }

    public void loginOnClick(View view) {
        if (useridEdit.getText().toString().trim().isEmpty()){
            showMessage("Enter a unique id");
            useridEdit.requestFocus();
            return;
        }
        if (nameEdit.getText().toString().trim().isEmpty()){
            showMessage("Enter your full name");
            nameEdit.requestFocus();
            return;
        }
        if (passwordEdit.getText().toString().trim().isEmpty()){
            showMessage("Enter valid password");
            nameEdit.requestFocus();
            return;
        }

        if (passwordEdit.getText().toString().trim().length()<6){
            showMessage("Password length should be more than 7");
            passwordEdit.requestFocus();
            return;
        }
        String userid=useridEdit.getText().toString().trim()+"@gmail.com";
        if (!Patterns.EMAIL_ADDRESS.matcher(userid).matches()) {
            useridEdit.setError("Please enter a valid user id");
            useridEdit.requestFocus();
            return;
        }
        registerUser(userid);
    }

    private void registerUser(String userid) {
        button.setText("");
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        String password=passwordEdit.getText().toString().trim();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(userid,password).addOnSuccessListener(authResult -> {
            SharedPreferences sharedPreferences = getSharedPreferences("CRED", MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("User",
                    userid);
            edit.putString("Password",
                    password);
            edit.apply();
            UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
           profileUpdates.setDisplayName(nameEdit.getText().toString().trim());
            // profileUpdates.build();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.updateProfile(profileUpdates.build())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Settings", "User profile updated.");
                        }else
                            Log.d("Settings", "User profile update failed.");

                    });
            Intent intent = new Intent(SignupActivity.this, ChooserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            if (e instanceof FirebaseAuthUserCollisionException)
            {
                showMessage("User id already exist");
                useridEdit.requestFocus();
            }else{
                Log.e("Register","Error");
                e.printStackTrace();
            }
            button.setText("Register");
            findViewById(R.id.progressbar).setVisibility(View.GONE);
        });
    }

    private void showMessage(String s) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}