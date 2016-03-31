# reactive-web-applications-bernhardt
Chapter6 is about Akka Actors for a Twitter-based analytics service.

Did a fair bit of typing out the solution-code from github at:
https://github.com/manuelbernhardt/reactive-web-applications/tree/master/CH06

Also needed to set-up MongoDB correctly, and did some smoke-testing. 
With the smoke-test in a Scala 'console app', it's a bit tricky to close the
connection and driver, but as that is just a smoke-test, you can use Ctrl-C
once you have established that something works.
You need to make sure the mongod process is running before you can connect to MongoDB.
If all is good, then starting up the mongo shell (cmd: mongo) should work ok).
Smoke-test-code is included in the file HelloMongo.scala (located in this root directory, it's not part of the book-app)

To test, used both Chrome and Postman Client. Got the sample tweetId by Inspect Element
on tweets that have retweets that I could see in my feed.

Test samples, and screenshot of what success looks like, can be viewed in the files:
Ch6-test-tweet-samples.txt
Screenshot-working-app1.png


