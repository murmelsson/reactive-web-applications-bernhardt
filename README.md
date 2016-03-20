# reactive-web-applications-bernhardt

Using the Early-Release pdf copy, starting reading this 19.03.2016 (d-m-y).

Note that I local-machine copy-paste conf/application.conf excluding things like secret API keys, into a file called 
conf/application-dummy.conf, so that secret keys don't end up in a public repo.

So if you want to use this repo, you will need to rename the dummy.conf to the standard name, and add your own keys etc.

--
# Using Play 2.5.0
The book (#2.2.2) notes a bug in 2.4.x releases: https://github.com/playframework/playframework/pull/4826
- but since in March 2016 we have in project/plugins.sbt :
```
// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.0")
```
... so let's see if we can do without the workarounds also.

Seems that the old-school file mentioned in section #2.2.3 app/controllers/Application.scala has been replaced with app/controllers/HomeController.scala

So I am not going to switch back to Play 2.4.x, but it means there is going to be a side-project learning from this book,
here is an example from Listing 2.3 Retrieval of the Configuration, when compiling:
```
[warn] /home/murmeister/playapps/twitter-stream/app/controllers/HomeController.scala:32: method configuration in object Play is deprecated: inject the play.api.Environment instead
[warn]       apiKey <- Play.configuration.getString("twitter.apiKey")
```



