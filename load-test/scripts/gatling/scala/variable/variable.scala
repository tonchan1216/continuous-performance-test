package variable

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.jdbc.Predef._

import scala.util.Random

object variable {
  val baseUrl = "https://exmaple.com"
  val acceptEncodingHeader = "gzip, deflate"
  val acceptLanguageHeader = "ja,en-US;q=0.9,en;q=0.8"
  val userAgentHeader = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36"

  val httpProtocol = http
    .baseUrl(variable.baseUrl)
    .acceptHeader("*/*")
    .acceptEncodingHeader(variable.acceptEncodingHeader)
    .acceptLanguageHeader(variable.acceptLanguageHeader)
    .userAgentHeader(variable.userAgentHeader)
    .disableWarmUp
    .disableCaching
}