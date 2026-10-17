package com.mycompany.chatapplication;

public class Login {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellPhoneNumber;

    private boolean loginSuccessful;

    // Constructor
    public Login(String firstName, String lastName, String username,
            String password, String cellPhoneNumber) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhoneNumber;
        this.loginSuccessful = false;
    }

    // Validates the username according to the assignment requirements.
    // The username must contain an underscore and be no more than five characters long.
    public boolean checkUserName() {

        return username != null
                && username.contains("_")
                && username.length() <= 5;
    }

    // Validates the password according to the assignment requirements.
    // The password must contain at least eight characters,
    // a capital letter, a number, and a special character.
    public boolean checkPasswordComplexity() {

        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasCapitalLetter = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (int i = 0; i < password.length(); i++) {

            char character = password.charAt(i);

            if (Character.isUpperCase(character)) {
                hasCapitalLetter = true;
            }

            if (Character.isDigit(character)) {
                hasNumber = true;
            }

            if (!Character.isLetterOrDigit(character)) {
                hasSpecialCharacter = true;
            }
        }

        return hasCapitalLetter
                && hasNumber
                && hasSpecialCharacter;
    }

    // Validates the cellphone number according to the assignment requirements.
    // The number must use the South African international format,
    // starting with +27 followed by nine digits.
    //
    // Regular expression reference:
    // Oracle Java Pattern documentation:
    // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
    public boolean checkCellPhoneNumber() {

        String phoneRegex = "^\\+27[0-9]{9}$";

        return cellPhoneNumber != null
                && cellPhoneNumber.matches(phoneRegex);
    }

    // Registers the user by checking the username,
    // password, and cellphone number requirements.
    // Returns a message explaining whether registration was successful.
        public String registerUser() {

        if (!checkUserName()) {

            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";

        } else if (!checkPasswordComplexity()) {

            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";

        } else if (!checkCellPhoneNumber()) {

            return "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.";

        } else {

            return "User successfully registered.";
        }
    }

    // Checks whether the entered username and password
    // match the details that were used during registration.
    // Returns true when the login is successful and false otherwise.
    public boolean loginUser(String enteredUsername,
            String enteredPassword) {

        if (enteredUsername != null
                && enteredPassword != null
                && enteredUsername.equals(username)
                && enteredPassword.equals(password)) {

            loginSuccessful = true;

        } else {

            loginSuccessful = false;
        }

        return loginSuccessful;
    }

    // Returns the login status message.
    public String returnLoginStatus() {

        if (loginSuccessful) {

            return "Welcome " + firstName + ", " + lastName
                    + " it is great to see you.";

        } else {

            return "Username or password incorrect, please try again.";
        }
    }

    // Getter for first name
    public String getFirstName() {
        return firstName;
    }

    // Getter for last name
    public String getLastName() {
        return lastName;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Getter for cellphone number
    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }
}
