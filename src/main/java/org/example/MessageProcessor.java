// MessageProcessor.java
package org.example;

import org.json.JSONArray;   // Used for handling JSON arrays
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

    // --- File Name Constant ---
    // Consolidated JSON file name for storing both sent and stored messages.
    private static final String DATA_FILE_NAME = "quickChatData.json";

    // --- Message Storage Lists ---
    // These lists hold MessageData.messageData objects based on their status.
    private List<MessageData.messageData> sentMessages;       // Messages that have been "sent" and will be persisted
    private List<MessageData.messageData> disregardedMessages; // Messages that were "discarded" (not persisted)
    private List<MessageData.messageData> storedMessages;     // Messages "stored for later" (also persisted)

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
        // It's generally good practice to load data upon initialization,
        // but given the MainMenu explicitly calls load via an option,
        // we'll keep the loading triggered by user action.
    }

    // --- Methods to Add Messages to Respective Lists ---

    /**
     * Adds a new message to the list of sent messages and persists all relevant data to JSON.
     * @param msg The message data object to add.
     */
    public void addSentMessage(MessageData.messageData msg) {
        sentMessages.add(msg);
        messageHashes.add(msg.getHash());
        messageIDs.add(msg.getId());
        saveAllMessagesToJson(); // Persist all relevant data after adding a sent message
    }

    /**
     * Adds a new message to the list of disregarded messages.
     * Disregarded messages are not persisted according to the user's requirements.
     * @param msg The message data object to add.
     */
    public void addDisregardedMessage(MessageData.messageData msg) {
        disregardedMessages.add(msg);
    }

    /**
     * Adds a new message to the list of stored messages and persists all relevant data to JSON.
     * @param msg The message data object to add.
     */
    public void addStoredMessage(MessageData.messageData msg) {
        storedMessages.add(msg);
        messageHashes.add(msg.getHash());
        messageIDs.add(msg.getId());
        saveAllMessagesToJson(); // Persist all relevant data after adding a stored message
    }

    // --- JSON File Handling Methods (Consolidated) ---

    /**
     * Saves both sent and stored messages to a single JSON file (`quickChatData.json`).
     * Disregarded messages are intentionally not persisted as per user requirements.
     * The data is structured as a single JSON object containing two JSON arrays:
     * "sentMessages" and "storedMessages". This method overwrites the file.
     */
    public void saveAllMessagesToJson() {
        JSONObject rootJson = new JSONObject();

        // Prepare sent messages for JSON array
        JSONArray sentMessagesArray = new JSONArray();
        for (MessageData.messageData msg : sentMessages) {
            JSONObject msgJson = new JSONObject();
            msgJson.put("id", msg.getId());
            msgJson.put("recipient", msg.getRecipient());
            msgJson.put("messageText", msg.getMessageText());
            msgJson.put("hash", msg.getHash());
            msgJson.put("status", msg.getStatus());
            sentMessagesArray.put(msgJson);
        }
        rootJson.put("sentMessages", sentMessagesArray); // Add sent messages array to root JSON

        // Prepare stored messages for JSON array
        JSONArray storedMessagesArray = new JSONArray();
        for (MessageData.messageData msg : storedMessages) {
            JSONObject msgJson = new JSONObject();
            msgJson.put("id", msg.getId());
            msgJson.put("recipient", msg.getRecipient());
            msgJson.put("messageText", msg.getMessageText());
            msgJson.put("hash", msg.getHash());
            msgJson.put("status", msg.getStatus());
            storedMessagesArray.put(msgJson);
        }
        rootJson.put("storedMessages", storedMessagesArray); // Add stored messages array to root JSON

        // Write the complete JSON object to the file, overwriting existing content.
        try (FileWriter file = new FileWriter(DATA_FILE_NAME)) {
            file.write(rootJson.toString(4)); // Write JSON object with 4-space indentation for readability
        } catch (IOException e) {
            // Display an error message if there's an issue writing to the file.
            JOptionPane.showMessageDialog(null, "Error saving messages to JSON: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads both sent and stored messages from the consolidated JSON file (`quickChatData.json`).
     * Clears existing sent and stored messages lists before loading to prevent duplicates.
     * Populates the global messageHashes and messageIDs tracking lists based on the loaded messages.
     *
     * @return true if messages were successfully loaded (file existed and contained valid data),
     * false otherwise (e.g., file not found, empty, or parsing error).
     */
    public boolean loadAllMessagesFromJson() {
        // Clear existing data to ensure a fresh load and prevent duplicate entries.
        sentMessages.clear();
        storedMessages.clear();
        messageHashes.clear(); // Clear global hash tracking list
        messageIDs.clear();    // Clear global ID tracking list

        File jsonFile = new File(DATA_FILE_NAME);
        // Check if the file exists and is not empty.
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return false; // Indicate no messages to load
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            StringBuilder jsonContent = new StringBuilder();
            int character;
            // Read the entire file content into a StringBuilder.
            while ((character = reader.read()) != -1) {
                jsonContent.append((char) character);
            }

            // Parse the entire file content as a single JSONObject.
            JSONObject rootJson = new JSONObject(jsonContent.toString());
            boolean messagesFound = false; // Flag to track if any valid messages are loaded

            // --- Load Sent Messages ---
            // Check if "sentMessages" array exists in the JSON.
            if (rootJson.has("sentMessages")) {
                JSONArray sentArray = rootJson.getJSONArray("sentMessages");
                for (int i = 0; i < sentArray.length(); i++) {
                    try {
                        JSONObject json = sentArray.getJSONObject(i);
                        // Extract message attributes from the JSON object.
                        String id = json.getString("id");
                        String recipient = json.getString("recipient");
                        String messageText = json.getString("messageText");
                        String hash = json.getString("hash");
                        String status = json.getString("status");

                        // Create a new messageData object and add it to the sentMessages list.
                        MessageData.messageData msg = new MessageData.messageData(id, recipient, messageText, hash, status);
                        sentMessages.add(msg);
                        messageHashes.add(hash); // Add to global hash list
                        messageIDs.add(id);      // Add to global ID list
                        messagesFound = true;    // Set flag to true if at least one message is loaded
                    } catch (JSONException e) {
                        System.err.println("Skipping malformed sent message JSON entry: " + sentArray.getJSONObject(i).toString() + " Error: " + e.getMessage());
                    }
                }
            }

            // --- Load Stored Messages ---
            // Check if "storedMessages" array exists in the JSON.
            if (rootJson.has("storedMessages")) {
                JSONArray storedArray = rootJson.getJSONArray("storedMessages");
                for (int i = 0; i < storedArray.length(); i++) {
                    try {
                        JSONObject json = storedArray.getJSONObject(i);
                        // Extract message attributes from the JSON object.
                        String id = json.getString("id");
                        String recipient = json.getString("recipient");
                        String messageText = json.getString("messageText");
                        String hash = json.getString("hash");
                        String status = json.getString("status");

                        // Create a new messageData object and add it to the storedMessages list.
                        MessageData.messageData msg = new MessageData.messageData(id, recipient, messageText, hash, status);
                        storedMessages.add(msg);
                        messageHashes.add(hash); // Add to global hash list
                        messageIDs.add(id);      // Add to global ID list
                        messagesFound = true;    // Set flag to true if at least one message is loaded
                    } catch (JSONException e) {
                        System.err.println("Skipping malformed stored message JSON entry: " + storedArray.getJSONObject(i).toString() + " Error: " + e.getMessage());
                    }
                }
            }
            return messagesFound; // Return true if any messages were loaded, false otherwise
        } catch (IOException | JSONException e) {
            // Inform the user if there's an error reading or parsing the file.
            JOptionPane.showMessageDialog(null, "Error loading messages from JSON file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // --- Message Display and Query Methods (No significant changes needed here) ---

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

    /**
     * Attempts to delete a message by its unique hash from sent, disregarded, or stored messages.
     * If found in sent or stored messages, it also updates the JSON file.
     * @param messageHash The hash of the message to delete.
     * @return true if the message was found and deleted, false otherwise.
     */
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
                this.sentMessages = tempSent; // Update the original list with the modified temporary list.
                saveAllMessagesToJson(); // Save changes to the JSON file immediately after deletion.
                JOptionPane.showMessageDialog(null, "Message with hash '" + messageHash + "' deleted from sent messages and JSON file.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                return true; // Exit immediately upon successful deletion.
            }
        }

        // --- Remove from disregarded messages ---
        // Disregarded messages are not persisted, so no JSON update is needed here.
        Iterator<MessageData.messageData> disregardedIterator = tempDisregarded.iterator();
        while (disregardedIterator.hasNext()) {
            MessageData.messageData msg = disregardedIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                disregardedIterator.remove(); // Remove from temporary list.
                removed = true;
                this.disregardedMessages = tempDisregarded; // Update the original list.
                JOptionPane.showMessageDialog(null, "Message with hash '" + messageHash + "' deleted from disregarded messages.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
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
                removed = true;
                this.storedMessages = tempStored; // Update the original list.
                saveAllMessagesToJson(); // Rewrite the JSON file *without* the deleted message.
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
