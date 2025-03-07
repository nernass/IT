package ru.jo4j.testexamples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TestFragment extends Fragment {
    private Button mCheck;
    private EditText mEmail, mText;
    private TextView mStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_fragment, container, false);
        mCheck = view.findViewById(R.id.check_fragment);
        mEmail = view.findViewById(R.id.email_fragment);
        mText = view.findViewById(R.id.text_fragment);
        mStatus = view.findViewById(R.id.status_fragment);
        mCheck.setOnClickListener(v -> {
            String email = mEmail.getText().toString();
            Validator<String> emailValidator = new EmailValidator();
            if(emailValidator.validate(email)) {
                mStatus.setText(R.string.success);
            } else {
                mStatus.setText(R.string.validation_error);
            }
        });
        return view;
    }
}
