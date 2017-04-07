package com.ouspark.model

/**
  * Created by spark.ou on 4/7/2017.
  */
object Permissions extends Enumeration {
  type permission = Value
  val MANAGE_PUBLISHERS = Value("MANAGE_PUBLISHERS")
  val MANAGE_USERS = Value("MANAGE_USERS")
}

case class User(username: String, password: Array[Byte], salt: Array[Byte], permissions: Seq[Permissions.permission] = Seq()) {
  def this(username: String, saltedPassword: (Array[Byte], Array[Byte])) {
    this(username, saltedPassword._1, saltedPassword._2)
  }
}
