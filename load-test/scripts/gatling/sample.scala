package tests.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {
  // parse load profile from Taurus
  val t_iterations = Integer.getInteger("iterations", 100).toInt
  val t_concurrency = Integer.getInteger("concurrency", 10).toInt
  val t_rampUp = Integer.getInteger("ramp-up", 1).toInt
  val t_holdFor = Integer.getInteger("hold-for", 60).toInt
  val t_throughput = Integer.getInteger("throughput", 100).toInt
  val cluster_size = Integer.getInteger("CLUSTER_SIZE", 0).toInt
  val partition_id = Integer.getInteger("PARTITION_ID", 0).toInt

  val httpConf = http.baseUrl("http://blazedemo.com/")

  // 'forever' means each thread will execute scenario until
  // duration limit is reached
  val loopScenario = scenario("Loop Scenario").forever() {
    exec(http("index").get("/"))
  }

  val execution = loopScenario
    .inject(rampUsers(t_concurrency) during t_rampUp)
    .protocols(httpConf)

  setUp(execution).
    throttle(jumpToRps(t_throughput), holdFor(t_holdFor)).
    maxDuration(t_rampUp + t_holdFor)
}
