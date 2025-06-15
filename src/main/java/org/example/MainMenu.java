// MainMenu.java
package org.example;
import javax.swing.*; // Required for JOptionPane to display dialogs

public class MainMenu {

    // An instance of MessageProcessor to handle all message-related operations.
    private static MessageProcessor messageProcessor = new MessageProcessor();

    public static void startApplication() {
        // Greet the user upon entering the QuickChat application.
        JOptionPane.showMessageDialog(null, "Welcome to the QuickChat Application!");

        // Loop indefinitely to keep the main menu running until explicitly exited.
        while (true) {
            // Display the main menu options to the user and get their choice.
            String choiceStr = JOptionPane.showInputDialog(null,
                    """
                    --- Main Menu ---
                    1. QuickChat Messaging
                    2. Display Senders and Recipients
                    3. Display Longest Message
                    4. Search Message by ID
                    5. Search Messages by Recipient
                    6. Delete Message by Hash
                    7. Display Full Message Report
                    8. Load Messages from JSON File
                    9. Logout/Exit
                    Choose an option:""",
                    "Main Menu", // Dialog title
                    JOptionPane.PLAIN_MESSAGE); // Message type

            // Handle cases where the user clicks 'Cancel' or closes the dialog.
            if (choiceStr == null) {
                JOptionPane.showMessageDialog(null, "Exiting application. Goodbye!");
                System.exit(0); // Terminate the application gracefully.
            }

            try {
                // Attempt to parse the user's input string into an integer.
                int choice = Integer.parseInt(choiceStr);

                // Use a switch statement to perform actions based on the user's choice.
                switch (choice) {
                    case 1 -> { // Option 1: QuickChat Messaging
                        // Start the QuickChat messaging interface, passing the messageProcessor.
                        Message.startQuickChat(messageProcessor);
                    }
                    case 2 -> { // Option 2: Display Senders and Recipients (formerly case 3)
                        // Display a report of all message senders and recipients.
                        messageProcessor.displaySentMessageSendersAndRecipients();
                    }
                    case 3 -> { // Option 3: Display Longest Message (formerly case 4)
                        // Find and display the longest message sent.
                        messageProcessor.displayLongestSentMessage();
                    }
                    case 4 -> { // Option 4: Search Message by ID (formerly case 5)
                        // Prompt for a Message ID and search for it.
                        String searchId = JOptionPane.showInputDialog("Enter Message ID to search:");
                        if (searchId != null && !searchId.trim().isEmpty()) {
                            messageProcessor.searchMessageById(searchId.trim());
                        } else {
                            JOptionPane.showMessageDialog(null, "No Message ID entered.", "Search Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    case 5 -> { // Option 5: Search Messages by Recipient (formerly case 6)
                        // Prompt for a recipient number and search for messages sent to them.
                        String searchRecipient = JOptionPane.showInputDialog("Enter Recipient number to search:");
                        if (searchRecipient != null && !searchRecipient.trim().isEmpty()) {
                            messageProcessor.searchMessagesByRecipient(searchRecipient.trim());
                        } else {
                            JOptionPane.showMessageDialog(null, "No Recipient entered.", "Search Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    case 6 -> { // Option 6: Delete Message by Hash (formerly case 7)
                        // Prompt for a message hash and attempt to delete the corresponding message.
                        String deleteHash = JOptionPane.showInputDialog("Enter Message Hash to delete:");
                        if (deleteHash != null && !deleteHash.trim().isEmpty()) {
                            messageProcessor.deleteMessageByHash(deleteHash.trim());
                        } else {
                            JOptionPane.showMessageDialog(null, "No Message Hash entered.", "Delete Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    case 7 -> { // Option 7: Display Full Message Report (formerly case 8)
                        // Display a comprehensive report of all sent messages.
                        messageProcessor.displaySentMessagesReport();
                    }
                    case 8 -> { // Option 8: Load Messages from JSON File (formerly case 9)
                        // Load messages from a JSON file into the message processor.
                        boolean loaded = messageProcessor.loadStoredMessagesFromJson();
                        if (loaded) {
                            JOptionPane.showMessageDialog(null, "Messages loaded from JSON file.");
                        } else {
                            JOptionPane.showMessageDialog(null, "No messages to load.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    case 9 -> { // Option 9: Logout (formerly case 10)
                        // Log out the user and exit the application.
                        JOptionPane.showMessageDialog(null, "Logging out. Goodbye!");
                        System.exit(0); // Terminate application process.
                    }
                    default -> { // Handle invalid numerical input
                        JOptionPane.showMessageDialog(null, "Invalid option. Please choose a number between 1 and 9.");
                    }
                }
            } catch (NumberFormatException e) {
                // Catch and handle cases where the user input is not a valid number.
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}