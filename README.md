# Sconduit

A Scala (and Java) interface to Phabricator's Conduit API.

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