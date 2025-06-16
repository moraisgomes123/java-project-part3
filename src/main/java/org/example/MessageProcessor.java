package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MessageProcessor {

    // --- Message Storage Lists ---
    private List<MessageData.messageData> sentMessages;
    private List<MessageData.messageData> disregardedMessages;
    private List<MessageData.messageData> storedMessages;
    private List<String> messageHashes;
    private List<String> messageIDs;

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
        saveMessageToJsonFile(msg);
    }

    // --- JSON File Handling Methods ---
    private void saveMessageToJsonFile(MessageData.messageData msg) {
        JSONObject json = new JSONObject();
        json.put("id", msg.getId());
        json.put("sender", msg.getSender());
        json.put("recipient", msg.getRecipient());
        json.put("messageText", msg.getMessageText());
        json.put("hash", msg.getHash());
        json.put("status", msg.getStatus());

        try (FileWriter file = new FileWriter("storedMessages.json", true)) {
            file.write(json.toString() + System.lineSeparator());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving message to JSON: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean loadStoredMessagesFromJson() {
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();

        File jsonFile = new File("storedMessages.json");
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            return false;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            StringBuilder jsonContent = new StringBuilder();
            int character;
            while ((character = reader.read()) != -1) {
                jsonContent.append((char) character);
            }

            String[] lines = jsonContent.toString().split(System.lineSeparator());
            boolean messagesFound = false;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                try {
                    JSONObject json = new JSONObject(line);
                    String id = json.getString("id");
                    String sender = json.getString("sender");
                    String recipient = json.getString("recipient");
                    String messageText = json.getString("messageText");
                    String hash = json.getString("hash");
                    String status = json.getString("status");

                    MessageData.messageData msg = new MessageData.messageData(
                            id, sender, recipient, messageText, hash, status);
                    storedMessages.add(msg);
                    messageHashes.add(hash);
                    messageIDs.add(id);
                    messagesFound = true;
                } catch (JSONException e) {
                    System.err.println("Skipping malformed JSON line: " + line + " Error: " + e.getMessage());
                }
            }
            return messagesFound;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading stored messages: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void rewriteStoredMessagesJson() {
        try (FileWriter file = new FileWriter("storedMessages.json")) {
            for (MessageData.messageData msg : storedMessages) {
                JSONObject json = new JSONObject();
                json.put("id", msg.getId());
                json.put("sender", msg.getSender());
                json.put("recipient", msg.getRecipient());
                json.put("messageText", msg.getMessageText());
                json.put("hash", msg.getHash());
                json.put("status", msg.getStatus());
                file.write(json.toString() + System.lineSeparator());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error rewriting JSON file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Message Display and Query Methods ---
    public void displaySentMessageSendersAndRecipients() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages to display.",
                    "Sent Messages", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("--- Sent Messages ---\n");
        for (MessageData.messageData msg : sentMessages) {
            sb.append("Sender: ").append(msg.getSender())
                    .append(" | Recipient: ").append(msg.getRecipient())
                    .append(" | ID: ").append(msg.getId())
                    .append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(null, scrollPane,
                "Sent Messages Detail", JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayLongestSentMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages to analyze.",
                    "Longest Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MessageData.messageData longestMessage = null;
        int maxLength = -1;

        for (MessageData.messageData msg : sentMessages) {
            if (msg.getMessageText().length() > maxLength) {
                maxLength = msg.getMessageText().length();
                longestMessage = msg;
            }
        }

        if (longestMessage != null) {
            JOptionPane.showMessageDialog(null,
                    "--- Longest Sent Message ---\n" +
                            "Sender: " + longestMessage.getSender() + "\n" +
                            "Recipient: " + longestMessage.getRecipient() + "\n" +
                            "ID: " + longestMessage.getId() + "\n" +
                            "Message: " + longestMessage.getMessageText() + "\n" +
                            "Length: " + maxLength + " characters",
                    "Longest Sent Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void searchMessageById(String messageId) {
        Optional<MessageData.messageData> foundMsg = Optional.empty();

        foundMsg = sentMessages.stream()
                .filter(msg -> msg.getId().equals(messageId))
                .findFirst();

        if (foundMsg.isEmpty()) {
            foundMsg = disregardedMessages.stream()
                    .filter(msg -> msg.getId().equals(messageId))
                    .findFirst();
        }
        if (foundMsg.isEmpty()) {
            foundMsg = storedMessages.stream()
                    .filter(msg -> msg.getId().equals(messageId))
                    .findFirst();
        }

        if (foundMsg.isPresent()) {
            MessageData.messageData msg = foundMsg.get();
            JOptionPane.showMessageDialog(null,
                    "--- Message Details (ID: " + messageId + ") ---\n" +
                            "Sender: " + msg.getSender() + "\n" +
                            "Recipient: " + msg.getRecipient() + "\n" +
                            "Message: " + msg.getMessageText() + "\n" +
                            "Hash: " + msg.getHash() + "\n" +
                            "Status: " + msg.getStatus(),
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Message with ID '" + messageId + "' not found.",
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void searchMessagesByRecipient(String recipient) {
        StringBuilder sb = new StringBuilder("--- Messages to: " + recipient + " ---\n");
        boolean found = false;

        for (MessageData.messageData msg : sentMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("Sender: ").append(msg.getSender()).append("\n")
                        .append("ID: ").append(msg.getId()).append("\n")
                        .append("Message: '").append(msg.getMessageText()).append("'\n")
                        .append("Status: ").append(msg.getStatus()).append("\n---\n");
                found = true;
            }
        }
        for (MessageData.messageData msg : disregardedMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("Sender: ").append(msg.getSender()).append("\n")
                        .append("ID: ").append(msg.getId()).append("\n")
                        .append("Message: '").append(msg.getMessageText()).append("'\n")
                        .append("Status: ").append(msg.getStatus()).append("\n---\n");
                found = true;
            }
        }
        for (MessageData.messageData msg : storedMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("Sender: ").append(msg.getSender()).append("\n")
                        .append("ID: ").append(msg.getId()).append("\n")
                        .append("Message: '").append(msg.getMessageText()).append("'\n")
                        .append("Status: ").append(msg.getStatus()).append("\n---\n");
                found = true;
            }
        }

        if (found) {
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            JOptionPane.showMessageDialog(null, scrollPane,
                    "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No messages found for recipient '" + recipient + "'.",
                    "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean deleteMessageByHash(String messageHash) {
        boolean removed = false;
        List<MessageData.messageData> tempSent = new ArrayList<>(sentMessages);
        List<MessageData.messageData> tempDisregarded = new ArrayList<>(disregardedMessages);
        List<MessageData.messageData> tempStored = new ArrayList<>(storedMessages);

        Iterator<MessageData.messageData> sentIterator = tempSent.iterator();
        while (sentIterator.hasNext()) {
            MessageData.messageData msg = sentIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                sentIterator.remove();
                messageIDs.remove(msg.getId());
                messageHashes.remove(msg.getHash());
                removed = true;
                this.sentMessages = tempSent;
                JOptionPane.showMessageDialog(null, "Message deleted from sent messages.",
                        "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }

        Iterator<MessageData.messageData> disregardedIterator = tempDisregarded.iterator();
        while (disregardedIterator.hasNext()) {
            MessageData.messageData msg = disregardedIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                disregardedIterator.remove();
                removed = true;
                this.disregardedMessages = tempDisregarded;
                JOptionPane.showMessageDialog(null, "Message deleted from disregarded messages.",
                        "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }

        Iterator<MessageData.messageData> storedIterator = tempStored.iterator();
        while (storedIterator.hasNext()) {
            MessageData.messageData msg = storedIterator.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                storedIterator.remove();
                messageHashes.remove(msg.getHash());
                messageIDs.remove(msg.getId());
                this.storedMessages = tempStored;
                rewriteStoredMessagesJson();
                removed = true;
                JOptionPane.showMessageDialog(null, "Message deleted from stored messages.",
                        "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }

        if (!removed) {
            JOptionPane.showMessageDialog(null, "Message not found.",
                    "Deletion Failed", JOptionPane.INFORMATION_MESSAGE);
        }
        return removed;
    }

    public void displaySentMessagesReport() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages to report.",
                    "Sent Messages Report", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("--- Full Report: Sent Messages ---\n");
        for (MessageData.messageData msg : sentMessages) {
            sb.append("Sender: ").append(msg.getSender()).append("\n")
                    .append("Recipient: ").append(msg.getRecipient()).append("\n")
                    .append("ID: ").append(msg.getId()).append("\n")
                    .append("Message: ").append(msg.getMessageText()).append("\n")
                    .append("Hash: ").append(msg.getHash()).append("\n")
                    .append("Status: ").append(msg.getStatus()).append("\n")
                    .append("----------------------------------\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(null, scrollPane,
                "Sent Messages Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Getters for Message Lists ---
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