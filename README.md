[![Build Status](https://travis-ci.org/SOBotics/Guttenberg.svg?branch=master)](https://travis-ci.org/SOBotics/Guttenberg)

## What is Guttenberg?

Guttenberg is a bot that searches for plagiarism or duplicated answers on Stack Overflow. It's currently running in [SOBotics][1] under the user [Guttenberg][2].


## Implementation

Every 60 seconds, Guttenberg fetches the most recent answers (the "targets") on Stack Overflow. For each of these answers, possibly related posts (for example answers to related questions) are collected. All those posts will be checked for different characteristics (such as the [Jaro-Winkler distance](https://en.wikipedia.org/wiki/Jaro–Winkler_distance) of the posts). If at least one of the characteristics mets the requirements, a message like this will be posted in chat:

![sample chat message](http://i.stack.imgur.com/hB7Hz.png)


## Accuracy

We have no statistics about the accuracy yet.

## Plans for the future

### Feedback statistics

Although users can send feedback, we are not logging it yet, so we can't provide statistics. In the future, this should be done by a dashboard like Sentinel.


  [1]: http://chat.stackoverflow.com/rooms/111347/sobotics
  [2]: http://stackoverflow.com/users/7418352/guttenberg
  [3]: https://github.com/SOBotics/Guttenberg
