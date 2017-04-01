package com.ouspark

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.ouspark.persistence.BookPersistence
import com.ouspark.web.RestApi
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by spark.ou on 4/1/2017.
  */
object Main extends App with RequestTimeOut {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem()
  implicit val ex = system.dispatcher

  implicit val materializer = ActorMaterializer()

  val persistence = new BookPersistence
  persistence.createSchema() onSuccess {
    case _ => persistence.createDataset()
  }

  val routes = new RestApi(system, requestTimeOut(config)).routes
  val bindFuture: Future[ServerBinding] = Http().bindAndHandle(routes, host, port)

  val log = Logging(system.eventStream, "akka-book")
  bindFuture.map { serverBinding =>
    log.info(s"API bound to ${serverBinding.localAddress}")
  }.onFailure {
    case ex: Exception =>
      log.error(ex, "Fail to bind to {}:{}", host, port)
      system.terminate()
  }
}

trait RequestTimeOut {
  def requestTimeOut(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}