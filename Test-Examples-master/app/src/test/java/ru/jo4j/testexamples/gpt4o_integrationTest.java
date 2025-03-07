import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testEmailValidationSuccess() {
        scenario.onActivity(activity -> {
            EditText emailField = activity.findViewById(R.id.email);
            Button checkButton = activity.findViewById(R.id.check);
            TextView statusView = activity.findViewById(R.id.status);

            emailField.setText("test@example.com");
            checkButton.performClick();

            assertEquals(activity.getString(R.string.success), statusView.getText().toString());
        });
    }

    @Test
    public void testEmailValidationError() {
        scenario.onActivity(activity -> {
            EditText emailField = activity.findViewById(R.id.email);
            Button checkButton = activity.findViewById(R.id.check);
            TextView statusView = activity.findViewById(R.id.status);

            emailField.setText("invalid-email");
            checkButton.performClick();

            assertEquals(activity.getString(R.string.validation_error), statusView.getText().toString());
        });
    }
}