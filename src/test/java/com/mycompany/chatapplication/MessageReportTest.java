
package com.mycompany.chatapplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class MessageReportTest {

    @TempDir
    Path directory;

    private Path json;
    private MessageReport report;

    @BeforeEach
    void prepare() {
        Message.clearSentMessages();
        json = directory.resolve("messages.json");
        report = new MessageReport(json, "Test Sender");
    }

    private Message message(String id, String recipient,
                            String text, int number) {
        return new Message(id, recipient, text, number);
    }

    private void populate() throws IOException {

        // Message 1 - Sent
        assertEquals(
                "Message successfully sent.",
                message(
                        "1234567890",
                        "+27834557896",
                        "Did you get the cake?",
                        0
                ).SentMessage(1)
        );

        // Message 2 - Stored
        assertEquals(
                "Message successfully stored.",
                message(
                        "2345678901",
                        "+27838884567",
                        "Where are you? You are late! I have asked you to be on time.",
                        1
                ).storeMessage(json)
        );

        // Message 3 - Disregarded
        assertEquals(
                "Press 0 to delete the message.",
                message(
                        "3456789012",
                        "+27834484567",
                        "Yohoooo, I am at your gate.",
                        2
                ).SentMessage(2)
        );

        // Message 4 - Sent
        // Required lecturer test ID: 0838884567
        // A separate valid recipient is used for this test fixture.
        assertEquals(
                "Message successfully sent.",
                message(
                        "0838884567",
                        "+27718693002",
                        "It is dinner time !",
                        3
                ).SentMessage(1)
        );

        // Message 5 - Stored
        assertEquals(
                "Message successfully stored.",
                message(
                        "5678901234",
                        "+27838884567",
                        "Ok, I am leaving without you.",
                        4
                ).storeMessage(json)
        );
    }

    @Test
    void sentArrayHasExpectedMessages() throws IOException {
        populate();

        assertEquals(
                List.of(
                        "Did you get the cake?",
                        "It is dinner time !"
                ),
                report.sentMessages()
                        .stream()
                        .map(Message::getMessageText)
                        .toList()
        );
    }

    @Test
    void disregardedArrayHasExpectedMessage() throws IOException {
        populate();

        assertEquals(
                "Yohoooo, I am at your gate.",
                report.disregardedMessages()
                        .get(0)
                        .getMessageText()
        );
    }

    @Test
    void storedArrayLoadsFromJson() throws IOException {
        populate();

        assertEquals(
                2,
                report.storedMessages().size()
        );

        assertEquals(
                "Where are you? You are late! I have asked you to be on time.",
                report.storedMessages()
                        .get(0)
                        .getMessageText()
        );
    }

    @Test
    void idsAndHashesArePopulated() throws IOException {
        populate();

        assertEquals(5, report.messageIds().size());
        assertEquals(5, report.messageHashes().size());

        assertTrue(report.messageIds().contains("0838884567"));
    }

    @Test
    void longestStoredMessageMatchesRubric() throws IOException {
        populate();

        assertEquals(
                "Where are you? You are late! I have asked you to be on time.",
                report.longestStoredMessage()
                        .orElseThrow()
                        .getMessageText()
        );
    }

    @Test
    void searchByIdFindsRecipientAndMessage() throws IOException {
        populate();

        Message result = report.searchById("0838884567")
                .orElseThrow();

        assertEquals(
                "+27718693002",
                result.getRecipientCell()
        );

        assertEquals(
                "It is dinner time !",
                result.getMessageText()
        );
    }

    @Test
    void recipientSearchFindsSentAndStored() throws IOException {
        populate();

        List<Message> results =
                report.searchByRecipient("+27838884567");

        assertEquals(2, results.size());

        assertEquals(
                List.of(
                        "Where are you? You are late! I have asked you to be on time.",
                        "Ok, I am leaving without you."
                ),
                results.stream()
                        .map(Message::getMessageText)
                        .toList()
        );
    }

    @Test
    void deletionByHashPersistsToJson() throws IOException {
        populate();

        String hash = report.storedMessages()
                .get(0)
                .getMessageHash();

        assertEquals(
                "Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                report.deleteByHash(hash)
        );

        assertEquals(
                1,
                report.storedMessages().size()
        );

        assertFalse(
                Files.readString(json).contains("Where are you?")
        );

        assertTrue(
                report.searchById("2345678901").isEmpty()
        );
    }

    @Test
    void reportContainsRequiredFields() throws IOException {
        populate();

        String result = report.fullReport();

        assertTrue(result.contains("Message ID:"));
        assertTrue(result.contains("Message Hash:"));
        assertTrue(result.contains("Sender:"));
        assertTrue(result.contains("Recipient:"));
        assertTrue(result.contains("Message:"));

        assertTrue(result.contains("Did you get the cake?"));
        assertTrue(result.contains("It is dinner time !"));
        assertTrue(result.contains("Ok, I am leaving without you."));
    }

    @Test
    void jsonEscapingRoundTrips() throws IOException {
        String text = "He said \"hello\" \\ again\nnext line";

        message(
                "1234567890",
                "+27838884567",
                text,
                0
        ).storeMessage(json);

        assertEquals(
                text,
                report.storedMessages()
                        .get(0)
                        .getMessageText()
        );
    }

    @Test
    void emptyFileAndMissingSearchAreHandled()
            throws IOException {

        assertTrue(report.storedMessages().isEmpty());

        assertTrue(
                report.searchById("9999999999").isEmpty()
        );

        assertEquals(
                "No stored messages.",
                report.storedSendersAndRecipients()
        );
    }

    @Test
    void malformedJsonIsRejected() throws IOException {
        Files.writeString(json, "[{invalid}]");

        assertThrows(
                IOException.class,
                report::storedMessages
        );
    }
}
