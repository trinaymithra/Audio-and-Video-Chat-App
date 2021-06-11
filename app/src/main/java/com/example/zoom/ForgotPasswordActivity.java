package com.example.zoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail;
    private Button btnClick;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();


        initView();
        clickListener();
    }

    private void clickListener() {
        btnClick.setOnClickListener(this);
    }

    private void initView() {
        editTextEmail = findViewById(R.id.editTextEmail);
        btnClick = findViewById(R.id.btnClick);
    }

    @Override
    public void onClick(View v) {
        if(editTextEmail.getText().toString().isEmpty())
            Toast.makeText(this, "Enter the email address", Toast.LENGTH_SHORT).show();
        else if (!isValidEmail(editTextEmail.getText().toString()))
            Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show();
        else {
            mAuth.sendPasswordResetEmail(editTextEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ForgotPasswordActivity.this, "Check your email for reset password link", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                    else
                        Toast.makeText(ForgotPasswordActivity.this, "Error occured. Please try again" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static boolean isValidEmail(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

}