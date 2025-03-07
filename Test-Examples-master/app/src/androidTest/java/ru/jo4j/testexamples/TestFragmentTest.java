package ru.jo4j.testexamples;

import com.android21buttons.fragmenttestrule.FragmentTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class TestFragmentTest {
    @Rule
    public FragmentTestRule<?, TestFragment> fragmentTestRule =
            FragmentTestRule.create(TestFragment.class);

    @Test
    public void checkLabelText() {
        onView(withId(R.id.status_fragment)).check(matches(withText("...")));
    }

    @Test
    public void checkButtonIsClickable() {
        onView(withId(R.id.check_fragment)).check(matches(isClickable()));
    }

    @Test
    public void checkLabelWithError() {
        onView(withId(R.id.check_fragment)).perform(click());
        onView(withId(R.id.status_fragment)).check(matches(withText(R.string.validation_error)));
    }

    @Test
    public void checkLabelSuccess() {
        onView(withId(R.id.email_fragment)).perform(click(), replaceText("test@test.com"), pressBack());
        onView(withId(R.id.check_fragment)).perform(click());
        onView(withId(R.id.status_fragment)).check(matches(withText(R.string.success)));
    }
}
