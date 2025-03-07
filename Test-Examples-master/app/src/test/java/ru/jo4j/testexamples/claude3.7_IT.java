package ru.jo4j.testexamples;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testInitialUIComponents() {
        scenario.onActivity(activity -> {
            Button checkButton = activity.findViewById(R.id.check);
            EditText emailEditText = activity.findViewById(R.id.email);
            EditText textEditText = activity.findViewById(R.id.text);
            TextView statusTextView = activity.findViewById(R.id.status);

            assertNotNull(checkButton);
            assertNotNull(emailEditText);
            assertNotNull(textEditText);
            assertNotNull(statusTextView);
        });
    }

    @Test
    public void testEmailValidationSuccess() {
        scenario.onActivity(activity -> {
            EditText emailEditText = activity.findViewById(R.id.email);
            Button checkButton = activity.findViewById(R.id.check);
            TextView statusTextView = activity.findViewById(R.id.status);

            emailEditText.setText("test@example.com");
            checkButton.performClick();

            assertEquals(activity.getString(R.string.success), statusTextView.getText().toString());
        });
    }

    @Test
    public void testEmailValidationError() {
        scenario.onActivity(activity -> {
            EditText emailEditText = activity.findViewById(R.id.email);
            Button checkButton = activity.findViewById(R.id.check);
            TextView statusTextView = activity.findViewById(R.id.status);

            emailEditText.setText("invalid-email");
            checkButton.performClick();

            assertEquals(activity.getString(R.string.validation_error), statusTextView.getText().toString());
        });
    }
}