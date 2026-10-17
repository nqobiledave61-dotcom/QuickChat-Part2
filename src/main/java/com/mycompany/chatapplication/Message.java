
package com.mycompany.chatapplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Represents one QuickChat message and its validation and storage operations. */
public class Message {

    private static final String RECIPIENT_ERROR =
            "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";

    private static final String RECIPIENT_SUCCESS =
            "Cell phone number successfully captured.";

    private static final List<Message> SENT_MESSAGES = new ArrayList<>();

    private static Scanner inputScanner = new Scanner(System.in);

    private final String messageID;
    private final int messageNumber;
    private final String recipientCell;
    private final String messageText;
    private String messageHash;

    /** Creates a message with a randomly generated ten-digit ID. */
    public Message(String recipientCell, String messageText, int messageNumber) {
        this(generateMessageID(), recipientCell, messageText, messageNumber);
    }

    /** Constructor with an explicit ID, useful for unit tests. */
    public Message(String messageID, String recipientCell,
                   String messageText, int messageNumber) {
        this.messageID = messageID == null ? "" : messageID.trim();
        this.recipientCell = recipientCell == null
                ? "" : recipientCell.trim();
        this.messageText = messageText == null ? "" : messageText;
        this.messageNumber = messageNumber;
    }

    /** Generates a random ten-digit message ID. */
    private static String generateMessageID() {
        long id = ThreadLocalRandom.current().nextLong(
                1_000_000_000L, 10_000_000_000L);
        return Long.toString(id);
    }

    /** Allows the application and this class to share console input. */
    public static void setInputScanner(Scanner scanner) {
        if (scanner != null) {
            inputScanner = scanner;
        }
    }

    /** Checks that the message ID contains exactly ten numeric digits. */
    public boolean checkMessageID() {
        return messageID.matches("[0-9]{10}");
    }

    /** Validates a South African international number: +27 followed by nine digits. */
    public String checkRecipientCell() {
        if (recipientCell.matches("\\+27[0-9]{9}")) {
            return RECIPIENT_SUCCESS;
        }
        return RECIPIENT_ERROR;
    }

    /** Messages of up to 250 characters are valid. */
    public String checkMessageLength() {
        if (messageText.length() <= 250) {
            return "Message ready to send.";
        }

        return "Message exceeds 250 characters by "
                + (messageText.length() - 250)
                + "; please reduce the size.";
    }

    /** Creates the hash: first two ID digits:number:first word + last word. */
    public String createMessageHash() {
        String firstTwoIDDigits = messageID.length() >= 2
                ? messageID.substring(0, 2) : messageID;

        Matcher matcher = Pattern.compile("[A-Za-z0-9]+")
                .matcher(messageText);

        List<String> words = new ArrayList<>();

        while (matcher.find()) {
            words.add(matcher.group());
        }

        String firstWord = words.isEmpty() ? "" : words.get(0);
        String lastWord = words.isEmpty()
                ? "" : words.get(words.size() - 1);

        messageHash = (firstTwoIDDigits + ":" + messageNumber + ":"
                + firstWord + lastWord).toUpperCase(Locale.ROOT);

        return messageHash;
    }

    /** Prompts the user to choose an action for the message. */
    public String SentMessage() {
        System.out.println("Choose what to do with this message:");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Enter your choice: ");

        String choice = inputScanner.nextLine().trim();

        int option;
        try {
            option = Integer.parseInt(choice);
        } catch (NumberFormatException ex) {
            return "Invalid choice. Please select 1, 2, or 3.";
        }

        return SentMessage(option);
    }

    /** Processes the send, disregard, or store action. */
    public String SentMessage(int option) {
        switch (option) {
            case 1:
                if (!checkMessageID()
                        || !RECIPIENT_SUCCESS.equals(checkRecipientCell())
                        || !"Message ready to send.".equals(checkMessageLength())) {
                    return "Message could not be sent. Please check the message ID, recipient number, and message length.";
                }

                createMessageHash();
                SENT_MESSAGES.add(this);
                return "Message successfully sent.";

            case 2:
                return "Press 0 to delete the message.";

            case 3:
                return storeMessage();

            default:
                return "Invalid choice. Please select 1, 2, or 3.";
        }
    }

    /** Returns the details of all successfully sent messages. */
    public String printMessages() {
        StringBuilder output = new StringBuilder();

        for (Message message : SENT_MESSAGES) {
            output.append("Message ID: ")
                    .append(message.messageID)
                    .append(System.lineSeparator());

            output.append("Message Hash: ")
                    .append(message.getMessageHash())
                    .append(System.lineSeparator());

            output.append("Recipient: ")
                    .append(message.recipientCell)
                    .append(System.lineSeparator());

            output.append("Message: ")
                    .append(message.messageText)
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return output.toString();
    }

    /** Returns the total number of successfully sent messages. */
    public int returnTotalMessages() {
        return SENT_MESSAGES.size();
    }

    /** Stores this message in a JSON array in messages.json. */
    public String storeMessage() {
        Path file = Path.of("messages.json");

        String hash = messageHash == null
                ? createMessageHash() : messageHash;

        String object = "{\"messageID\":\""
                + jsonEscape(messageID)
                + "\",\"messageHash\":\""
                + jsonEscape(hash)
                + "\",\"messageNumber\":"
                + messageNumber
                + ",\"recipient\":\""
                + jsonEscape(recipientCell)
                + "\",\"message\":\""
                + jsonEscape(messageText)
                + "\"}";

        try {
            String existing = Files.exists(file)
                    ? Files.readString(file, StandardCharsets.UTF_8).trim()
                    : "";

            String updated;

            if (existing.isEmpty() || "[]".equals(existing)) {
                updated = "[" + System.lineSeparator()
                        + "  " + object + System.lineSeparator() + "]";
            } else if (existing.startsWith("[")
                    && existing.endsWith("]")) {

                String contents = existing.substring(
                        1, existing.length() - 1).trim();

                updated = "[" + System.lineSeparator()
                        + "  " + contents + ","
                        + System.lineSeparator()
                        + "  " + object
                        + System.lineSeparator() + "]";
            } else {
                return "Message could not be stored because messages.json is not a valid JSON array.";
            }

            Files.writeString(file, updated + System.lineSeparator(),
                    StandardCharsets.UTF_8);

            return "Message successfully stored.";

        } catch (IOException ex) {
            return "Message could not be stored: " + ex.getMessage();
        }
    }

    /** Escapes special characters so message content can be stored in JSON. */
    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }

        return escaped.toString();
    }

    /** Clears sent messages, primarily for unit testing. */
    public static void clearSentMessages() {
        SENT_MESSAGES.clear();
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipientCell() {
        return recipientCell;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash == null
                ? createMessageHash() : messageHash;
    }
}
