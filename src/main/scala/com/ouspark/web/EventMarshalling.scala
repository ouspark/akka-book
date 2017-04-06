package com.ouspark.web


import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import com.ouspark.model.BookActor._
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, deserializationError}

/**
  * Created by spark.ou on 4/1/2017.
  */
trait EventMarshalling extends DefaultJsonProtocol {

  implicit object LocalDateJsonProtocol extends JsonFormat[LocalDate] {
    private val formatter = ISODateTimeFormat.date()

    def write(date: LocalDate) = JsString(date.toString(formatter))

    def read(value: JsValue) = value match {
      case JsString(date) => formatter.parseLocalDate(date)
      case _ => deserializationError("String value expected")
    }
  }

  implicit val bookFormat = jsonFormat5(Book.apply)
  implicit val publisherFormat = jsonFormat2(Publisher.apply)
  implicit val bookResourceFormat = jsonFormat5(BookResource.apply)
  implicit val bookUpdateFormat = jsonFormat3(BookUpdatePayload.apply)
  implicit val bookCreateFormat = jsonFormat4(BookCreatePayload.apply)
}


