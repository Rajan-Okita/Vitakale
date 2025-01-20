package com.example.vitakale;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email);
        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(ResetPassword.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPassword.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPassword.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ResetPassword.this, "Error sending reset email! Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
