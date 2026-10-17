
package com.mycompany.chatapplication;

import java.util.Scanner;

public class ChatApplication {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Message.setInputScanner(scanner);

        System.out.println("==========================================");
        System.out.println("             CHAT APPLICATION");
        System.out.println("==========================================");
        System.out.println();

        // REGISTRATION
        System.out.println("REGISTER ACCOUNT");
        System.out.println("----------------");

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your South African cell phone number: ");
        String cellPhoneNumber = scanner.nextLine();

        Login login = new Login(
                firstName,
                lastName,
                username,
                password,
                cellPhoneNumber
        );

        System.out.println();
        System.out.println("REGISTRATION RESULT");
        System.out.println("-------------------");
        System.out.println(login.registerUser());

        if (!login.checkUserName()
                || !login.checkPasswordComplexity()
                || !login.checkCellPhoneNumber()) {

            System.out.println(
                    "Please correct your registration details and try again.");
            return;
        }

        System.out.println();
        System.out.println("Registration successful.");

        // LOGIN
        System.out.println();
        System.out.println("LOGIN");
        System.out.println("-----");

        System.out.print("Enter your username: ");
        String loginUsername = scanner.nextLine();

        System.out.print("Enter your password: ");
        String loginPassword = scanner.nextLine();

        boolean loginResult = login.loginUser(
                loginUsername, loginPassword);

        System.out.println();
        System.out.println(login.returnLoginStatus());

        if (!loginResult) {
            System.out.println(
                    "Login unsuccessful. QuickChat cannot be accessed.");
            return;
        }

        // QUICKCHAT
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        Message.clearSentMessages();

        int messageLimit = readMessageLimit(scanner);
        int messagesEntered = 0;
        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("========== QUICKCHAT MENU ==========");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("====================================");

            System.out.print("Choose an option: ");
            String option = scanner.nextLine().trim();

            switch (option) {

                case "1":

                    if (messagesEntered >= messageLimit) {
                        System.out.println(
                                "You have reached the message entry limit of "
                                + messageLimit + ".");
                        break;
                    }

                    while (messagesEntered < messageLimit) {

                        System.out.println();
                        System.out.println(
                                "Message " + (messagesEntered + 1)
                                + " of " + messageLimit);

                        // RECIPIENT VALIDATION
                        String recipient;

                        while (true) {
                            System.out.print(
                                    "Enter recipient number in international format "
                                    + "(e.g. +27718693002): ");

                            recipient = scanner.nextLine().trim();

                            Message validationMessage = new Message(
                                    recipient, "", messagesEntered);

                            String validationResult =
                                    validationMessage.checkRecipientCell();

                            System.out.println(validationResult);

                            if (validationResult.equals(
                                    "Cell phone number successfully captured.")) {
                                break;
                            }
                        }

                        // MESSAGE LENGTH VALIDATION
                        String text;

                        while (true) {
                            System.out.print(
                                    "Enter your message (maximum 250 characters): ");

                            text = scanner.nextLine();

                            if (text.length() <= 250) {
                                break;
                            }

                            System.out.println(
                                    "Message exceeds 250 characters by "
                                    + (text.length() - 250)
                                    + "; please reduce the size.");
                        }

                        // CREATE MESSAGE
                        Message message = new Message(
                                recipient, text, messagesEntered);

                        if (!message.checkMessageID()) {
                            System.out.println(
                                    "Message ID is invalid. Please try again.");
                            continue;
                        }

                        System.out.println();
                        System.out.println("Message ready to send.");
                        System.out.println(
                                "Message ID generated: "
                                + message.getMessageID());

                        // SEND, DISCARD OR STORE
                        String result = message.SentMessage();

                        System.out.println(result);

                        if (result.equals("Message successfully sent.")) {

                            System.out.println();
                            System.out.println("MESSAGE DETAILS");
                            System.out.println("----------------");

                            System.out.println(
                                    "Message ID: " + message.getMessageID());

                            System.out.println(
                                    "Message Hash: " + message.getMessageHash());

                            System.out.println(
                                    "Recipient: " + message.getRecipientCell());

                            System.out.println(
                                    "Message: " + message.getMessageText());
                        }

                        // A valid message has been processed.
                        // Send, disregard and store each count as an entry.
                        if (result.equals("Message successfully sent.")
                                || result.equals(
                                        "Press 0 to delete the message.")
                                || result.equals(
                                        "Message successfully stored.")) {

                            messagesEntered++;
                        }

                        if (messagesEntered >= messageLimit) {
                            System.out.println();
                            System.out.println(
                                    "You have reached your message entry limit.");
                            break;
                        }

                        System.out.print(
                                "Enter another message? (Y/N): ");

                        String again = scanner.nextLine().trim();

                        if (!again.equalsIgnoreCase("Y")) {
                            break;
                        }
                    }

                    break;

                case "2":

                    System.out.println("Coming Soon.");
                    break;

                case "3":

                    running = false;
                    break;

                default:

                    System.out.println(
                            "Invalid option. Please choose 1, 2, or 3.");
                    break;
            }
        }

        // TOTAL SENT MESSAGES
        Message summary = new Message(
                "+27718693002", "summary", 0);

        System.out.println();
        System.out.println("====================================");
        System.out.println(
                "Total messages sent: "
                + summary.returnTotalMessages());

        System.out.println(
                "Thank you for using QuickChat. Goodbye!");
        System.out.println("====================================");
    }

    /** Reads the maximum number of messages to enter. */
    private static int readMessageLimit(Scanner scanner) {

        while (true) {

            System.out.print(
                    "How many messages would you like to enter? ");

            String input = scanner.nextLine().trim();

            try {
                int limit = Integer.parseInt(input);

                if (limit >= 0) {
                    return limit;
                }

            } catch (NumberFormatException ex) {
                // Ask for input again.
            }

            System.out.println(
                    "Please enter a whole number that is zero or greater.");
        }
    }
}
