# Guidelines for contributing to Guttenberg

We are always happy when someone wants to help us on this project, but please read these short guidelines before you jump in.

## Opening issues

If you have found a bug or have an idea for an enhancement, feel free to open an issue. Please try to be as detailed as possible.
When reporting a bug, please always give us the date and time when this bug occurred, as this will help us to identify the version and any available logs. A link to the chat message is very useful as well, if you have it.

## Contributing code

Code contributions are always welcome. To keep the project clean, please generally follow the code style you see used.

**It's highly recommended to use [IntelliJ IDEA](https://www.jetbrains.com/idea/).**

### What issues can I work on?

In general, feel free to look into issues that don't have anyone assigned.
Before you start (especially if it's your first contribution), please contact [FelixSFD](https://chat.stackoverflow.com/users/4687348/felixsfd) or any other room-owner in [chat](https://sobotics.org/chat) for some guidance.

### Branches

We are using [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) in this repository. Every tagged commit on the `master`-branch will automatically be built and deployed.
Issues need to be worked on in a new branch (usually branched from `develop`).

The name of the branch should contain the number of the issue, a very short description and whether the issue is a bug or enhancement.

**Examples:** `feature/1337-new-test-command` or `bugfix/1337-test-command-broken`

### Committing

Each commit should start with the number of the issue followed by a `:`. Only for really small hotfixes (like typos), we don't need that. In that case, use `fixed #[issue number]`.
If you're just fixing a typo in a .md file and there is no issue open about that, just describe what typo/grammar error you fixed.

### Code style

It's recommended to use IntelliJ for developing Guttenberg, since the project already contains the necessary settings for the code style.

These are the most important guidelines:

#### Indentation

We are using **2 spaces** per level of indentation.

#### Whitespaces

Lines should not have trailing whitespaces. Empty lines should not have whitespaces at all.

#### Encoding

Every file should be encoded in `UTF-8`. We are using UNIX line-breaks (`\n`).

#### Methods

The opening brace `{` should be in the same line as the method declaration.

Example:
```java
public void test(String param, int otherParam) {

}
```

For better readability, the whitespaces should be used as shown in the example above.

#### if/for/while/...

These statements should be followed by a whitespace before the opening brace.

```java
if (a == b) {

}
```

### Naming conventions

#### Classes

Class-names start with an uppercase letter and only contain letters from A-Z. To combine multiple words, use `PascalCase`.
**Examples:** `Post`, `RunnerService`, `PostMatch`

#### Methods

Method-names always start with a lowercase letter (i.e. use `camelCase`) and don't contain special characters like `_`. Make sure that the name clearly shows what this method is doing.

### Comments

If you add a new class or method, please add a Javadoc comment.
Additionally, for longer functions, it's useful to add comments describing what certain pieces of code are supposed to do (but you don't need to comment trivial things like "checking if `variable` is `null`" - if it's not obvious, comment it)

When making changes to an existing function, please add a comment with the number of the issue you are working on.

## Fixing grammar and typos

Since most of us are not native speakers in English, there are plenty of grammar issues or typos. If you find some, please feel free to fix them and send in a pull request.
You don't need to open an issue first if you're just editing .md files.
