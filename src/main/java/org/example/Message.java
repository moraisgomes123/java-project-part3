//Message.java
package org.example;
import javax.swing.*;
import java.util.*;
import java.awt.Dimension;

public class Message {

    // A static reference to the MessageProcessor to manage all message data.
    private static MessageProcessor messageProcessor;
    // Tracks the maximum number of messages the user intends to send/store in the current session.
    private static int allowedMessagesInSession = 0;
    // Counts how many messages have been processed (sent or stored) in the current session.
    private static int messagesSentInSession = 0;

    public static void startQuickChat(MessageProcessor processor) {
        // Assign the passed MessageProcessor instance to the static variable.
        messageProcessor = processor;
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat Messaging!");

        // Reset session-specific counters each time the user enters QuickChat messaging
        // from the MainMenu to ensure a fresh session.
        allowedMessagesInSession = 0;
        messagesSentInSession = 0;

        // Load any previously stored messages from the JSON file.
        // This ensures that 'storedMessages' list in MessageProcessor is populated
        // with existing data when QuickChat starts.
        messageProcessor.loadStoredMessagesFromJson();

        // Main loop for the QuickChat menu.
        while (true) {
            // Display QuickChat menu options and get user's choice.
            String choiceStr = JOptionPane.showInputDialog(null,
                    """
                    --- QuickChat Menu ---
                    1. Send a new message
                    2. Show recently sent/stored/disregarded messages
                    3. Go back to main application menu
                    Choose an option (1-3):""",
                    "QuickChat Menu",
                    JOptionPane.PLAIN_MESSAGE);

            // Check if the user cancelled the dialog (e.g., clicked 'Cancel' or closed window).
            if (choiceStr == null) {
                JOptionPane.showMessageDialog(null, "Returning to Main Menu.");
                return; // Exit the startQuickChat method, returning control to MainMenu.
            }

            try {
                // Parse the user's string input into an integer.
                int choice = Integer.parseInt(choiceStr);

                // Handle the chosen option using a switch statement.
                switch (choice) {
                    case 1 -> { // Option 1: Send a new message
                        // --- Scenario 1: User has already reached their allowed message limit ---
                        // This check prevents sending more messages if the quota for the current session is met.
                        if (allowedMessagesInSession > 0 && messagesSentInSession == allowedMessagesInSession) {
                            JOptionPane.showMessageDialog(null, "Reached the limit of " + allowedMessagesInSession + " message(s). You cannot send more messages in this QuickChat session.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                            // Reset session variables and return to Main Menu after warning the user.
                            allowedMessagesInSession = 0; // Reset for next QuickChat entry
                            messagesSentInSession = 0;    // Reset
                            return; // Exit startQuickChat, go back to MainMenu.
                        }
                        // --- Scenario 2: User is starting a new message sending sequence ---
                        // If allowedMessagesInSession is 0, it means a new limit needs to be set
                        // (either first entry to QuickChat messaging or previous sequence was cancelled/completed).
                        if (allowedMessagesInSession == 0) {
                            String input = JOptionPane.showInputDialog("Enter how many new messages you wish to send/store *in this session*:");
                            if (input == null) { // User clicked cancel during limit input.
                                JOptionPane.showMessageDialog(null, "Message sending setup cancelled.");
                                continue; // Go back to the QuickChat menu (continue the while loop).
                            }
                            try {
                                int newLimit = Integer.parseInt(input);
                                if (newLimit <= 0) { // Ensure the limit is a positive number.
                                    JOptionPane.showMessageDialog(null, "Number of messages must be positive. Please try again.");
                                    continue; // Go back to the QuickChat menu (continue the while loop).
                                }
                                allowedMessagesInSession = newLimit; // Set the new limit.
                                messagesSentInSession = 0; // Reset the counter for the new limit.
                                JOptionPane.showMessageDialog(null, "You can now process " + allowedMessagesInSession + " message(s).");
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Invalid number. Please enter a valid integer for the limit.", "Input Error", JOptionPane.ERROR_MESSAGE);
                                continue; // Go back to the QuickChat menu (continue the while loop).
                            }
                        }
                        // --- Scenario 3: Processing messages within the current quota ---
                        // Loop to allow the user to send/store messages up to the 'allowedMessagesInSession' limit.
                        while (messagesSentInSession < allowedMessagesInSession) {
                            // Call the helper method to handle a single message's input and processing.
                            boolean cancelled = sendMessageProcedure(messagesSentInSession + 1);
                            if (!cancelled) { // If the message was successfully created (not cancelled by user).
                                messagesSentInSession++; // Increment the counter for processed messages.
                            } else {
                                // If the user cancelled message creation mid-way, exit this inner loop.
                                // Reset session variables so that the next time they choose '1',
                                // they are prompted for a new limit, ensuring a clean slate.
                                JOptionPane.showMessageDialog(null, "Message creation cancelled for current sequence. Returning to QuickChat menu.");
                                allowedMessagesInSession = 0; // Reset limit.
                                messagesSentInSession = 0;    // Reset count.
                                break; // Exit the 'while (messagesSentInSession < allowedMessagesInSession)' loop.
                            }
                        }

                        // --- Scenario 4: Quota has been met after processing messages ---
                        // This block executes if the 'while' loop above completed because the full quota was met.
                        if (allowedMessagesInSession > 0 && messagesSentInSession == allowedMessagesInSession) {
                            JOptionPane.showMessageDialog(null, "Reached the limit of " + allowedMessagesInSession + " message(s).", "Limit Reached", JOptionPane.INFORMATION_MESSAGE);
                            // Do NOT reset allowedMessagesInSession here or return to Main Menu immediately.
                            // The state (messagesSentInSession == allowedMessagesInSession) will persist,
                            // ensuring that if the user picks '1' again, Scenario 1 will correctly trigger.
                            continue; // Go back to the QuickChat Menu (continue the outer while loop).
                        }
                    }
                    case 2 -> { // Option 2: Show recently sent/stored messages (all types)
                        // Call a dedicated method to display all categories of messages.
                        showAllMessages();
                    }
                    case 3 -> { // Option 3: Go back to main application menu
                        JOptionPane.showMessageDialog(null, "Returning to Main Menu.");
                        return; // Exit the startQuickChat method, returning control to MainMenu.
                    }
                    default -> { // Handle invalid numerical input (not 1, 2, or 3).
                        JOptionPane.showMessageDialog(null, "Invalid option. Please choose a number between 1 and 3.");
                    }
                }
            } catch (NumberFormatException e) {
                // Catch and handle cases where the user's input for menu choice is not a valid number.
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static boolean sendMessageProcedure(int messageNumber) {
        // Create a MessageData utility instance for validation and hash generation.
        MessageData messageDataUtil = new MessageData();

        // Generate a unique 10-character message ID (e.g., MSG123456789).
        // It combines a fixed prefix "MSG", a 7-digit random part, and a 2-digit sequence number.
        long rawIdPart = 100_000_000L + (long) (Math.random() * 900_000_000L);
        String messageID = "MSG" + String.valueOf(rawIdPart).substring(0, 7) + String.format("%02d", messageNumber % 100);

        // --- Sender Input and Validation Loop ---
        String sender;
        while (true) {
            sender = JOptionPane.showInputDialog("Enter your name (sender):\n(Message #" + messageNumber + "/" + allowedMessagesInSession + ")");
            if (sender == null) { // User clicked Cancel.
                JOptionPane.showMessageDialog(null, "Message creation cancelled for message #" + messageNumber + ".");
                return true; // Indicate cancellation.
            }
            if (!sender.trim().isEmpty()) {
                break; // Valid sender, exit loop.
            }
            JOptionPane.showMessageDialog(null, "Sender name cannot be empty.");
        }

        // --- Recipient Input and Validation Loop ---
        String recipient;
        while (true) {
            recipient = JOptionPane.showInputDialog("Enter Recipient's international cell number (e.g., +27123456789, max 15 characters including +):\n(Message #" + messageNumber + "/" + allowedMessagesInSession + ")");
            if (recipient == null) { // User clicked Cancel.
                JOptionPane.showMessageDialog(null, "Message creation cancelled for message #" + messageNumber + ".");
                return true; // Indicate cancellation.
            }
            if (messageDataUtil.checkRecipientCell(recipient)) {
                break; // Valid recipient, exit loop.
            } else {
                JOptionPane.showMessageDialog(null, "Invalid number. Must include '+' and be 10-15 digits long.");
            }
        }

        // --- Message Text Input and Validation Loop ---
        String messageText;
        while (true) {
            messageText = JOptionPane.showInputDialog("Enter your message (max 250 characters):\n(Message #" + messageNumber + "/" + allowedMessagesInSession + ")");
            if (messageText == null) { // User clicked Cancel.
                JOptionPane.showMessageDialog(null, "Message creation cancelled for message #" + messageNumber + ".");
                return true; // Indicate cancellation.
            }
            // Validate if the message is empty or contains only whitespace.
            if (messageText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Message is empty. Please enter some text.");
                continue; // Re-prompt for message text.
            }
            if (messageText.length() <= 250) {
                break; // Valid message length, exit loop.
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a message of less than 250 characters.");
            }
        }

        // Create a unique message hash using the MessageData utility.
        String messageHash = messageDataUtil.createMessageHash(messageID, messageNumber, messageText);

        // Create a new MessageData.messageData object with the gathered details.
        // The status is initially empty and will be set based on user's action choice.
        MessageData.messageData newMessage = new MessageData.messageData(
                messageID, sender, recipient, messageText, messageHash, "");

        // --- Action Choice for the Message ---
        String actionChoiceStr = JOptionPane.showInputDialog(null,
                """
                Choose an action for this message:
                1. Send Message Now
                2. Discard Message
                3. Store Message for Later
                Your choice (1-3):""",
                "Message Options",
                JOptionPane.PLAIN_MESSAGE);

        // Handle cases where the user cancels or provides empty input for action choice.
        if (actionChoiceStr == null || actionChoiceStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No action selected. Message #" + messageNumber + " not saved.");
            return true; // Treat as cancellation.
        }

        try {
            int action = Integer.parseInt(actionChoiceStr);
            switch (action) {
                case 1 -> { // Option 1: Send Message Now
                    newMessage.setStatus("SENT"); // Set status to SENT.
                    messageProcessor.addSentMessage(newMessage); // Add to sent messages.
                    JOptionPane.showMessageDialog(null, "Message #" + messageNumber + " sent successfully.");
                }
                case 2 -> { // Option 2: Discard Message
                    newMessage.setStatus("DISREGARDED"); // Set status to DISREGARDED.
                    messageProcessor.addDisregardedMessage(newMessage); // Add to disregarded messages.
                    JOptionPane.showMessageDialog(null, "Message #" + messageNumber + " disregarded.");
                }
                case 3 -> { // Option 3: Store Message for Later
                    newMessage.setStatus("PENDING"); // Set status to PENDING.
                    messageProcessor.addStoredMessage(newMessage); // Add to stored messages (also saves to JSON).
                    JOptionPane.showMessageDialog(null, "Message #" + messageNumber + " stored for later.");
                }
                default -> { // Handle invalid numerical input for action.
                    JOptionPane.showMessageDialog(null, "Invalid choice. Message #" + messageNumber + " not saved.");
                    return true; // Treat as cancellation if invalid choice.
                }
            }
        } catch (NumberFormatException e) {
            // Catch and handle cases where action input is not a valid number.
            JOptionPane.showMessageDialog(null, "Invalid input for action. Message #" + messageNumber + " not saved.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return true; // Treat as cancellation if invalid input.
        }
        return false; // Message was successfully processed (not cancelled by the user).
    }

    private static void showAllMessages() {
        // Use StringBuilder for efficient string concatenation.
        StringBuilder sb = new StringBuilder("--- All Messages ---\n\n");

        // --- Display Sent Messages ---
        sb.append("==== Sent Messages ====\n");
        if (messageProcessor.getSentMessages().isEmpty()) {
            sb.append("No sent messages.\n");
        } else {
            for (MessageData.messageData msg : messageProcessor.getSentMessages()) {
                sb.append(msg.toString()).append("\n"); // Append each sent message's details.
            }
        }
        sb.append("-------------------------\n\n");

        // --- Display Stored Messages ---
        sb.append("==== Stored Messages ====\n");
        if (messageProcessor.getStoredMessages().isEmpty()) {
            sb.append("No stored messages.\n");
        } else {
            for (MessageData.messageData msg : messageProcessor.getStoredMessages()) {
                sb.append(msg.toString()).append("\n"); // Append each stored message's details.
            }
        }
        sb.append("-------------------------\n\n");

        // --- Display Disregarded Messages ---
        sb.append("==== Disregarded Messages ====\n");
        if (messageProcessor.getDisregardedMessages().isEmpty()) {
            sb.append("No disregarded messages.\n");
        } else {
            for (MessageData.messageData msg : messageProcessor.getDisregardedMessages()) {
                sb.append(msg.toString()).append("\n"); // Append each disregarded message's details.
            }
        }
        sb.append("-------------------------\n");

        // Create a JTextArea to hold the compiled message report.
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false); // Make the text area read-only.

        // Wrap the JTextArea in a JScrollPane to allow scrolling if content is long.
        JScrollPane scrollPane = new JScrollPane(textArea);
        // Set a preferred size for the scroll pane to ensure the dialog is appropriately sized.
        scrollPane.setPreferredSize(new Dimension(500, 400));

        // Display the scrollable text area in a JOptionPane dialog.
        JOptionPane.showMessageDialog(null, scrollPane, "All Messages", JOptionPane.INFORMATION_MESSAGE);
    }
}