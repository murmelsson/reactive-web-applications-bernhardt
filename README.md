# reactive-web-applications-bernhardt
Chapter8 is about using Scala.js and AngularJS in a Play application.

--
You might have to add the annotation ```@js.native     //i.e. scala.scalajs.js.native```
to keep the compiler happy at some places, e.g.:
```
// Define a facade for the object returned by service-constructor
@js.native
trait WebsocketDataStream extends js.Object {
  // Define type-safe wrapper for send-method (which returns an HttpPromise):
  def send[T](data: js.Any): HttpPromise[T] = js.native
```

--
If the sbt-command "test" fails with something like: 
```[error] (client/test:loadedTestFrameworks) Exception while running JS code: ReferenceError: "window" is not defined.```

then this means you need to remember to include 
```RuntimeDOM % "test"```
as a jsDependencies member in ```build.sbt``` (so that there is a "headless" DOM to test against, as you are not opening a browser manually or automagically here).

...and if after that the sbt-command "test" still fail like this:
```[error] (client/test:loadedTestFrameworks) Exception while running JS code: ReferenceError: "java" is not defined. (env.rhino.js#1391)```

then this means you are running Rhino instead of PhantomJS. Assuming you have PhantomJS installed on your development-computer, 
you can add the "switch on Node.js by switching off Rhino"-trick into ```build.sbt``` just after: 
```scalaJSStage in Global := FastOptStage```
...i.e. you need to add this:
```scalaJSUseRhino in Global := false```

Then hopefully the test runs ok, i.e. you get some success-yippee output when running sbt-command "test".


