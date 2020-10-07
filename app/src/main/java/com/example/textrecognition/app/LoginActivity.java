package com.example.textrecognition.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.textrecognition.R;
import com.example.textrecognition.java.ChooserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextusername;
    private EditText editTextPassword;
    private FirebaseAuth mAuth;
    private Button bt;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        editTextusername = findViewById(R.id.email_address);
        editTextPassword = findViewById(R.id.password);
        bt = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressbar);
        ((EditText) findViewById(R.id.password)).setOnEditorActionListener
                ((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        loginOnClick(bt);
                        return true;
                    }
                    return false;
                });
    }

    public void loginOnClick(View view) {
        checkFields();
    }

    private void checkFields() {
         String username = editTextusername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        if (username.isEmpty()) {
            editTextusername.setError("username is required");
            editTextusername.requestFocus();
            return;
        }
        username=username+"@gmail.com";
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            editTextusername.setError("Please enter a valid user id");
            editTextusername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Minimum length of password should be 6");
            editTextPassword.requestFocus();
            return;
        }
        bt.setText("");

        progressBar.setVisibility(View.VISIBLE);
        String finalusername = username;
        mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SharedPreferences sharedPreferences = getSharedPreferences("CRED", MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("User",
                        finalusername);
                edit.putString("Password",
                        password);
                edit.apply();
                Intent intent = new Intent(LoginActivity.this, ChooserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                progressBar.setVisibility(View.GONE);
                bt.setText("Login");
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    Toast.makeText(getApplicationContext(), "username address or password is incorrect", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void registerUserClicked(View view) {
        startActivity(new Intent(this,SignupActivity.class));
    }
}
