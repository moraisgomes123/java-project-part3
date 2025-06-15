package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageDataTest {

    @Test
    void checkMessageID() {
        MessageData.messageData msg = new MessageData.messageData("123", "+27830001111", "Hello!", "xyz123", "sent");
        assertEquals("123", msg.getId());
    }

    @Test
    void checkRecipientCell() {
        MessageData.messageData msg = new MessageData.messageData("124", "+27830001122", "Hi!", "abc456", "stored");
        assertEquals("+27830001122", msg.getRecipient());
    }

    @Test
    void createMessageHash() {
        MessageData.messageData msg = new MessageData.messageData("125", "+27830001133", "Test Message", "hash789", "disregarded");
        assertEquals("hash789", msg.getHash());
    }
}
