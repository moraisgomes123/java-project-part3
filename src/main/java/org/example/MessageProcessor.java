// MessageProcessor.java
package org.example;

import org.json.JSONArray;   // Used for handling JSON arrays (though not directly in current version)
import org.json.JSONException; // Exception for issues with JSON parsing
import org.json.JSONObject;  // Used for handling JSON objects

import javax.swing.*; // Required for JOptionPane for dialogs and JTextArea, JScrollPane
import java.awt.*;    // Specifically imported for Dimension class, used with JScrollPane
import java.io.File;        // Added for checking file existence
import java.io.FileReader; // For reading data from a file
import java.io.FileWriter; // For writing data to a file
import java.io.IOException; // Exception for I/O operations
import java.util.ArrayList; // For dynamic lists
import java.util.Iterator;  // For safe removal during iteration
import java.util.List;      // Interface for list collections
import java.util.Optional;  // For handling potential absence of search results

public class MessageProcessor {

    // --- Message Storage Lists ---
    // These lists hold MessageData.messageData objects based on their status.
    private List<MessageData.messageData> sentMessages;       // Messages that have been "sent"
    private List<MessageData.messageData> disregardedMessages; // Messages that were "discarded"
    private List<MessageData.messageData> storedMessages;     // Messages "stored for later" (also persisted to JSON)

    // --- Helper Lists for Quick Lookups/Management ---
    // These lists are used to quickly check for existing hashes or IDs across all message types.
    private List<String> messageHashes; // Stores unique hashes of messages for quick lookup/deletion.
    private List<String> messageIDs;    // Stores unique IDs of messages for quick lookup.

    public MessageProcessor() {
        this.sentMessages = new ArrayList<>();
        this.disregardedMessages = new ArrayList<>();
        this.storedMessages = new ArrayList<>();
        this.messageHashes = new ArrayList<>();
        this.messageIDs = new ArrayList<>();
    }

    // --- Methods to Add Messages to Respective Lists ---

    public void addSentMessage(MessageData.messageData msg) {
        sentMessages.add(msg);
        messageHashes.add(msg.getHash());
        messageIDs.add(msg.getId());
    }

    public void addDisregardedMessage(MessageData.messageData msg) {
        disregardedMessages.add(msg);
    }

    public void addStoredMessage(MessageData.messageData msg) {
        storedMessages.add(msg);
        messageHashes.add(msg.getHash());
        messageIDs.add(msg.getId());
        saveMessageToJsonFile(msg); // Persist to file immediately
    }

    // --- JSON File Handling Methods ---

