package org.example;

import javax.swing.*; // Required for JOptionPane to display dialogs

public class Main {

    public static void main(String[] args) {
        // Create an instance of the Login class.
        // Initial values for username, password, etc., are empty strings
        // as they will be populated by user input during the registration process.
        Login user = new Login("", "", "", "", "");

        // --- User Registration Section ---
        // Display a message indicating the start of the registration process.
        JOptionPane.showMessageDialog(null, "=== Registration ===");

        // Attempt to register a new user.
        // The registerUser() method handles input prompts and validation.
        String registrationMessage = user.registerUser();

        // Display the outcome of the registration attempt to the user.
        JOptionPane.showMessageDialog(null, registrationMessage);

        // --- Conditional Logic: Proceed based on Registration Success ---
        // Check if registration was successful. The message "Regist
        // ration successful!"
        // is used as a flag from the Login class's registerUser method.
        if (registrationMessage.contains("Registration successful!")) {

            // --- User Login Section (only if registration was successful) ---
            // Attempt to log in the user.
            // The loginUser() method handles input prompts and credential verification.
            boolean isSuccess = user.loginUser();


            // Display the login status (success or failure) to the user.
            JOptionPane.showMessageDialog(null, user.returnLoginStatus(isSuccess));

            // --- Conditional Logic: Launch MainMenu if Login is Successful ---
            // If login was successful, proceed to the main application menu.
            if (isSuccess) {
                MainMenu.startApplication(); // Calls a static method to start the MainMenu.
            } else {
                // Inform the user that QuickChat (implied application functionality)
                // cannot be launched due to login failure.
                JOptionPane.showMessageDialog(null, "Cannot proceed to QuickChat due to login failure.");
            }
        } else {
            // Inform the user that login cannot be attempted because registration failed.
            JOptionPane.showMessageDialog(null, "Cannot proceed to login due to registration failure.");
        }
    }
}
