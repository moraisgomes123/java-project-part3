package org.example;

import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProcessorTest {

    private MessageProcessor processor;

    private final MessageData.messageData msg1 = new MessageData.messageData(
            "001", "+27838884567", "Did you get the cake?", "hash1", "sent");

    private final MessageData.messageData msg2 = new MessageData.messageData(
            "002", "+27838884567", "Where are you? You are late! I have asked you to be on time.", "hash2", "sent");

    private final MessageData.messageData msg3 = new MessageData.messageData(
            "003", "+27838884568", "Ok, I am leaving without you.", "hash3", "sent");

    private final MessageData.messageData msg4 = new MessageData.messageData(
            "0838884567", "+27838884569", "It is dinner time!", "hash4", "sent");

    @BeforeEach
    void setUp() {
        processor = new MessageProcessor();

        // Simulate adding sent messages
        processor.addSentMessage(msg1);
        processor.addSentMessage(msg2);
        processor.addSentMessage(msg3);
        processor.addSentMessage(msg4);
    }

    @AfterEach
    void tearDown() {
        // Clean up test file if it exists
        File file = new File("storedMessages.json");
        if (file.exists()) file.delete();
    }

    @Test
    void testSentMessagesArrayIsCorrectlyPopulated() {
        List<MessageData.messageData> sent = processor.getSentMessages();
        assertEquals(4, sent.size());

        assertTrue(sent.stream().anyMatch(msg -> msg.getMessageText().equals("Did you get the cake?")));
        assertTrue(sent.stream().anyMatch(msg -> msg.getMessageText().equals("It is dinner time!")));
    }

    @Test
    void testDisplayLongestSentMessage() {
        MessageData.messageData longest = processor.getSentMessages().stream()
                .max((m1, m2) -> Integer.compare(m1.getMessageText().length(), m2.getMessageText().length()))
                .orElse(null);

        assertNotNull(longest);
        assertEquals("Where are you? You are late! I have asked you to be on time.", longest.getMessageText());
    }

    @Test
    void testSearchMessageById() {
        String searchId = "0838884567";
        MessageData.messageData found = processor.getSentMessages().stream()
                .filter(m -> m.getId().equals(searchId))
                .findFirst()
                .orElse(null);

        assertNotNull(found);
        assertEquals("It is dinner time!", found.getMessageText());
    }

    @Test
    void testSearchMessagesByRecipient() {
        String recipient = "+27838884567";

        List<MessageData.messageData> sent = processor.getSentMessages();
        List<String> messagesToRecipient = sent.stream()
                .filter(m -> m.getRecipient().equals(recipient))
                .map(MessageData.messageData::getMessageText)
                .toList();

        assertTrue(messagesToRecipient.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(messagesToRecipient.contains("Did you get the cake?"));
    }

    @Test
    void testDeleteMessageByHash() {
        boolean deleted = processor.deleteMessageByHash("hash2");
        assertTrue(deleted);

        List<MessageData.messageData> sent = processor.getSentMessages();
        assertFalse(sent.stream().anyMatch(m -> m.getHash().equals("hash2")));
    }

    @Test
    void testDisplaySentMessagesReport() {
        List<MessageData.messageData> sent = processor.getSentMessages();
        assertEquals(4, sent.size());

        for (MessageData.messageData msg : sent) {
            assertNotNull(msg.getHash());
            assertNotNull(msg.getRecipient());
            assertNotNull(msg.getMessageText());
        }
    }
}
