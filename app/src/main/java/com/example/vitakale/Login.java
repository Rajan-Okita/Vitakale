package com.example.vitakale;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        loginButton.setOnClickListener(view -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Login failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        resetPasswordButton.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, ResetPassword.class);
            startActivity(intent);
        });
    }
}
