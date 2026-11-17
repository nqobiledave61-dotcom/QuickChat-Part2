package com.mycompany.chatapplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads and rewrites the Part 2 JSON array without external dependencies. */
public final class MessageStore {
    private final Path file;

    public MessageStore(Path file) { this.file = file; }

    public List<Message> load() throws IOException {
        if (!Files.exists(file)) return new ArrayList<>();
        String json = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) return new ArrayList<>();
        Parser parser = new Parser(json);
        List<Map<String, String>> objects = parser.parseArray();
        List<Message> result = new ArrayList<>();
        for (Map<String, String> fields : objects) {
            try {
                String id = required(fields, "messageID");
                String recipient = required(fields, "recipient");
                String body = required(fields, "message");
                int number = Integer.parseInt(required(fields, "messageNumber"));
                Message message = new Message(id, recipient, body, number);
                message.setLoadedHash(fields.getOrDefault("messageHash", message.createMessageHash()));
                result.add(message);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid stored message: " + ex.getMessage(), ex);
            }
        }
        return result;
    }

    private static String required(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null) throw new IllegalArgumentException("Missing " + key);
        return value;
    }

    public void save(List<Message> messages) throws IOException {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (i > 0) json.append(",\n");
            json.append("  {\"messageID\":\"").append(escape(m.getMessageID()))
                .append("\",\"messageHash\":\"").append(escape(m.getMessageHash()))
                .append("\",\"messageNumber\":").append(m.getMessageNumber())
                .append(",\"recipient\":\"").append(escape(m.getRecipientCell()))
                .append("\",\"message\":\"").append(escape(m.getMessageText())).append("\"}");
        }
        json.append("\n]\n");
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        StringBuilder b = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                case '\b' -> b.append("\\b");
                case '\f' -> b.append("\\f");
                default -> { if (c < 32) b.append(String.format("\\u%04x", (int)c)); else b.append(c); }
            }
        }
        return b.toString();
    }

    /** Strict parser for JSON arrays containing flat objects of string/number fields. */
    private static final class Parser {
        private final String s;
        private int pos;
        Parser(String s) { this.s = s; }
        private void ws() { while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++; }
        private boolean take(char c) { ws(); if (pos < s.length() && s.charAt(pos) == c) { pos++; return true; } return false; }
        private void expect(char c) throws IOException { if (!take(c)) throw error("Expected '" + c + "'"); }
        private IOException error(String detail) { return new IOException("Invalid JSON at position " + pos + ": " + detail); }
        List<Map<String,String>> parseArray() throws IOException {
            List<Map<String,String>> rows = new ArrayList<>();
            expect('[');
            if (!take(']')) {
                do { rows.add(parseObject()); } while (take(','));
                expect(']');
            }
            ws();
            if (pos != s.length()) throw error("Unexpected trailing content");
            return rows;
        }
        private Map<String,String> parseObject() throws IOException {
            Map<String,String> row = new LinkedHashMap<>();
            expect('{');
            if (!take('}')) {
                do {
                    String key = string();
                    expect(':');
                    ws();
                    String value;
                    if (pos < s.length() && s.charAt(pos) == '"') value = string();
                    else {
                        int start = pos;
                        if (pos < s.length() && s.charAt(pos) == '-') pos++;
                        while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
                        if (start == pos || (s.charAt(start) == '-' && pos == start + 1)) throw error("Expected string or integer");
                        value = s.substring(start, pos);
                    }
                    row.put(key, value);
                } while (take(','));
                expect('}');
            }
            return row;
        }
        private String string() throws IOException {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') return b.toString();
                if (c < 32) throw error("Control character in string");
                if (c != '\\') { b.append(c); continue; }
                if (pos >= s.length()) throw error("Incomplete escape");
                char e = s.charAt(pos++);
                switch (e) {
                    case '"' -> b.append('"'); case '\\' -> b.append('\\');
                    case '/' -> b.append('/'); case 'n' -> b.append('\n');
                    case 'r' -> b.append('\r'); case 't' -> b.append('\t');
                    case 'b' -> b.append('\b'); case 'f' -> b.append('\f');
                    case 'u' -> {
                        if (pos + 4 > s.length()) throw error("Incomplete unicode escape");
                        try { b.append((char)Integer.parseInt(s.substring(pos, pos + 4), 16)); }
                        catch (NumberFormatException ex) { throw error("Invalid unicode escape"); }
                        pos += 4;
                    }
                    default -> throw error("Invalid escape");
                }
            }
            throw error("Unterminated string");
        }
    }
}
