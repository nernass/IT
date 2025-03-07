package ru.jo4j.testexamples;


import android.os.Build;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class IntegrationTest {
    @Test
    @Config(sdk = {Build.VERSION_CODES.P})
    public void checkStatusError() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        activity.findViewById(R.id.check).performClick();
        TextView label = activity.findViewById(R.id.status);
        String status = label.getText().toString();
        assertThat(status).isEqualTo("Validation error!!!");
    }

    @Test
    @Config(sdk = {Build.VERSION_CODES.P})
    public void checkStatusSuccess() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        EditText email = activity.findViewById(R.id.email);
        email.setText("test@test.com");
        activity.findViewById(R.id.check).performClick();
        TextView label = activity.findViewById(R.id.status);
        String status = label.getText().toString();
        assertThat(status).isEqualTo("Success");
    }

    @Test
    @Config(sdk = {Build.VERSION_CODES.P})
    public void checkStatus() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        TextView label = activity.findViewById(R.id.status);
        String status = label.getText().toString();
        assertThat(status).isEqualTo("Text");
    }
}
