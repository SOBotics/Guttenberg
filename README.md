[![Build Status](https://travis-ci.org/SOBotics/Guttenberg.svg?branch=master)](https://travis-ci.org/SOBotics/Guttenberg)

## What is Guttenberg?

Guttenberg is a bot that searches for plagiarism or duplicated answers on Stack Overflow. It's currently running in [SOBotics][1] under the user [Guttenberg][2].


## Implementation

Every 60 seconds, Guttenberg fetches the most recent answers (the "targets") on Stack Overflow. For each of these answers, possibly related posts (for example answers to related questions) are collected. Each related post will be split into the full markdown, code-blocks, plaintext paragraphs and blockquotes. These parts are compared with the "target" and the [Jaro-Winkler distance](https://en.wikipedia.org/wiki/Jaro–Winkler_distance) will be calculated. If one of the comparison reaches a certain score (which is defined in `general.properties`), a message like this will be posted in chat:

![](https://i.imgur.com/HhwCWJr.png)


## Accuracy

At the moment, we are testing a very early version of the bot. That's why we get lot's of false positives. But we did already find copied answers without proper attribution and users posting the same answer on multiple questions.


## Plans for the future

### Feedback

We have no statistics about the amount of tps and fps yet. In a future release, we want to allow users replying to a report to give feedback.


  [1]: http://chat.stackoverflow.com/rooms/111347/sobotics
  [2]: http://stackoverflow.com/users/7418352/guttenberg
  [3]: https://github.com/SOBotics/Guttenberg