    private void saveMessageToJsonFile(MessageData.messageData msg) {
        JSONObject json = new JSONObject();
        json.put("id", msg.getId());
        json.put("recipient", msg.getRecipient());
        json.put("messageText", msg.getMessageText());
        json.put("hash", msg.getHash());
        json.put("status", msg.getStatus());

        try (FileWriter file = new FileWriter("storedMessages.json", true)) { // 'true' enables append mode
            file.write(json.toString() + System.lineSeparator()); // Write JSON object followed by a new line
        } catch (IOException e) {
            // Display an error message if there's an issue writing to the file.
            JOptionPane.showMessageDialog(null, "Error saving message to JSON: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads stored messages from the "storedMessages.json" file.
     * Clears existing stored messages before loading to prevent duplicates.
     *
     * @return true if messages were successfully loaded, false otherwise (e.g., file not found or empty).
     */
    public boolean loadStoredMessagesFromJson() {
        // Clear existing data to ensure a fresh load and prevent duplicate entries.
        storedMessages.clear();
        messageHashes.clear(); // Clear hashes related to previously stored data
        messageIDs.clear();    // Clear IDs related to previously stored data

        File jsonFile = new File("storedMessages.json");
        if (!jsonFile.exists() || jsonFile.length() == 0) { // Check if file exists and is not empty
            // JOptionPane.showMessageDialog(null, "No existing stored messages file found or file is empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return false; // Indicate no messages to load
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            StringBuilder jsonContent = new StringBuilder();
            int character;
            // Read the entire file content into a StringBuilder.
            while ((character = reader.read()) != -1) {
                jsonContent.append((char) character);
            }

            // Split the content into individual JSON strings based on line separators.
            String[] lines = jsonContent.toString().split(System.lineSeparator());
            boolean messagesFound = false; // Flag to track if any valid messages are loaded
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue; // Skip any empty lines in the file.
                }
                try {
                    // Parse each line as a JSONObject.
                    JSONObject json = new JSONObject(line);
                    // Extract message attributes from the JSON object.
                    String id = json.getString("id");
                    String recipient = json.getString("recipient");
                    String messageText = json.getString("messageText");
                    String hash = json.getString("hash");
                    String status = json.getString("status");

                    // Create a new messageData object and add it to the list.
                    MessageData.messageData msg = new MessageData.messageData(id, recipient, messageText, hash, status);
                    storedMessages.add(msg);
                    messageHashes.add(hash); // Add to global hash list
                    messageIDs.add(id);      // Add to global ID list
                    messagesFound = true;    // Set flag to true if at least one message is loaded
                } catch (JSONException e) {
                    // Log an error for malformed JSON lines but continue processing others.
                    System.err.println("Skipping malformed JSON line in storedMessages.json: " + line + " Error: " + e.getMessage());
                }
            }
            return messagesFound; // Return true if any messages were loaded, false otherwise
        } catch (IOException e) {
            // Inform the user if there's an error reading it.
            JOptionPane.showMessageDialog(null, "Error reading stored messages file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void rewriteStoredMessagesJson() {
        try (FileWriter file = new FileWriter("storedMessages.json")) { // No append mode (false by default), overwrites file
            for (MessageData.messageData msg : storedMessages) {
                // Convert each messageData object back to a JSONObject.
                JSONObject json = new JSONObject();
                json.put("id", msg.getId());
                json.put("recipient", msg.getRecipient());
                json.put("messageText", msg.getMessageText());
                json.put("hash", msg.getHash());
                json.put("status", msg.getStatus());
                // Write the JSON object followed by a new line.
                file.write(json.toString() + System.lineSeparator());
            }
        } catch (IOException e) {
            // Display an error message if rewriting the file fails.
            JOptionPane.showMessageDialog(null, "Error rewriting JSON file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Message Display and Query Methods ---

    public void displaySentMessageSendersAndRecipients() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages to display.", "Sent Messages", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("--- Sent Messages (Sender: You) ---\n");
        for (MessageData.messageData msg : sentMessages) {
            sb.append("Recipient: ").append(msg.getRecipient())
                    .append(", Message ID: ").append(msg.getId())
                    .append("\n");
        }
        // Use JTextArea and JScrollPane for better display of potentially long content.
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false); // Make text area read-only.
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300)); // Set preferred size for the dialog.
        JOptionPane.showMessageDialog(null, scrollPane, "Sent Messages Detail", JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayLongestSentMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages to analyze for longest message.", "Longest Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MessageData.messageData longestMessage = null;
        int maxLength = -1; // Initialize with -1 to correctly handle the first message

        // Iterate through all sent messages to find the one with the greatest text length.
        for (MessageData.messageData msg : sentMessages) {
            if (msg.getMessageText().length() > maxLength) {
                maxLength = msg.getMessageText().length();
                longestMessage = msg;
            }
        }

        if (longestMessage != null) {
            // Display details of the longest message found.
            JOptionPane.showMessageDialog(null,
                    "--- Longest Sent Message ---\n" +
                            "ID: " + longestMessage.getId() + "\n" +
                            "Recipient: " + longestMessage.getRecipient() + "\n" +
                            "Message: " + longestMessage.getMessageText() + "\n" +
                            "Length: " + maxLength + " characters",
                    "Longest Sent Message", JOptionPane.INFORMATION_MESSAGE);
        }
        // No else needed here, as the initial isEmpty() check covers the no messages case.
    }

    public void searchMessageById(String messageId) {
        // Use Optional to safely handle cases where a message might not be found.
        Optional<MessageData.messageData> foundMsg = Optional.empty();

        // Search in sentMessages first.
        foundMsg = sentMessages.stream()
                .filter(msg -> msg.getId().equals(messageId))
                .findFirst();

        // If not found in sentMessages, search in disregardedMessages.
        if (foundMsg.isEmpty()) {
            foundMsg = disregardedMessages.stream()
                    .filter(msg -> msg.getId().equals(messageId))
                    .findFirst();
        }
        // If still not found, search in storedMessages.
        if (foundMsg.isEmpty()) {
            foundMsg = storedMessages.stream()
                    .filter(msg -> msg.getId().equals(messageId))
                    .findFirst();
        }

        // Display the result based on whether the message was found.
        if (foundMsg.isPresent()) {
            MessageData.messageData msg = foundMsg.get();
            JOptionPane.showMessageDialog(null,
                    "--- Message Details (ID: " + messageId + ") ---\n" +
                            "Recipient: " + msg.getRecipient() + "\n" +
                            "Message: " + msg.getMessageText() + "\n" +
                            "Hash: " + msg.getHash() + "\n" + // Display the message hash
                            "Status: " + msg.getStatus(),     // Display the message status
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Message with ID '" + messageId + "' not found.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void searchMessagesByRecipient(String recipient) {
        StringBuilder sb = new StringBuilder("--- Messages to Recipient: " + recipient + " ---\n");
        boolean found = false; // Flag to check if any messages were found.

        // Search through sent messages.
        for (MessageData.messageData msg : sentMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("ID: ").append(msg.getId()).append(", Message: '").append(msg.getMessageText()).append("', Status: ").append(msg.getStatus()).append("\n");
                found = true;
            }
        }
        // Search through disregarded messages.
        for (MessageData.messageData msg : disregardedMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("ID: ").append(msg.getId()).append(", Message: '").append(msg.getMessageText()).append("', Status: ").append(msg.getStatus()).append("\n");
                found = true;
            }
        }
        // Search through stored messages.
        for (MessageData.messageData msg : storedMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("ID: ").append(msg.getId()).append(", Message: '").append(msg.getMessageText()).append("', Status: ").append(msg.getStatus()).append("\n");
                found = true;
            }
        }

        // Display results or a "not found" message.
        if (found) {
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No messages found for recipient '" + recipient + "'.", "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean deleteMessageByHash(String messageHash) {
        boolean removed = false;

        // --- Important: Create temporary lists to avoid ConcurrentModificationException ---
        // Iterating and modifying the same list can cause issues. We modify temporary lists,
        // then reassign them to the original lists if a change occurs.
        List<MessageData.messageData> tempSent = new ArrayList<>(sentMessages);
        List<MessageData.messageData> tempDisregarded = new ArrayList<>(disregardedMessages);
        List<MessageData.messageData> tempStored = new ArrayList<>(storedMessages);

        // --- Remove from sent messages ---
        // Use an Iterator for safe removal during iteration.
        Iterator<MessageData.messageData> sentIterator = tempSent.iterator();
        while (sentIterator.hasNext()) {
            MessageData.messageData msg = sentIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                sentIterator.remove(); // Remove from temporary list.
                messageIDs.remove(msg.getId());   // Remove from global ID tracking.
                messageHashes.remove(msg.getHash()); // Remove from global hash tracking.
                removed = true;
                JOptionPane.showMessageDialog(null, "Message with hash '" + messageHash + "' deleted from sent messages.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                this.sentMessages = tempSent; // Update the original list with the modified temporary list.
                return true; // Exit immediately upon successful deletion.
            }
        }

        // --- Remove from disregarded messages ---
        Iterator<MessageData.messageData> disregardedIterator = tempDisregarded.iterator();
        while (disregardedIterator.hasNext()) {
            MessageData.messageData msg = disregardedIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                disregardedIterator.remove(); // Remove from temporary list.
                // Hashes/IDs for disregarded messages are typically not in global tracking lists,
                // so no need to remove from messageIDs/messageHashes here.
                removed = true;
                JOptionPane.showMessageDialog(null, "Message with hash '" + messageHash + "' deleted from disregarded messages.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                this.disregardedMessages = tempDisregarded; // Update the original list.
                return true; // Exit immediately upon successful deletion.
            }
        }

        // --- Remove from stored messages and update JSON file ---
        Iterator<MessageData.messageData> storedIterator = tempStored.iterator();
        while (storedIterator.hasNext()) {
            MessageData.messageData msg = storedIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                storedIterator.remove(); // Remove from temporary list.
                messageHashes.remove(msg.getHash()); // Remove from global hash tracking.
                messageIDs.remove(msg.getId());      // Remove from global ID tracking.
                this.storedMessages = tempStored; // Update the original list.
                rewriteStoredMessagesJson(); // Rewrite the JSON file *without* the deleted message.
                removed = true;
                JOptionPane.showMessageDialog(null, "Message with hash '" + messageHash + "' deleted from stored messages and JSON file.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                return true; // Exit immediately upon successful deletion.
            }
        }

        // If no message was found after checking all lists.
        if (!removed) {
            JOptionPane.showMessageDialog(null, "Message with hash '" + messageHash + "' not found.", "Deletion Failed", JOptionPane.INFORMATION_MESSAGE);
        }
        return removed;
    }

    public void displaySentMessagesReport() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages to generate a report for.", "Sent Messages Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("--- Full Report: Sent Messages ---\n");
        // Iterate through each sent message and append its details to the StringBuilder.
        for (MessageData.messageData msg : sentMessages) {
            sb.append("ID: ").append(msg.getId()).append("\n")
                    .append("Recipient: ").append(msg.getRecipient()).append("\n")
                    .append("Message: ").append(msg.getMessageText()).append("\n")
                    .append("Hash: ").append(msg.getHash()).append("\n")
                    .append("Status: ").append(msg.getStatus()).append("\n")
                    .append("----------------------------------\n"); // Separator for readability.
        }
        // Use JTextArea and JScrollPane to display the report, allowing scrolling for long reports.
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false); // Make the text area read-only.
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400)); // Set preferred size for the dialog.
        JOptionPane.showMessageDialog(null, scrollPane, "Sent Messages Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Getters for Message Lists (for external access or testing) ---

    public List<MessageData.messageData> getSentMessages() {
        return sentMessages;
    }

    public List<MessageData.messageData> getDisregardedMessages() {
        return disregardedMessages;
    }

    public List<MessageData.messageData> getStoredMessages() {
        return storedMessages;
    }

    public List<String> getMessageHashes() {
        return messageHashes;
    }

    public List<String> getMessageIDs() {
        return messageIDs;
    }
}