package org.example;

import javax.swing.JOptionPane; // Required for displaying dialog boxes to the user

public class Login {
    // --- Attributes ---
    // Private instance variables to store user details.
    // These are initialized via the constructor or setters based on user input.
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellNumber;


    public Login(String firstName, String lastName, String username, String password, String cellNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellNumber = cellNumber;
    }

    // --- Getters ---
    // These methods provide read-only access to the private instance variables.

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCellNumber() {
        return cellNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    // --- Setters ---
    // These methods allow modification of the private instance variables.

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCellNumber(String cellNumber) {
        this.cellNumber = cellNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // --- Validation Methods ---

    public boolean checkUserName() {
        // Checks if the username contains '_' AND its length is 5 characters or less.
        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity() {
        // Combined checks using regular expressions for character types and basic length check.
        return password.length() >= 8 &&          // Check minimum length
                password.matches(".*[A-Z].*") &&   // Check for at least one uppercase letter
                password.matches(".*[a-z].*") &&   // Check for at least one lowercase letter
                password.matches(".*[0-9].*") &&   // Check for at least one digit
                password.matches(".*[!@#$%^&*()].*"); // Check for at least one special character
    }

    public boolean checkCellNumber() {
        // Uses a regular expression to match "+27" followed by 9 digits OR "0" followed by 9 digits.
        return cellNumber.matches("^(\\+27|0)\\d{9}$");
    }

    // --- Core Functionality Methods ---

    public String registerUser() {
        // Prompt for and set user's first name
        setFirstName(JOptionPane.showInputDialog("Enter first name:"));
        // Prompt for and set user's last name
        setLastName(JOptionPane.showInputDialog("Enter last name:"));
        // Prompt for and set user's cell number with format example
        setCellNumber(JOptionPane.showInputDialog("Enter cell number (e.g., +27XXXXXXXXX or 0XXXXXXXXX):"));
        // Prompt for and set username with specific rules
        setUsername(JOptionPane.showInputDialog("Enter username (must contain '_' and be <= 5 characters):"));
        // Prompt for and set password with complexity rules
        setPassword(JOptionPane.showInputDialog("Enter password (must be >= 8 chars, include uppercase, lowercase, number, special character):"));

        // Validate each input and return an appropriate message
        if (!checkUserName()) {
            return "Invalid username. Must contain an underscore(_) and be no more than 5 characters.";
        } else if (!checkPasswordComplexity()) {
            return "Weak password. Must be at least 8 characters long and include uppercase, lowercase, number, and a special character.";
        } else if (!checkCellNumber()) {
            return "Invalid South African cell number. Must be in format +27XXXXXXXXX or 0XXXXXXXXX.";
        } else {
            // If all validations pass, registration is successful.
            return "Registration successful!";
        }
    }
    public boolean loginUser() {

        JOptionPane.showMessageDialog(null,"===Login===");
        // Get username input from the user
        String inputUsername = JOptionPane.showInputDialog("Enter username:");
        // Get password input from the user
        String inputPassword = JOptionPane.showInputDialog("Enter password:");

        // Compare the entered credentials with the attributes of this Login object.
        return inputUsername.equals(username) && inputPassword.equals(password);
    }
    public String returnLoginStatus(boolean loginSuccess) {
        if (loginSuccess) {
            // Return a personalized welcome message upon successful login.
            return "Login successful. Welcome, " + firstName + " " + lastName + "!";
        } else {
            // Return a generic error message for failed login attempts.
            return "Login failed. Please check your username and password.";
        }
    }
}