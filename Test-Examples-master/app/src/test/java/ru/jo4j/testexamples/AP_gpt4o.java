import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityIntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private Validator<String> emailValidatorMock;

    @Before
    public void setUp() {
        emailValidatorMock = Mockito.mock(EmailValidator.class);
    }

    @Test
    public void testSuccessPath() {
        when(emailValidatorMock.validate("test@example.com")).thenReturn(true);

        ActivityScenario<MainActivity> scenario = activityScenarioRule.getScenario();
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
    public void testFailurePath() {
        when(emailValidatorMock.validate("invalid-email")).thenReturn(false);

        ActivityScenario<MainActivity> scenario = activityScenarioRule.getScenario();
        scenario.onActivity(activity -> {
            EditText emailEditText = activity.findViewById(R.id.email);
            Button checkButton = activity.findViewById(R.id.check);
            TextView statusTextView = activity.findViewById(R.id.status);

            emailEditText.setText("invalid-email");
            checkButton.performClick();

            assertEquals(activity.getString(R.string.validation_error), statusTextView.getText().toString());
        });
    }

    @Test
    public void testEdgeCaseEmptyInput() {
        when(emailValidatorMock.validate("")).thenReturn(false);

        ActivityScenario<MainActivity> scenario = activityScenarioRule.getScenario();
        scenario.onActivity(activity -> {
            EditText emailEditText = activity.findViewById(R.id.email);
            Button checkButton = activity.findViewById(R.id.check);
            TextView statusTextView = activity.findViewById(R.id.status);

            emailEditText.setText("");
            checkButton.performClick();

            assertEquals(activity.getString(R.string.validation_error), statusTextView.getText().toString());
        });
    }
}