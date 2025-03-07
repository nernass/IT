package ru.jo4j.testexamples;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@ExtendWith(MockitoExtension.class)
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {

    private MainActivity activity;

    @Mock
    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        activity = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    void shouldShowSuccessMessageWhenEmailIsValid() {
        // Arrange
        EditText emailField = activity.findViewById(R.id.email);
        TextView statusText = activity.findViewById(R.id.status);
        Button checkButton = activity.findViewById(R.id.check);

        String validEmail = "test@example.com";
        when(emailValidator.validate(validEmail)).thenReturn(true);

        // Act
        emailField.setText(validEmail);
        checkButton.performClick();

        // Assert
        assertEquals(activity.getString(R.string.success), statusText.getText().toString());
    }

    @Test
    void shouldShowErrorMessageWhenEmailIsInvalid() {
        // Arrange
        EditText emailField = activity.findViewById(R.id.email);
        TextView statusText = activity.findViewById(R.id.status);
        Button checkButton = activity.findViewById(R.id.check);

        String invalidEmail = "invalid-email";
        when(emailValidator.validate(invalidEmail)).thenReturn(false);

        // Act
        emailField.setText(invalidEmail);
        checkButton.performClick();

        // Assert
        assertEquals(activity.getString(R.string.validation_error), statusText.getText().toString());
    }

    @Test
    void shouldInitializeViewsCorrectly() {
        assertNotNull(activity.findViewById(R.id.email));
        assertNotNull(activity.findViewById(R.id.text));
        assertNotNull(activity.findViewById(R.id.status));
        assertNotNull(activity.findViewById(R.id.check));
    }

    @Test
    void shouldPreserveStateOnConfigurationChange() {
        // Arrange
        EditText emailField = activity.findViewById(R.id.email);
        String testEmail = "test@example.com";

        // Act
        emailField.setText(testEmail);
        activity.recreate();

        // Assert
        assertEquals(testEmail, ((EditText) activity.findViewById(R.id.email)).getText().toString());
    }
}