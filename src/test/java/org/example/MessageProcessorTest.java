package org.example;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageProcessorTest {

    private static final String DATA_FILE_NAME = "quickChatData.json";

    private MessageProcessor processor;
    private MessageData.messageData msg1, msg2, msg3, msg4;

    @BeforeEach
    void setUp() {
        processor = new MessageProcessor();

        msg1 = new MessageData.messageData("1", "+27838884567", "Did you get the cake?", "hash1", "sent");
        msg2 = new MessageData.messageData("2", "+27838884567", "Where are you? You are late! I have asked you to be on time.", "hash2", "sent");
        msg3 = new MessageData.messageData("3", "+27838884567", "Ok, I am leaving without you.", "hash3", "stored");
        msg4 = new MessageData.messageData("4", "+27830001111", "It is dinner time!", "hash4", "sent");

        // Clean state before each test
        File f = new File(DATA_FILE_NAME);
        if (f.exists()) f.delete();
    }

    @AfterEach
    void tearDown() {
        File f = new File(DATA_FILE_NAME);
        if (f.exists()) f.delete();
    }

    @Test
    void addSentMessage() {
        processor.addSentMessage(msg1);
        processor.addSentMessage(msg4);

        List<MessageData.messageData> sent = processor.getSentMessages();
        assertEquals(2, sent.size());
        assertEquals("Did you get the cake?", sent.get(0).getMessageText());
        assertEquals("It is dinner time!", sent.get(1).getMessageText());
    }

    @Test
    void displayLongestSentMessage() {
        processor.addSentMessage(msg1);
        processor.addSentMessage(msg2);
        processor.addSentMessage(msg4);

        MessageData.messageData longest = processor.getSentMessages().stream()
                .max((a, b) -> Integer.compare(a.getMessageText().length(), b.getMessageText().length()))
                .orElse(null);

        assertNotNull(longest);
        assertEquals("Where are you? You are late! I have asked you to be on time.", longest.getMessageText());
    }

    @Test
    void searchMessageById() {
        processor.addSentMessage(msg4);

        MessageData.messageData found = processor.getSentMessages().stream()
                .filter(m -> m.getId().equals("4"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);
        assertEquals("It is dinner time!", found.getMessageText());
    }

    @Test
    void searchMessagesByRecipient() {
        processor.addSentMessage(msg2);
        processor.addStoredMessage(msg3);

        long count = processor.getSentMessages().stream()
                .filter(m -> m.getRecipient().equals("+27838884567"))
                .count()
                + processor.getStoredMessages().stream()
                .filter(m -> m.getRecipient().equals("+27838884567"))
                .count();

        assertEquals(2, count);
    }

    @Test
    void deleteMessageByHash() {
        processor.addSentMessage(msg2);

        boolean deleted = processor.deleteMessageByHash("hash2");

        assertTrue(deleted);
        assertFalse(processor.getMessageHashes().contains("hash2"));
    }

    @Test
    void displaySentMessagesReport() {
        processor.addSentMessage(msg1);
        processor.addSentMessage(msg4);

        List<MessageData.messageData> report = processor.getSentMessages();

        assertEquals(2, report.size());
        assertEquals("+27838884567", report.get(0).getRecipient());
        assertEquals("hash1", report.get(0).getHash());
        assertEquals("Did you get the cake?", report.get(0).getMessageText());
    }

    @Test
    void getDisregardedMessages() {
        processor.addDisregardedMessage(msg1);
        List<MessageData.messageData> disregarded = processor.getDisregardedMessages();
        assertEquals(1, disregarded.size());
    }

    @Test
    void addStoredMessage() {
        processor.addStoredMessage(msg3);
        assertEquals(1, processor.getStoredMessages().size());
        assertTrue(new File(DATA_FILE_NAME).exists());
    }

    @Test
    void loadStoredMessagesFromJson() {
        processor.addStoredMessage(msg3); // Save to file
        MessageProcessor newProcessor = new MessageProcessor();
        boolean loaded = newProcessor.loadAllMessagesFromJson(); // NOTE: updated to correct method
        assertTrue(loaded);
        assertEquals(1, newProcessor.getStoredMessages().size());
    }

    @Test
    void getSentMessages() {
        processor.addSentMessage(msg1);
        assertEquals(1, processor.getSentMessages().size());
    }

    @Test
    void getStoredMessages() {
        processor.addStoredMessage(msg3);
        assertEquals(1, processor.getStoredMessages().size());
    }

    @Test
    void getMessageHashes() {
        processor.addSentMessage(msg1);
        assertTrue(processor.getMessageHashes().contains("hash1"));
    }

    @Test
    void getMessageIDs() {
        processor.addSentMessage(msg1);
        assertTrue(processor.getMessageIDs().contains("1"));
    }
}
