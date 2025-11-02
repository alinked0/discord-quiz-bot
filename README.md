# Discord Quiz Bot

A feature-rich, customizable Discord bot for creating, managing, and playing quiz games with support for custom question lists, tags and user statistics.

---

## Features

- **Create and manage question lists:** Users can create, import, rename, and view their own quiz lists.
- **Tagging system:** Organize question lists with custom tags and emojis for easy filtering.
- **Quiz gameplay:** Start quizzes, answer questions, and receive explanations and scoring.
- **User statistics:** Track games played, points earned, and user preferences.
- **Command-line and Discord support:** Run the bot in test mode or production mode.
- **Rich command system:** Slash commands and message commands, with detailed help and examples.
- **JSON import/export:** Easily back up or share question lists.

---

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven**
- **A Discord bot token** ([how to get one](https://discord.com/developers/applications))

### Build

Clone the repository and build with Maven:

```sh
git clone https://github.com/alinked0/discord-quiz-bot
cd discord-quiz-bot
mvn package
```

This will generate the bot jar in the `target/` directory.

### Run

```sh
java -jar target/discordquizbot-1.0-SNAPSHOT-jar-with-dependencies.jar BOTTOKEN USERID \[TESTCHANNELID TESTGUILDID\]
```

- **BOTTOKEN**: Your Discord bot token.
- **TESTGUILDID**: Discord server (guild) ID for testing.
- **TESTCHANNELID**: Channel ID for test messages.
- **USERID**: Your Discord user ID (for test mode).

If you run without arguments, the bot will prompt for your token.

---

## Usage

### Command Overview

The bot supports both **slash commands** and **message commands** (e.g., `q!help`). Use `/help` or `q!help` to see all available commands and their usage.

#### Example Commands

- `q!createlist { ... }` — Create a new question list from JSON.
- `q!addlist { ... }` — Add a question list to your collection.
- `q!collection` — List all your question lists.
- `q!view <list-id>` — View a question list in a readable format.
- `q!start <list-id>` — Start a quiz from a list.
- `q!addtag <tag-name> <list-id>` — Tag a list for easier organization.
- `q!createtag <tag-name> <emoji>` — Create a new tag.
- `q!renamelist <list-id> <new-name>` — Rename a question list.
- `q!setprefix <prefix>` — Change your command prefix.
- `q!userinfo [user-id]` — Show user info in JSON format.

For a full list and detailed help, use `q!help` or `/help`.

---

## Question List Format

Question lists are stored as JSON files. Example:

```json
{
  "ownerId": "123456789012345678",
  "name": "Sample Quiz",
  "id": "abcdefg",
  "timeCreatedMillis": 1700000000000,
  "emojiPerTagName": { "science": "🧪" },
  "questions": [
	{
	  "question": "What is the chemical symbol for water?",
	  "explication": "H2O is the chemical formula for water.",
	  "imageSrc": null,
	  "options": [
		{ "text": "H2O", "isCorrect": true, "explication": null },
		{ "text": "CO2", "isCorrect": false, "explication": null }
	  ]
	}
  ]
}
```

You can import/export these lists using the bot commands.

---

## Development

### Project Structure

- `src/main/java/com/linked/quizbot/` — Main bot source code.
- `src/main/resources/lists/` — Default and user-created question lists.
- `src/main/resources/user-data/` — User data and statistics.
- `src/test/java/` — Unit tests.

### Key Classes

- [`QuestionList`](src/main/java/com/linked/quizbot/utils/QuestionList.java): Manages a collection of questions, supports JSON import/export, tagging, and metadata.
- [`BotCommand`](src/main/java/com/linked/quizbot/commands/BotCommand.java): Abstract base for all commands.
- [`CommandOutput`](src/main/java/com/linked/quizbot/commands/CommandOutput.java): Handles command responses.
- [`Users`](src/main/java/com/linked/quizbot/utils/Users.java): User management and data persistence.

### Running Tests

```sh
mvn test
```

---

## Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -am 'Add new feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a pull request.

---

## License

This project is licensed under the MIT License.

---

## Acknowledgements

- Built with [JDA](https://github.com/DV8FromTheWorld/JDA) for Discord integration.
- Inspired by community quiz bots and open-source projects.

---

## Troubleshooting

- **Bot not responding?**  
  Check your bot token, permissions, and that the bot is invited to your server.
- **Command not recognized?**  
  Use the correct prefix (default `q!`), or check your custom prefix with `q!setprefix`.
- **Need help?**  
  Use `q!help` or `/help` for command documentation.

---

Happy quizzing!