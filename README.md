# reactive-web-applications-bernhardt

Using the Early-Release pdf copy, starting reading this 19.03.2016 (d-m-y).

Note that I local-machine copy-paste conf/application.conf excluding things like secret API keys, into a file called 
conf/application-dummy.conf, so that secret keys don't end up in a public repo.

So if you want to use this repo, you will need to rename the dummy.conf to the standard name, and add your own keys etc.

--
# Using Play 2.4.6
The book (#2.2.2) notes a bug in 2.4.x releases: https://github.com/playframework/playframework/pull/4826
- but since in March 2016 we have in project/plugins.sbt (when generating the project using the "play-scala" in Activator:
```
// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.0")
```
... so my original idea was to just use Play 2.5.0 and change the code where needed

But that idea, ongoing in other branch or two of this repo, has hit some stormy weather, because you need to already be quite
expert in Play Framework to replace all the 2.5.0 - deprecated (compile or run-time failure) code with the new ways of
doing things.

So for the moment let's go with Play 2.4.6 (last release in 2.4.x series). As luck would have it, i had the sample "my-first-scala-play-app"
project generated from 2.4.6-template sitting around on laptop, so I first of all copied it into a new standalone project, then
got the basic Twitter-stream working (much easier with the right template), then did the necessary copy-pasting into this 2.4.6 branch, and push to repo etc.

(I cannot remember now which template I used back in January, two months is a long time in coding etc. But it was probably something like "just-play-scala", or maybe the old default "play-scala" before they went n started changing everything to match
the name-change to Lightbend... anyway I think "just-play-scala" is still available from Activator UI, and source code is on GitHub: https://github.com/julienrf/just-play-scala#master

Then after first push to this 2.4.6-branch, noticed how murmelsson was warbling in this README about how he was going to use only 2.5.0 for this learning project. Oops, fix that - fixed.

--
On Listing 2.10: seems that it is important to not leave a space between the two sets of brackets in index.scala.html:
```@(message: String)(implicit request: RequestHeader)```  - is ok
```@(message: String) (implicit request: RequestHeader)```  - compilation error when trying to create the WebSocket, 
because the RequestHeader needed by tweets()-method is not in scope. 



