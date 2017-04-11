package com.ouspark.persistence

import com.ouspark.model.{Permission, Permissions}
import com.ouspark.persistence.Users.users
import slick.driver.H2Driver.api._
/**
  * Created by spark.ou on 4/11/2017.
  */
class UserPermissions(tag: Tag) extends Table[Permission](tag, "USER_PERMISSIONS") {
  import UserPermissions.permissionColumnType
  def username = column[String]("USERNAME", O.Length(64))
  def permission = column[Permissions.Permission]("PERMISSION", O.Length(64))

  def user = foreignKey("USER_FK", username, users)(_.username, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def pk = primaryKey("USER_PERMISSION_PK", (username, permission))
  def * = (username, permission) <> (Permission.tupled, Permission.unapply)
}

object UserPermissions {
  val permissions = TableQuery[UserPermissions]

  implicit val permissionColumnType = MappedColumnType.base[Permissions.Permission, String] (
    { permission => permission.toString },
    { str => Permissions.withName(str) }
  )
}
