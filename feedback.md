# How to react to reports

| Case | Feedback | Flag? | Comment? |
| --- | --- | --- | --- |
| Guttenberg reports a plagiarism, and it is a correct find. | tp | x | o |
| Guttenberg reports a plagiarism, and it is not plagiarized. | fp | o | o |
| Guttenberg reports a plagiarism, but it is attributed to the original post. | tp | o | o |
| Guttenberg reports a repost, and CopyPastor clearly shows that they are perfect reposts. (i.e. automatic moderator flag) | tp | o | x |
| Guttenberg reports a repost, but CopyPastor shows that there are just formatting differences between the two versions. | tp | x | x |
| Guttenberg reports a repost, and CopyPastor shows that there are quite a few differences between the two versions. | fp | o | o |


## Texts for the flag

### Plagiarism
> This post is plagiarized from an other answer. [link to CopyPastor] #plagiarism

### Repost
We only flag reposts, if the user posted the same answer again, after we added a [comment](#auto-comments).
> The user is posting the same answer to lots of questions instead of flagging them as duplicate. Example: [link to CopyPastor] 

## Auto-comment

```
###[A] Duplicated answer to multiple questions
Please don't add [the same answer]([type here]) to multiple questions. Answer the best one and flag the rest as duplicates. See [Is it acceptable to add a duplicate answer to several questions?](https://meta.stackexchange.com/q/104227/347985)
```
