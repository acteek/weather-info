package ru.acteek.weather

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import ru.acteek.weather.api.weathermap.WeatherMapClient
import ru.acteek.weather.conf.ApplicationConfig._
import ru.acteek.weather.storage.StorageImpl


object Application extends App with StrictLogging {

  implicit val system = ActorSystem("weather-info")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val api = WeatherMapClient.fromConfig()
  val storage = new StorageImpl(api)
  //

  val route =
    get {
      path("") {
        getFromResource("static/index.html")
      } ~
        pathPrefix("css") {
          encodeResponse {
            getFromResourceDirectory("static/css")
          }
        } ~
        pathPrefix("js") {
          encodeResponse {
            getFromResourceDirectory("static/js")
          }
        } ~
        path("metrics") {
          parameters("city", "date-from", "date-to") { (city, dateFrom, dateTo) =>
            onSuccess(storage.getMetrics(city, dateFrom, dateTo)) { resp =>
              complete(resp)
            }
          }
        }
    }

  Http().bindAndHandle(route, "0.0.0.0", port)
  logger.info(s"Server start at http://0.0.0.0:$port/")


//  storage.getMetrics("Москва", "", "").map {
//    r =>
//      system.log.info("RESPONSE => {}", r)
//  }
}
