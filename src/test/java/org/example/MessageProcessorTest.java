package org.example;

import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageProcessorTest {

    private MessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MessageProcessor();
    }

    @AfterEach
    void cleanUp() {
        File jsonFile = new File("storedMessages.json");
        if (jsonFile.exists()) {
            jsonFile.delete();
        }
    }

    private MessageData.messageData sampleMessage(String id) {
        return new MessageData.messageData(
                id,
                "Alice",
                "Bob",
                "Hello Bob!",
                "hash-" + id,
                "SENT"
        );
    }

    @Test
    void testAddSentMessage() {
        MessageData.messageData msg = sampleMessage("1");
        processor.addSentMessage(msg);

        List<MessageData.messageData> sent = processor.getSentMessages();
        assertEquals(1, sent.size());
        assertEquals("Alice", sent.get(0).getSender());
    }

    @Test
    void testAddStoredMessageAndLoadFromJson() {
        MessageData.messageData msg = sampleMessage("2");
        processor.addStoredMessage(msg);

        // Clear state to force reload
        processor.getStoredMessages().clear();
        boolean loaded = processor.loadStoredMessagesFromJson();

        assertTrue(loaded);
        List<MessageData.messageData> stored = processor.getStoredMessages();
        assertEquals(1, stored.size());
        assertEquals("2", stored.get(0).getId());
    }

    @Test
    void testAddDisregardedMessage() {
        MessageData.messageData msg = sampleMessage("3");
        processor.addDisregardedMessage(msg);

        List<MessageData.messageData> disregarded = processor.getDisregardedMessages();
        assertEquals(1, disregarded.size());
        assertEquals("3", disregarded.get(0).getId());
    }

    @Test
    void testDeleteMessageByHash() {
        MessageData.messageData msg = sampleMessage("4");
        processor.addSentMessage(msg);

        boolean deleted = processor.deleteMessageByHash("hash-4");
        assertTrue(deleted);
        assertTrue(processor.getSentMessages().isEmpty());
    }

    @Test
    void testDeleteMessageByHash_NotFound() {
        MessageData.messageData msg = sampleMessage("5");
        processor.addSentMessage(msg);

        boolean deleted = processor.deleteMessageByHash("nonexistent-hash");
        assertFalse(deleted);
        assertFalse(processor.getSentMessages().isEmpty());
    }
}
