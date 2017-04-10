package com.ouspark.web




import com.ouspark.model.BookActor._
import com.ouspark.model.Permissions
import com.ouspark.model.PublisherActor.Publisher
import com.ouspark.model.UserActor.UserPayload
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, JsonFormat, deserializationError}

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

  implicit object PermissionJsonProtocol extends JsonFormat[Seq[Permissions.Permission]] {
    def write(permission: Seq[Permissions.Permission]) = JsObject(
      Map("permissions" -> JsArray(JsString(permission.toString))
    ))
    def read(value: JsValue) = value match {
      case JsArray(permissions) => permissions.map(_.toString).map(Permissions.withName(_))
      case _ => deserializationError("String value expected")
    }
  }

  implicit val bookFormat = jsonFormat5(Book.apply)
  implicit val publisherFormat = jsonFormat2(Publisher.apply)
  implicit val bookResourceFormat = jsonFormat5(BookResource.apply)
  implicit val bookUpdateFormat = jsonFormat3(BookUpdatePayload.apply)
  implicit val bookCreateFormat = jsonFormat4(BookCreatePayload.apply)
  implicit val userFormat = jsonFormat2(UserPayload.apply)
}


