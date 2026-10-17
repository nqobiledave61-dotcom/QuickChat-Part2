# QuickChat - Part 2

This is the existing Maven/NetBeans ChatApplication project extended for Part 2.

## Requirements
- JDK 21 or newer
- Apache Maven

## Run in NetBeans
1. Open the `ChatApplication` folder as a Maven project.
2. Run `com.mycompany.chatapplication.ChatApplication`.
3. Register a valid account, then log in with the same username and password.
4. Enter the message limit and use the QuickChat menu.

## Run tests
From this folder, run:

```bash
mvn clean test
```

GitHub Actions runs the Maven unit tests on pushes and pull requests via `.github/workflows/maven-tests.yml`.

## JSON storage
Choosing option 3 for a message stores it in `messages.json` in the application's current working directory.

## Recipient validation note
The assignment's supplied examples conflict: one is in international format (`+27718693002`) while another is local format (`08575975889`), even though the validation requirement says an international code is required. This implementation follows the explicit validation rule and accepts `+27` followed by nine digits. The second example (`08575975889`) contains 11 digits and does not match the standard South African local-number length, so confirm whether the assignment has a typo. For a valid test number, use `+27857597589`.
