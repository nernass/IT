package ru.jo4j.testexamples;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainActivityIntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testValidEmailIntegration() {
        // Test the success path with a valid email
        String validEmail = "test@example.com";

        // Input a valid email
        onView(withId(R.id.email))
                .perform(clearText(), typeText(validEmail), closeSoftKeyboard());

        // Click the check button
        onView(withId(R.id.check))
                .perform(click());

        // Verify the status text shows success
        onView(withId(R.id.status))
                .check(matches(withText(context.getString(R.string.success))));
    }

    @Test
    public void testInvalidEmailIntegration() {
        // Test the failure path with an invalid email
        String invalidEmail = "not-an-email";

        // Input an invalid email
        onView(withId(R.id.email))
                .perform(clearText(), typeText(invalidEmail), closeSoftKeyboard());

        // Click the check button
        onView(withId(R.id.check))
                .perform(click());

        // Verify the status text shows validation error
        onView(withId(R.id.status))
                .check(matches(withText(context.getString(R.string.validation_error))));
    }

    @Test
    public void testEmptyEmailIntegration() {
        // Test edge case with empty email

        // Clear the email field
        onView(withId(R.id.email))
                .perform(clearText(), closeSoftKeyboard());

        // Click the check button
        onView(withId(R.id.check))
                .perform(click());

        // Empty email should be invalid, verify status shows validation error
        onView(withId(R.id.status))
                .check(matches(withText(context.getString(R.string.validation_error))));
    }

    @Test
    public void testEmailValidationWithTextFieldInteraction() {
        // Test interaction between email field, text field and validation
        String validEmail = "user@domain.com";
        String someText = "Some sample text";

        // Input a valid email
        onView(withId(R.id.email))
                .perform(clearText(), typeText(validEmail), closeSoftKeyboard());

        // Input some text in the text field
        onView(withId(R.id.text))
                .perform(clearText(), typeText(someText), closeSoftKeyboard());

        // Click the check button
        onView(withId(R.id.check))
                .perform(click());

        // Verify status shows success for valid email
        onView(withId(R.id.status))
                .check(matches(withText(context.getString(R.string.success))));

        // Verify text field still contains the entered text
        onView(withId(R.id.text))
                .check(matches(withText(someText)));
    }

    @Test
    public void testComponentLifecycleIntegration() {
        // Test component interaction through activity lifecycle
        String validEmail = "lifecycle@test.com";

        // Input a valid email and validate
        onView(withId(R.id.email))
                .perform(clearText(), typeText(validEmail), closeSoftKeyboard());

        onView(withId(R.id.check))
                .perform(click());

        // Recreate the activity to test component state persistence
        activityRule.getScenario().recreate();

        // Validate email again after recreation
        onView(withId(R.id.check))
                .perform(click());

        // Status should still show success if components integrate properly
        onView(withId(R.id.status))
                .check(matches(withText(context.getString(R.string.success))));
    }
}