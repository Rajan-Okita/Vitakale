package com.example.vitakale;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField, confirmPasswordField;
    private Button registerButton, loginPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerButton);
        loginPageButton = findViewById(R.id.loginPageButton);

        registerButton.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(Register.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(Register.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Toast.makeText(Register.this,
                                                        "Verification email sent! Please verify and login.",
                                                        Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(Register.this, Login.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(Register.this,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            handleRegistrationError(task.getException());
                        }
                    });
        });

        loginPageButton.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "This email is already registered.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            Toast.makeText(this, "Password is too weak. Use at least 6 characters.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Registration failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
