package page

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

import request._

object TopPage {
  def top():ChainBuilder =
    group("TOP PAGE"){
      exec(request.top.get("TOP"))
    }
}