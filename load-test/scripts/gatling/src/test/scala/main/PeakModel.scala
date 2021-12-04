package main

import scenario._
import variable._
import io.gatling.core.Predef._

import scala.collection.MapView
import scala.concurrent.duration._
import scala.language.postfixOps

class PeakModel extends Simulation {
  // parse load profile from Taurus
  val t_concurrency: Int = Integer.getInteger("concurrency", 1).toInt
  val t_rampUp: Int = Integer.getInteger("ramp-up", 1).toInt
  val t_holdFor: Int = Integer.getInteger("hold-for", 60).toInt

  val user: MapView[Int, Int] = Map(
    1 -> 0.5,
    2 -> 0.5,
  ).view.mapValues(v => (v * t_concurrency).toInt)

  setUp(
    scenario01.sc01.inject(
      rampConcurrentUsers(0) to user(1) during (t_rampUp seconds),
      constantConcurrentUsers(user(1)) during (t_holdFor seconds))
      .protocols(variable.httpProtocol),

    scenario02.sc02.inject(
      rampConcurrentUsers(0) to user(2) during (t_rampUp seconds),
      constantConcurrentUsers(user(2)) during (t_holdFor seconds))
      .protocols(variable.httpProtocol)
  )
}