package com.ouspark.web


import com.ouspark.model.BookActor.{Book, BookResource, Publisher}
import spray.json.DefaultJsonProtocol

/**
  * Created by spark.ou on 4/1/2017.
  */
trait EventMarshalling extends DefaultJsonProtocol {

  implicit val bookFormat = jsonFormat4(Book.apply)
  implicit val publisherFormat = jsonFormat2(Publisher.apply)
  implicit val bookResourceFormat = jsonFormat4(BookResource.apply)
}


