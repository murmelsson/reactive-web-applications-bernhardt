# Simple app - deploy it
We use Docker for the build, Jenkins for CI, and target-environment clever-cloud.com .

App - simple-app.
 
Localhost selenium testing - easy once you realise you need to add 
```"org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.53.0"``` into the build.sbt.

Docker build/run - easy. 
Jenkins - couldn't work out how to get Jenkins to install the actual xvfb-binaries,
but not really a showstopper since the actual app testing was done locally already.

clever-cloud - first deployment run failed due to out-of-memory-error during the staging task,
so increased instance size and then it went through fine.
http://app-effd1682-b796-4193-8fd7-44db2ebd9d05.cleverapps.io/ running on 15.04.2016,
probably will switch it off before the initial 20€-credit is used up.



