// MessageData.java
package org.example;

import javax.swing.*;

public class MessageData {

    public static class messageData { // This inner class name remains lowercase 'messageData' as is common for inner record-like classes
        // Attributes for message details
        private String id;          // Unique message ID
        private String recipient;   // Recipient's cell number
        private String messageText; // Content of the message
        private String hash;        // Unique hash for the message
        private String status;      // Status of the message (e.g., "SENT", "PENDING")

        public messageData(String id, String recipient, String messageText, String hash, String status) {
            this.id = id;
            this.recipient = recipient;
            this.messageText = messageText;
            this.hash = hash;
            this.status = status;
        }

        @Override
        public String toString() {
            return "ID: " + id + ", To: " + recipient + ", Message: " + messageText +
                    ", Hash: " + hash + ", Status: " + status;
        }

        // --- Getters ---
        public String getId() {
            return id;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getMessageText() {
            return messageText;
        }

        public String getHash() {
            return hash;
        }

        public String getStatus() {
            return status;
        }

        // --- Setters ---
        public void setId(String id) {
            this.id = id;
        }

        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }

        public void setMessageText(String messageText) {
            this.messageText = messageText; // Corrected: should be 'this.messageText = messageText;'
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // The methods below are kept as they are generic checks for message components,
    // though the main processing logic will be in MessageProcessor.


    public boolean checkMessageID(String messageID) {
        return messageID != null && messageID.length() == 10;
    }

    public boolean checkRecipientCell(String cellNumber) {
        // Matches an international number starting with '+' followed by 10 to 15 digits
        return cellNumber != null && cellNumber.matches("^\\+\\d{10,15}$");
    }

    public String createMessageHash(String messageID, int msgNumber, String msgText) {
        // Check for invalid inputs
        if (messageID == null || messageID.length() < 2 || msgText == null || msgText.isBlank()) {
            return "INVALID_HASH";
        }

        // Extract first and last words from the message
        String[] words = msgText.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;

        // Use first two characters of message ID as prefix
        String prefix = messageID.substring(0, 2);

        // Combine elements to create the hash, converting to uppercase
        return (prefix + ":" + msgNumber + ":" + firstWord + lastWord).toUpperCase();
    }
}