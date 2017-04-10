package com.ouspark.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
  * Created by spark.ou on 4/7/2017.
  */
object PasswordHasher {

  lazy val random = SecureRandom.getInstance("SHA1PRNG")

  def hash(password: String): (Array[Byte], Array[Byte]) = {
    val salt = random.generateSeed(32)
    (hash(password, salt), salt)
  }

  def hash(password: String, salt: Array[Byte]): Array[Byte] = {
    val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
    val keySpec = new PBEKeySpec(password.toCharArray, salt, 1000, 256)
    val secretKey = secretKeyFactory.generateSecret(keySpec)
    secretKey.getEncoded
  }

  def hasherString(password: String, salt: Array[Byte]): String = {
    new String(hash(password, salt), "UTF-8")
  }
  def hasherString(password: Array[Byte]): String = {
    new String(password, "UTF-8")
  }

}
