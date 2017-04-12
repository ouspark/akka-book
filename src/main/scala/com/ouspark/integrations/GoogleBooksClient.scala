package com.ouspark.integrations

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.ouspark.integrations.GoogleBooksClient.SearchBook
import com.ouspark.model.BookActor.{BookSearchResult, BookVolumeInfo}
import com.ouspark.web.EventMarshalling

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by spark.ou on 4/12/2017.
  */
class GoogleBooksClient extends Actor with EventMarshalling {
  implicit val timeout: Timeout = 5 seconds
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {
    case SearchBook(query) =>
      findBooks(query) pipeTo sender()
  }

  def uri(query: String): Uri = Uri("https://www.googleapis.com/books/v1/volumes").withQuery(Query("q" -> query))

  def Get(uri: Uri): Future[HttpResponse] = Http(context.system).singleRequest(HttpRequest(HttpMethods.GET, uri))

  def findBooks(query: String): Future[List[BookVolumeInfo]] = {
    Get(uri(query)) flatMap {
      x: HttpResponse => Unmarshal(x.entity).to[BookSearchResult] map (_.items.map(_.volumeInfo))
    }
  }
}

object GoogleBooksClient {
  def props = Props(new GoogleBooksClient)
  def name = "GoogleBooksClientActor"

  case class SearchBook(query: String)
}
