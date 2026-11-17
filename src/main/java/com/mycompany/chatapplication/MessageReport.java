package com.mycompany.chatapplication;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Array-backed search, deletion and reporting operations for Part 3. */
public final class MessageReport {
    private final MessageStore store;
    private final String sender;

    public MessageReport(Path jsonFile, String sender) {
        this.store = new MessageStore(jsonFile);
        this.sender = sender;
    }

    public List<Message> sentMessages() { return Message.getSentMessages(); }
    public List<Message> disregardedMessages() { return Message.getDisregardedMessages(); }
    public List<Message> storedMessages() throws IOException { return store.load(); }
    public List<String> messageHashes() { return Message.getMessageHashes(); }
    public List<String> messageIds() { return Message.getMessageIds(); }

    public String storedSendersAndRecipients() throws IOException {
        StringBuilder b = new StringBuilder();
        for (Message m : storedMessages()) {
            b.append("Sender: ").append(sender).append(" | Recipient: ")
             .append(m.getRecipientCell()).append(System.lineSeparator());
        }
        return b.isEmpty() ? "No stored messages." : b.toString();
    }

    public Optional<Message> longestStoredMessage() throws IOException {
        return storedMessages().stream().max((a, b) ->
                Integer.compare(a.getMessageText().length(), b.getMessageText().length()));
    }

    /** Includes sent and stored messages to support the assignment's search test. */
    public Optional<Message> searchById(String id) throws IOException {
        return allSentAndStored().stream()
                .filter(m -> m.getMessageID().equals(id)).findFirst();
    }

    public List<Message> searchByRecipient(String recipient) throws IOException {
        return allSentAndStored().stream()
                .filter(m -> m.getRecipientCell().equals(recipient)).toList();
    }

    public String deleteByHash(String hash) throws IOException {
        List<Message> messages = storedMessages();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageHash().equalsIgnoreCase(hash)) {
                String body = messages.remove(i).getMessageText();
                store.save(messages);
                return "Message: \"" + body + "\" successfully deleted.";
            }
        }
        return "No stored message found for hash: " + hash;
    }

    public String fullReport() throws IOException {
        List<Message> messages = allSentAndStored();
        if (messages.isEmpty()) return "No sent or stored messages to report.";
        StringBuilder b = new StringBuilder("QUICKCHAT MESSAGE REPORT\n");
        for (Message m : messages) {
            b.append("Message ID: ").append(m.getMessageID()).append('\n')
             .append("Message Hash: ").append(m.getMessageHash()).append('\n')
             .append("Sender: ").append(sender).append('\n')
             .append("Recipient: ").append(m.getRecipientCell()).append('\n')
             .append("Message: ").append(m.getMessageText()).append("\n\n");
        }
        return b.toString();
    }

    private List<Message> allSentAndStored() throws IOException {
        List<Message> messages = new ArrayList<>(sentMessages());
        messages.addAll(storedMessages());
        return messages;
    }
}
