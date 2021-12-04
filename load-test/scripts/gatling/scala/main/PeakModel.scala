package main

import scenario._
import variable._

import io.gatling.core.Predef._
import scala.concurrent.duration._

class PeakModel extends Simulation {
  // parse load profile from Taurus
  val t_iterations = Integer.getInteger("iterations", 100).toInt
  val t_concurrency = Integer.getInteger("concurrency", 10).toInt
  val t_rampUp = Integer.getInteger("ramp-up", 1).toInt
  val t_holdFor = Integer.getInteger("hold-for", 60).toInt
  val t_throughput = Integer.getInteger("throughput", 100).toInt
  val httpConf = http.baseUrl("http://blazedemo.com/")

  val user = Map(1 -> 4,
    2 -> 4,
  )

  setUp(
    scenario01.sc01.inject(
      rampConcurrentUsers(0) to (user(1)) during (t_rampUp seconds),
      constantConcurrentUsers(user(1)) during (time seconds))
      .protocols(variable.httpProtocol),

    scenario02.sc02.inject(
      rampConcurrentUsers(0) to (user(2)) during (t_rampUp seconds),
      constantConcurrentUsers(user(2)) during (time seconds))
      .protocols(variable.httpProtocol)
}