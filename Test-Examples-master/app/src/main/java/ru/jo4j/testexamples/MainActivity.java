package ru.jo4j.testexamples;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button mCheck;
    private EditText mEmail, mText;
    private TextView mStatus;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mCheck = findViewById(R.id.check);
            mEmail = findViewById(R.id.email);
            mText = findViewById(R.id.text);
            mStatus = findViewById(R.id.status);
            mCheck.setOnClickListener(v -> {
                String email = mEmail.getText().toString();
                Validator<String> emailValidator = new EmailValidator();
                if(emailValidator.validate(email)) {
                    mStatus.setText(R.string.success);
                } else {
                    mStatus.setText(R.string.validation_error);
                }
            });
        }
}