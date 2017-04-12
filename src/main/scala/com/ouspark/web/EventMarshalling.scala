package com.ouspark.web

import com.ouspark.model.BookActor._
import com.ouspark.model.Permissions
import com.ouspark.model.PublisherActor.Publisher
import com.ouspark.model.UserActor.UserPayload
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import spray.json.{DefaultJsonProtocol, JsArray, JsString, JsValue, JsonFormat, deserializationError}

/**
  * Created by spark.ou on 4/1/2017.
  */
trait EventMarshalling extends DefaultJsonProtocol {

  implicit object LocalDateJsonProtocol extends JsonFormat[LocalDate] {
    private val formatter = ISODateTimeFormat.date()

    def write(date: LocalDate) = JsString(date.toString(formatter))

    def read(value: JsValue): LocalDate = value match {
      case JsString(date) => formatter.parseLocalDate(date)
      case _ => deserializationError("String value expected")
    }
  }

  implicit object PermissionJsonProtocol extends JsonFormat[Seq[Permissions.Permission]] {
    def write(permission: Seq[Permissions.Permission]) = JsArray(JsString(permission.toString))
    def read(value: JsValue): Vector[_root_.com.ouspark.model.Permissions.Value] = value match {
      case JsArray(permissions) => permissions.map(_.toString).map(Permissions.withName)
      case _ => deserializationError("String value expected")
    }
  }

  implicit val bookFormat = jsonFormat5(Book.apply)
  implicit val publisherFormat = jsonFormat2(Publisher.apply)
  implicit val bookResourceFormat = jsonFormat5(BookResource.apply)
  implicit val bookUpdateFormat = jsonFormat3(BookUpdatePayload.apply)
  implicit val bookCreateFormat = jsonFormat4(BookCreatePayload.apply)
  implicit val userFormat = jsonFormat2(UserPayload.apply)
  implicit val bookVolumeFormat = jsonFormat7(BookVolumeInfo.apply)
  implicit val bookSearchResultItemFormat = jsonFormat1(BookSearchResultItem.apply)
  implicit val bookSearchResultFormat = jsonFormat2(BookSearchResult.apply)
}


