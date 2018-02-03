# Guidelines for contributing to Guttenberg

We are always happy, when some wants to help us on this project, but please read these short guidelines before.

## Opening issues

If you have found a bug or have an idea for an enhancement, feel free to open an issue. Please try to be as detailed as possible.
When reporting a bug, please always give us the date and time, when this bug occurred. A link to the chat-message would be very useful as well.

## Contributing code

Code-contributions are always welcome. To keep the project clean, please make sure to follow a few conventions.

### What issues can I work on?

In general, you can have a look at those issues, which don't have an user assigned.
Before you start (especially, if it's your first contribution!), please contact [FelixSFD](https://chat.stackoverflow.com/users/4687348/felixsfd) or any other room-owner in [chat](https://sobotics.org/chat).

### Branches

We are using [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) in this repository. Every tagged commit on the `master`-branch will automatically be built and deployed.
Issues need to be worked on in a new branch (usually branched from `develop`).

The name of the branch should contain the number of the issue, a very short description and whether the issue is a bug or enhancement.

**Examples:** `feature/1337-new-test-command` or `bugfix/1337-test-command-broken`

### Committing

Each commit should start with the number of the issue followed by a `:`. Only for really small hotfixes (like typos), we don't need that. In that case, use `fixed #[issue number]`.
If you're just fixing a typo in a .md-file and there is no issue open about that, just describe, what typo/grammar you fixed.

### Naming conventions

#### Classes

Class-names start with an uppercase letter and only contain letters from A-Z. To combine multiple words, use camel-case.
**Examples:** `Post`, `RunnerService`, `PostMatch`

#### Methods

Method-names always start with a lowercase letter and don't contain special-characters like `_`. Make sure that the name clearly shows, what this method is doing.

### Comments

If you add a new class or method, please add a Javadoc-comment.
Additionally, for longer functions, it's useful to add comments describing what certain pieces of code are supposed to do. (but you don't need to comment trivial things like "checking if `variable` is `null`")

When making changes to an existing function, it would be nice, if you leave a comment with the number of the issue, you are working on.

## Fixing grammar and typos

Since most of us are not native speakers in English, there are texts with wrong grammar or typos. It would be nice, if you fix them, when you found them, by forking the repository and creating a pull-request.
You don't need to open an isue first, if you're just editing .md-files.
