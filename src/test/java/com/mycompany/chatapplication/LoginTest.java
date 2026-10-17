package com.mycompany.chatapplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LoginTest {

    // USERNAME TESTS

    @Test
    public void usernameCorrectlyFormatted() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        assertTrue(login.checkUserName());
    }

    @Test
    public void usernameIncorrectlyFormatted() {

        Login login = new Login(
                "John",
                "Smith",
                "kyle123",
                "Password1!",
                "+27831234567"
        );

        assertFalse(login.checkUserName());
    }

    // PASSWORD TESTS

    @Test
    public void passwordMeetsComplexityRequirements() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        assertTrue(login.checkPasswordComplexity());
    }

    @Test
    public void passwordDoesNotMeetComplexityRequirements() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "password",
                "+27831234567"
        );

        assertFalse(login.checkPasswordComplexity());
    }

    // CELL PHONE TESTS

    @Test
    public void cellPhoneNumberCorrectlyFormatted() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        assertTrue(login.checkCellPhoneNumber());
    }

    @Test
    public void cellPhoneNumberIncorrectlyFormatted() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "0831234567"
        );

        assertFalse(login.checkCellPhoneNumber());
    }

    // REGISTRATION TEST

    @Test
    public void registrationSuccessful() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        assertEquals(
                "User successfully registered.",
                login.registerUser()
        );
    }

    // LOGIN TESTS

    @Test
    public void loginSuccessful() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        boolean result = login.loginUser(
                "kyl_1",
                "Password1!"
        );

        assertTrue(result);
    }

    @Test
    public void loginFailed() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        boolean result = login.loginUser(
                "wrong",
                "wrong"
        );

        assertFalse(result);
    }

    // LOGIN STATUS TESTS

    @Test
    public void successfulLoginStatus() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        login.loginUser(
                "kyl_1",
                "Password1!"
        );

        assertEquals(
                "Welcome John, Smith it is great to see you.",
                login.returnLoginStatus()
        );
    }

    @Test
    public void failedLoginStatus() {

        Login login = new Login(
                "John",
                "Smith",
                "kyl_1",
                "Password1!",
                "+27831234567"
        );

        login.loginUser(
                "wrong",
                "wrong"
        );

        assertEquals(
                "Username or password incorrect, please try again.",
                login.returnLoginStatus()
        );
    }
}