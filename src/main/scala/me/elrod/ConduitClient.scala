package me.elrod.sconduit

import dispatch._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

class ConduitClient(
  val apiURL: String = "https://secure.phabricator.com/api/",
  val sessionKey: Option[String] = None,
  val connectionID: Option[Int] = None
) {

  val authenticated = (sessionKey != None && connectionID != None)

  /** Call a Conduit method.
    *
    * @param method The Conduit method to call.
    * @param params The parameters to pass to Conduit.
    * @return The parsed response from Conduit
    */
  def call(
    method: String,
    params: Map[String, Any] = Map("" -> "")): Promise[Either[Throwable, JValue]] = {
    val dispatchURL = if (apiURL.contains("/api")) apiURL else apiURL + "/api/"

    // I can't figure out how to get json4s to handle a Map[String, Any], so
    // I'm using scala.util.parsing.json for this for now. It's slightly
    // less efficient and slightly more ugly, but it works for now with not
    // too much of a loss.
    val jsonString = scala.util.parsing.json.JSONObject(
      if (authenticated) {
        params ++ Map(
          "__conduit__" -> Map(
            "sessionKey" -> sessionKey.getOrElse(""),
            "connectionID" -> connectionID.getOrElse(-1)
          )
        )
      } else {
        params
      }).toString()

    val request = url(dispatchURL) / method << Map(
      "params" -> jsonString
    )

    Http(request > as.json4s.Json)
  }
}

object ConduitClient {
  implicit val formats = DefaultFormats

  /** A convenience method to try getting an authenticated client.
    *
    * @param token The user's token.
    * @return A new, authenticated ConduitClient
    */
  def fromToken(
    token: String,
    apiURL: String = "https://secure.phabricator.com/api/") = {
    val client = new ConduitClient(apiURL)
    val certResponse = client.call(
      "conduit.getcertificate",
      Map(
        "host" -> apiURL,
        "token" -> token
      )).apply() // Block because we want to return a client, not a promise.
    val username = (certResponse \ "result" \ "username").extract[String]
    val certificate = (certResponse \ "result" \ "certificate").extract[String]

    val time = System.currentTimeMillis / 1000

    val authSignatureMD = java.security.MessageDigest.getInstance("SHA-1")
    authSignatureMD.update((time.toString + certificate).getBytes)
    val authSignature = authSignatureMD.digest
      .map(x =>"%02x".format(x)).mkString

    println(time.toString)
    println(certificate)
    println(authSignature)

    client.call(
      "conduit.connect",
      Map(
        "client" -> "sconduit",
        "clientVersion" -> 6,
        "user" -> username,
        "authToken" -> time,
        "authSignature" -> authSignature,
        "host" -> apiURL
      )).apply() // Block for same reason as above.

    val sessionKey = (connectResponse \ "result" \ "sessionKey").extract[String]
    val connectionID = (connectResponse \ "result" \ "connectionID").extract[Int]

    new ConduitClient(apiURL, Some(sessionKey), Some(connectionID))
  }
}
