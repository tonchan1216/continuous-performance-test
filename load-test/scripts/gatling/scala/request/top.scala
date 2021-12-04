package request

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object top {
  def get(page_name: String): HttpRequestBuilder = {
    val request_name = "_post_pickup"
    http(page_name + request_name)
      .post("/pickup")
      .check(status.is(200).name(request_name))
      .check(jsonPath("$.success").is("true"))
  }
}
