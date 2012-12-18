# Sconduit

A Scala (and Java) interface to Phabricator's Conduit API.

# How to use it

The authentication handshake from Phabricator is scarcely documented right now.
Some Conduit methods require authentication and others don't.

You need a user's certificate to use authenticated methods. You then use the
certificate to call `conduit.connect` which will get you a `sessionKey` and a
`connectionID`. These should last for the remainder of the session.

There are several ways to get the certificate, but perhaps the easiest is to
call `conduit.getcertificate` and pass it a "host" parameter and a "token"
parameter. (`host` being the full URI (with protocol!) to the Phabricator
instance, and `token` being the user's current token from the instance's
`/conduit/token` page.

You generally want to save the user's username, certificate, and "host" value as
defined above.

Sconduit provides a convenience method to deal with not having a user's
certificate, which works like this:

```scala
import me.elrod.sconduit.ConduitClient
val certMap = ConduitClient.getCertificate(token, host)
```

...where `token` and `host` are as defined above. `certMap` is now a
`Map[String, String]` which contains two keys, `username` and `certificate`.

Sconduit also provides a convenience method for generating a `ConduitClient` with
authentication, once you have a valid username, certificate, and host.

```scala
import me.elrod.sconduit.ConduitClient
val client = ConduitClient.fromCertificate(username, certificate, apiURL)
```

Alternatively, you can use `fromCertificateMap` instead of `fromCertificate` and
just pass the result of `getCertificate` to it, along with the host.

```scala
import me.elrod.sconduit.ConduitClient
val client = ConduitClient.fromCertificateMap(
  ConduitClient.getCertificate(token, apiURL))
```

The main method to use to make calls to the API is, aptly named, `.call()`.
It works like this:

```scala
client.call(
  "conduit.ping",
  Map(
    "your" -> "parameters"
  ))
```

...and returns a Dispatch `Promise` which, when resolved, contains a `JValue`
provided by `org.json4s`. See the Dispatch and json4s documentation for details
on how to handle these.

If you don't want to get a `Promise` back, and would like to block, simply call
the response's `.apply()` method (which in Scala can be abbreviated to simply
`()`).

```scala
val result = client.call(
  "conduit.ping",
  Map(
    "your" -> "parameters"
  )).apply()

// Or... (exact equivalent in Scala)

val result = client.call(
  "conduit.ping",
  Map(
    "your" -> "parameters"
  ))()
```

Again, calling `.apply()` (or `()`) will cause execution to **block** until a
response is received. It will then return a `JValue`.

# Known Issues

* Little error handling.

Other than the error handling that the Dispatch library provides (for things
like bad HTTP response codes), Sconduit doesn't do any error checking.

This is relatively fixable, but as of writing it's 8 AM, I've not had any sleep
and the Dispatch docs getting `Promise[Either[Throwable, JValue]]` weren't making
sense to me.

This isn't so much a dealbreaker except that right now, any client using this
library has to manually check responses for `error_code` and `error_info`.

* No Java API

You can use the Scala API from Java, but it's probably pretty ugly. We should
provide a Java API to this at some point.

# License

Apache 2 -- same as Phabricator.

(c) 2012 Ricky Elrod <ricky@elrod.me>, and such. :)