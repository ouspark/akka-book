package com.ouspark.security

import akka.actor.ActorRef
import akka.http.scaladsl.server.directives.Credentials
import akka.pattern.ask
import akka.util.Timeout
import com.ouspark.model.UserActor.{GetUser, HasPermission}
import com.ouspark.model.{Permissions, User}
import com.ouspark.persistence.BookPersistence
import com.ouspark.security.PasswordHasher.hasherString

import scala.concurrent.Future

/**
  * Created by spark.ou on 4/11/2017.
  */
class AuthenticationService(userActor: ActorRef, timeout: Timeout) {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val requestTimeout = timeout

  val persistence = new BookPersistence

  def userAuthentication(credentials: Credentials): Future[Option[User]] =
    credentials match {
      case p@Credentials.Provided(id) =>
        userActor.ask(GetUser(id)).mapTo[Option[User]] map {
          case Some(user) =>
            if (p.verify(hasherString(user.password), hasherString(_, user.salt))) Some(user) else None
          case _ => None
        }
      case _ => Future.successful(None)
    }

  def userHasPermission(user: User, permission: Permissions.Permission): Future[Boolean] = {
    userActor.ask(HasPermission(user, permission)).mapTo[Boolean]
  }
}
