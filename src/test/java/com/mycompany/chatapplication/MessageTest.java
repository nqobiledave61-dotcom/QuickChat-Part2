package com.mycompany.chatapplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MessageTest {

    @BeforeEach
    public void resetSentMessages() {
        Message.clearSentMessages();
    }

    @Test
    public void messageIdHasTenDigits() {
        Message message = new Message(
                "+27718693002",
                "Hi Mike, can you join us for dinner tonight?", 0);

        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
        assertTrue(message.getMessageID().matches("[0-9]{10}"));
    }

    @Test
    public void invalidMessageIdIsRejected() {
        Message message = new Message(
                "12345",
                "+27718693002",
                "Hello there",
                0);

        assertFalse(message.checkMessageID());
    }

    @Test
    public void messageLengthOf250CharactersIsValid() {
        Message message = new Message(
                "+27718693002", "a".repeat(250), 0);

        assertEquals("Message ready to send.",
                message.checkMessageLength());
    }

    @Test
    public void messageLongerThan250CharactersIsRejected() {
        Message message = new Message(
                "+27718693002", "a".repeat(253), 0);

        assertEquals(
                "Message exceeds 250 characters by 3; please reduce the size.",
                message.checkMessageLength());
    }

    @Test
    public void recipientInternationalNumberIsAccepted() {
        Message message = new Message(
                "+27718693002", "Hello", 0);

        assertEquals("Cell phone number successfully captured.",
                message.checkRecipientCell());
    }

    @Test
    public void recipientWithoutInternationalCodeIsRejected() {
        Message message = new Message(
                "08575975889", "Hello", 0);

        assertEquals(
                "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                message.checkRecipientCell());
    }

    @Test
    public void messageHashMatchesRequiredFormat() {
        Message message = new Message(
                "0000000000",
                "+27718693002",
                "Hi Mike, can you join us for dinner tonight?",
                0);

        assertEquals("00:0:HITONIGHT",
                message.createMessageHash());
    }

    @Test
    public void remainingMessageHashesCanBeCheckedInALoop() {
        String[][] testCases = {
            {"1234567890", "Hello there", "12:1:HELLOTHERE"},
            {"9876543210", "Payment received!", "98:2:PAYMENTRECEIVED"},
            {"0000000000",
                "Hi Mike, can you join us for dinner tonight?",
                "00:0:HITONIGHT"}
        };

        for (String[] testCase : testCases) {
            Message message = new Message(
                    testCase[0],
                    "+27718693002",
                    testCase[1],
                    Integer.parseInt(testCase[2].split(":")[1]));

            assertEquals(testCase[2], message.createMessageHash());
        }
    }

    @Test
    public void sendMessageReturnsSuccessAndIncrementsTotal() {
        Message message = new Message(
                "0000000000",
                "+27718693002",
                "Hi Mike tonight",
                0);

        assertEquals("Message successfully sent.",
                message.SentMessage(1));
        assertEquals(1, message.returnTotalMessages());
    }

    @Test
    public void disregardMessageDoesNotIncrementTotal() {
        Message message = new Message(
                "0000000000",
                "+27718693002",
                "Hi Keegan payment",
                1);

        assertEquals("Press 0 to delete the message.",
                message.SentMessage(2));
        assertEquals(0, message.returnTotalMessages());
    }

@Test
public void storeMessageReturnsSuccess(@TempDir Path tempDir)
        throws IOException {

    Message message = new Message(
            "0000000000",
            "+27718693002",
            "Store this message",
            0);

    Path testFile = tempDir.resolve("messages.json");

    assertEquals("Message successfully stored.",
            message.storeMessage(testFile));

    assertTrue(Files.exists(testFile));

    String storedContent = Files.readString(testFile);

    assertTrue(storedContent.contains("Store this message"));
    assertTrue(storedContent.contains("00:0:STOREMESSAGE"));

    assertEquals(0, message.returnTotalMessages());
  }

}

