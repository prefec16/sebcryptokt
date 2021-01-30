# sebcryptokt

A Kotlin implementation of all cryptographic methods required to spoof the usage of 
[SafeExamBrowser](https://safeexambrowser.org/news_en.html). Also includes a config parser. 

## How does SafeExamBrowser's crypto work?

SafeExamBrowser uses a config that determines which settings of the computer should be accessible after the browser has been launched, which should be disabled, etc.
This config can be downloaded from the Moodle course by going to `/mod/quiz/accessrule/seb/config.php?cmid=%QUIZ_ID%` where `%QUIZ_ID%` is the id paramter of the exam page. 
The config is in Apple's plist format (wtf?), and is then converted by the browser to a JSON object with sorted keys. Also, the `originatorVersion` key is removed for some reason. 
After that, the JSON object is converted to String and hashed using `SHA-256`. This is the `Configuration Key`. 
Using this `Configuration Key`, the `Browser Exam Key` is calculated by concating the hash of the certificate used to sign the SEB binary (which can easily extracted with [this nifty tool](https://github.com/prefec16/extract-signature-hash)), the version and the `Configuration Key`, and then hashing it using `HMAC-SHA256`. The salt for this computation is either an empty unsigned byte array if the `examKeySalt` key does not exist in the config, or the `examKeySalt` base64 decoded to an unsigned byte array. 

On every request, the browser takes the `request url` and creates the `X-SafeExamBrowser-ConfigKeyHash` header by concating the `request url` and the `Configuration Key` and hashing the result using `SHA-256`. It also generates the `X-SafeExamBrowser-RequestHash` by doing the same thing, exept now the `Browser Exam Key` is used instead of the `Configuration Key`. These two headers are then sent to the quizpage. 

### In short:

* `/mod/quiz/accessrule/seb/config.php?cmid=%QUIZ_ID%` -> to `sortedJsonConfig`
* Remove `originatorVersion` from `sortedJsonConfig`
* `Configuration Key` = `configKey` = `sha256(sortedJsonConfig)`
* `sortedJsonConfig["examKeySalt"] != null` then `salt = base64decode(examKeySalt)` else `salt = new byte[0]`
* `Browser Exam Key` = `browserExamKey` = `hmacSha256(certSignature + sebVersion + configKey, salt)`
* `X-SafeExamBrowser-ConfigKeyHash` = `sha256(requestUrl + configKey)`
* `X-SafeExamBrowser-RequestHash` = `sha256(requestUrl + browserExamKey)`

## Use via

[![](https://jitpack.io/v/prefec16/sebcryptokt.svg)](https://jitpack.io/#prefec16/sebcryptokt)



