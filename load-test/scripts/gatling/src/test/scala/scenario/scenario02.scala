package scenario

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

object scenario02 {
  val sc02 : ScenarioBuilder = scenario("シナリオ2")
    .group("group2"){
      exec(page.TopPage.top())
        .exitHereIfFailed
        .pause(10 seconds)
    }
}