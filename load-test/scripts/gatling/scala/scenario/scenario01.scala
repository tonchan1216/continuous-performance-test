package scenario

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

object scenario01 {
  val sc01 : ScenarioBuilder = scenario("シナリオ1")
    .group("group1"){
      exec(page.TopPage.top())
      .exitHereIfFailed
      .pause(10 seconds)
    }
}