package ru.jo4j.testexamples;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;


public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityRule = new ActivityScenarioRule<>(MainActivity.class);


    @Test
    public void checkLabelText() {
        onView(withId(R.id.status)).check(matches(withText("...")));
    }

    @Test
    public void checkButtonIsClickable() {
        onView(withId(R.id.check)).check(matches(isClickable()));
    }

    @Test
    public void checkLabelWithError() {
        onView(withId(R.id.check)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(R.string.validation_error)));
    }

    @Test
    public void checkLabelSuccess() {
        onView(withId(R.id.email)).perform(click(), replaceText("test@test.com"), pressBack());
        onView(withId(R.id.check)).perform(click());
        onView(withId(R.id.status)).check(matches(withText(R.string.success)));
    }
}

