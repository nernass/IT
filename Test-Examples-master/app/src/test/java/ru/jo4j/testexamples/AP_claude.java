package ru.jo4j.testexamples;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityIntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testValidEmailIntegration() {
        // Test valid email flow
        String validEmail = "test@example.com";
        onView(withId(R.id.email))
                .perform(replaceText(validEmail));
        
        onView(withId(R.id.check))
                .perform(click());

        onView(withId(R.id.status))
                .check(matches(withText(R.string.success)));
    }

    @Test
    public void testInvalidEmailIntegration() {
        // Test invalid email flow
        String invalidEmail = "invalid.email";
        onView(withId(R.id.email))
                .perform(replaceText(invalidEmail));
        
        onView(withId(R.id.check))
                .perform(click());

        onView(withId(R.id.status))
                .check(matches(withText(R.string.validation_error)));
    }

    @Test
    public void testEmptyEmailIntegration() {
        // Test edge case with empty email
        onView(withId(R.id.email))
                .perform(replaceText(""));
        
        onView(withId(R.id.check))
                .perform(click());

        onView(withId(R.id.status))
                .check(matches(withText(R.string.validation_error)));
    }

    @Test
    public void testUIComponentsInteraction() {
        // Test complete UI flow with text field interaction
        String validEmail = "test@example.com";
        String sampleText = "Sample text";

        onView(withId(R.id.email))
                .perform(replaceText(validEmail));
        
        onView(withId(R.id.text))
                .perform(replaceText(sampleText));
        
        onView(withId(R.id.check))
                .perform(click());

        onView(withId(R.id.status))
                .check(matches(withText(R.string.success)));
        
        onView(withId(R.id.text))
                .check(matches(withText(sampleText)));
    }
}