package org.example;

import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProcessorTest {

    private MessageProcessor processor;
    private MessageData.messageData sampleMessage;

    @BeforeEach
    void setUp() {
        processor = new MessageProcessor();

        sampleMessage = new MessageData.messageData(
                "id001",
                "Alice",
                "Bob",
                "Hello Bob!",
                "hash001",
                "SENT"
        );
    }

    @AfterEach
    void tearDown() {
        File file = new File("storedMessages.json");
        if (file.exists()) file.delete();
    }

    @Test
    void testAddSentMessage() {
        processor.addSentMessage(sampleMessage);

        List<MessageData.messageData> sent = processor.getSentMessages();
        assertEquals(1, sent.size());
        assertEquals("Alice", sent.get(0).getSender());
    }

    @Test
    void testAddStoredMessageAndFileWrite() {
        processor.addStoredMessage(sampleMessage);

        List<MessageData.messageData> stored = processor.getStoredMessages();
        assertEquals(1, stored.size());
        assertTrue(new File("storedMessages.json").exists());
    }

    @Test
    void testLoadStoredMessagesFromJson() {
        processor.addStoredMessage(sampleMessage);

        MessageProcessor newProcessor = new MessageProcessor();
        assertTrue(newProcessor.loadStoredMessagesFromJson());

        List<MessageData.messageData> loaded = newProcessor.getStoredMessages();
        assertEquals(1, loaded.size());
        assertEquals("Bob", loaded.get(0).getRecipient());
    }

    @Test
    void testDeleteMessageByHash() {
        processor.addSentMessage(sampleMessage);
        assertTrue(processor.deleteMessageByHash("hash001"));
        assertTrue(processor.getSentMessages().isEmpty());
    }

    @Test
    void testSearchMessageByIdFound() {
        processor.addSentMessage(sampleMessage);
        processor.searchMessageById("id001");
        // We expect console output (or GUI if not headless), no assert needed here
    }

    @Test
    void testSearchMessageByIdNotFound() {
        processor.searchMessageById("nonexistent");
        // Expect no crash, only output
    }

    @Test
    void testSearchMessagesByRecipient() {
        processor.addSentMessage(sampleMessage);
        processor.searchMessagesByRecipient("Bob");
        processor.searchMessagesByRecipient("Charlie"); // Not found
    }

    @Test
    void testDisplaySentMessagesReport() {
        processor.addSentMessage(sampleMessage);
        processor.displaySentMessagesReport(); // Just verifying it doesn't crash
    }

    @Test
    void testDisplayLongestSentMessage() {
        processor.addSentMessage(sampleMessage);
        processor.displayLongestSentMessage(); // Should show "Hello Bob!"
    }

    @Test
    void testNoCrashOnEmptyJsonLoad() {
        assertFalse(processor.loadStoredMessagesFromJson()); // no file yet
    }
}

