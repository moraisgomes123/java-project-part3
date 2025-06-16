package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProcessorTest {

    private MessageProcessor processor;

    private MessageData.messageData message1;
    private MessageData.messageData message2;
    private MessageData.messageData message3;
    private MessageData.messageData message4;

    @BeforeEach
    public void setup() {
        processor = new MessageProcessor();

        message1 = new MessageData.messageData("MSG0000001", "Dev", "+27838884567",
                "Did you get the cake?", "MS:1:Didcake", "SENT");
        message2 = new MessageData.messageData("MSG0000002", "Dev", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.",
                "MS:2:Wheretime.", "SENT");
        message3 = new MessageData.messageData("MSG0000003", "Dev", "+27838884567",
                "Ok, I am leaving without you.", "MS:3:Okyou.", "SENT");
        message4 = new MessageData.messageData("MSG0000004", "Dev", "+27838884567",
                "It is dinner time!", "MS:4:Ittime!", "SENT");

        processor.addSentMessage(message1);
        processor.addSentMessage(message2);
        processor.addSentMessage(message3);
        processor.addSentMessage(message4);
    }

    @Test
    public void testSentMessagesCorrectlyPopulated() {
        List<MessageData.messageData> sent = processor.getSentMessages();

        assertEquals(4, sent.size());
        assertEquals("Did you get the cake?", sent.get(0).getMessageText());
        assertEquals("It is dinner time!", sent.get(3).getMessageText());
    }

    @Test
    public void testLongestSentMessage() {
        MessageData.messageData longest = processor.getSentMessages().stream()
                .max((a, b) -> Integer.compare(a.getMessageText().length(), b.getMessageText().length()))
                .orElse(null);

        assertNotNull(longest);
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                longest.getMessageText());
    }

    @Test
    public void testSearchMessageById() {
        String searchId = "MSG0000004";
        boolean found = processor.getSentMessages().stream()
                .anyMatch(msg -> msg.getId().equals(searchId) &&
                        msg.getMessageText().equals("It is dinner time!"));

        assertTrue(found);
    }

    @Test
    public void testSearchMessagesByRecipient() {
        List<String> messages = processor.getSentMessages().stream()
                .filter(m -> m.getRecipient().equals("+27838884567"))
                .map(MessageData.messageData::getMessageText)
                .toList();

        assertTrue(messages.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(messages.contains("Ok, I am leaving without you.") ||
                messages.contains("It is dinner time!"));
    }

    @Test
    public void testDeleteMessageByHash() {
        boolean deleted = processor.deleteMessageByHash("MS:2:Wheretime.");
        assertTrue(deleted);

        boolean stillExists = processor.getSentMessages().stream()
                .anyMatch(m -> m.getHash().equals("MS:2:Wheretime."));
        assertFalse(stillExists);
    }

    @Test
    public void testSentMessagesReportFields() {
        for (MessageData.messageData msg : processor.getSentMessages()) {
            assertNotNull(msg.getHash());
            assertNotNull(msg.getRecipient());
            assertNotNull(msg.getMessageText());
        }
    }
}
