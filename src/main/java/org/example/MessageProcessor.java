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
import java.util.*;
import java.util.List;

public class MessageProcessor {

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
            showErrorMessage("Error saving message to JSON: " + e.getMessage(), "File Error");
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
                    MessageData.messageData msg = new MessageData.messageData(
                            json.getString("id"),
                            json.getString("sender"),
                            json.getString("recipient"),
                            json.getString("messageText"),
                            json.getString("hash"),
                            json.getString("status")
                    );
                    storedMessages.add(msg);
                    messageHashes.add(msg.getHash());
                    messageIDs.add(msg.getId());
                    messagesFound = true;
                } catch (JSONException e) {
                    System.err.println("Skipping malformed JSON line: " + line);
                }
            }
            return messagesFound;
        } catch (IOException e) {
            showErrorMessage("Error reading stored messages: " + e.getMessage(), "File Error");
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
            showErrorMessage("Error rewriting JSON file: " + e.getMessage(), "File Error");
        }
    }

    public void displaySentMessageSendersAndRecipients() {
        if (sentMessages.isEmpty()) {
            showInfoMessage("No sent messages to display.", "Sent Messages");
            return;
        }

        StringBuilder sb = new StringBuilder("--- Sent Messages ---\n");
        for (MessageData.messageData msg : sentMessages) {
            sb.append("Sender: ").append(msg.getSender())
                    .append(" | Recipient: ").append(msg.getRecipient())
                    .append(" | ID: ").append(msg.getId())
                    .append("\n");
        }

        showScrollableMessage(sb.toString(), "Sent Messages Detail", 400, 300);
    }

    public void displayLongestSentMessage() {
        if (sentMessages.isEmpty()) {
            showInfoMessage("No sent messages to analyze.", "Longest Message");
            return;
        }

        MessageData.messageData longest = null;
        int maxLength = -1;

        for (MessageData.messageData msg : sentMessages) {
            if (msg.getMessageText().length() > maxLength) {
                maxLength = msg.getMessageText().length();
                longest = msg;
            }
        }

        if (longest != null) {
            showInfoMessage(
                    "--- Longest Sent Message ---\n" +
                            "Sender: " + longest.getSender() + "\n" +
                            "Recipient: " + longest.getRecipient() + "\n" +
                            "ID: " + longest.getId() + "\n" +
                            "Message: " + longest.getMessageText() + "\n" +
                            "Length: " + maxLength + " characters",
                    "Longest Sent Message"
            );
        }
    }

    public void searchMessageById(String messageId) {
        Optional<MessageData.messageData> found = findMessageById(messageId);

        if (found.isPresent()) {
            MessageData.messageData msg = found.get();
            showInfoMessage(
                    "--- Message Details (ID: " + messageId + ") ---\n" +
                            "Sender: " + msg.getSender() + "\n" +
                            "Recipient: " + msg.getRecipient() + "\n" +
                            "Message: " + msg.getMessageText() + "\n" +
                            "Hash: " + msg.getHash() + "\n" +
                            "Status: " + msg.getStatus(),
                    "Search Result"
            );
        } else {
            showInfoMessage("Message with ID '" + messageId + "' not found.", "Search Result");
        }
    }

    public void searchMessagesByRecipient(String recipient) {
        StringBuilder sb = new StringBuilder("--- Messages to: " + recipient + " ---\n");
        boolean found = false;

        for (MessageData.messageData msg : getAllMessages()) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("Sender: ").append(msg.getSender()).append("\n")
                        .append("ID: ").append(msg.getId()).append("\n")
                        .append("Message: '").append(msg.getMessageText()).append("'\n")
                        .append("Status: ").append(msg.getStatus()).append("\n---\n");
                found = true;
            }
        }

        if (found) {
            showScrollableMessage(sb.toString(), "Messages by Recipient", 500, 400);
        } else {
            showInfoMessage("No messages found for recipient '" + recipient + "'.",
                    "Messages by Recipient");
        }
    }

    public boolean deleteMessageByHash(String messageHash) {
        boolean removed = false;

        Iterator<MessageData.messageData> it = sentMessages.iterator();
        while (it.hasNext()) {
            MessageData.messageData msg = it.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                it.remove();
                messageIDs.remove(msg.getId());
                messageHashes.remove(msg.getHash());
                showInfoMessage("Message deleted from sent messages.", "Deletion Successful");
                return true;
            }
        }

        it = disregardedMessages.iterator();
        while (it.hasNext()) {
            MessageData.messageData msg = it.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                it.remove();
                showInfoMessage("Message deleted from disregarded messages.", "Deletion Successful");
                return true;
            }
        }

        it = storedMessages.iterator();
        while (it.hasNext()) {
            MessageData.messageData msg = it.next();
            if (msg.getHash().equalsIgnoreCase(messageHash)) {
                it.remove();
                messageIDs.remove(msg.getId());
                messageHashes.remove(msg.getHash());
                rewriteStoredMessagesJson();
                showInfoMessage("Message deleted from stored messages.", "Deletion Successful");
                return true;
            }
        }

        showInfoMessage("Message not found.", "Deletion Failed");
        return false;
    }

    public void displaySentMessagesReport() {
        if (sentMessages.isEmpty()) {
            showInfoMessage("No sent messages to report.", "Sent Messages Report");
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

        showScrollableMessage(sb.toString(), "Sent Messages Report", 500, 400);
    }

    // ---------- Utilities ----------

    private Optional<MessageData.messageData> findMessageById(String id) {
        return getAllMessages().stream()
                .filter(msg -> msg.getId().equals(id))
                .findFirst();
    }

    private List<MessageData.messageData> getAllMessages() {
        List<MessageData.messageData> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);
        return all;
    }

    private void showInfoMessage(String message, String title) {
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("[INFO] " + title + ": " + message);
        }
    }

    private void showErrorMessage(String message, String title) {
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        } else {
            System.err.println("[ERROR] " + title + ": " + message);
        }
    }

    private void showScrollableMessage(String content, String title, int width, int height) {
        if (!GraphicsEnvironment.isHeadless()) {
            JTextArea textArea = new JTextArea(content);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(width, height));
            JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("[INFO - " + title + "]\n" + content);
        }
    }

    // ---------- Getters ----------

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
