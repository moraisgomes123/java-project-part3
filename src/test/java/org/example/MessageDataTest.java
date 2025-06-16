package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageDataTest {

    @Test
    public void testMessageConstructorAndToString() {
        MessageData.messageData msg = new MessageData.messageData(
                "MSG0000001",
                "Dev",
                "+27838884567",
                "Did you get the cake?",
                "MS:1:Didcake",
                "SENT"
        );

        assertEquals("MSG0000001", msg.getId());
        assertEquals("Dev", msg.getSender());
        assertEquals("+27838884567", msg.getRecipient());
        assertEquals("Did you get the cake?", msg.getMessageText());
        assertEquals("MS:1:Didcake", msg.getHash());
        assertEquals("SENT", msg.getStatus());

        String expected = "id: MSG0000001, sender: Dev, recipient: +27838884567, Message: Did you get the cake?, Hash: MS:1:Didcake, Status: SENT";
        assertEquals(expected, msg.toString());
    }

    @Test
    public void testCreateMessageHashValid() {
        MessageData util = new MessageData();

        String messageId = "MS98765432";
        int msgNumber = 2;
        String messageText = "Hello again everyone";

        String hash = util.createMessageHash(messageId, msgNumber, messageText);

        assertEquals("MS:2:HELLOEVERYONE", hash);
    }

    @Test
    public void testCreateMessageHashInvalid() {
        MessageData util = new MessageData();

        String invalidHash = util.createMessageHash(null, 1, "Test");
        assertEquals("INVALID_HASH", invalidHash);

        invalidHash = util.createMessageHash("A", 1, "Test");
        assertEquals("INVALID_HASH", invalidHash);

        invalidHash = util.createMessageHash("MS12345678", 1, "   ");
        assertEquals("INVALID_HASH", invalidHash);
    }

    @Test
    public void testCheckMessageID() {
        MessageData util = new MessageData();

        assertTrue(util.checkMessageID("MSG1234567"));
        assertFalse(util.checkMessageID("MSG123"));
    }

    @Test
    public void testCheckRecipientCell() {
        MessageData util = new MessageData();

        assertTrue(util.checkRecipientCell("+27838884567"));
        assertFalse(util.checkRecipientCell("0838884567"));  // Missing '+'
    }
}
