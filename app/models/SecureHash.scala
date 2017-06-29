package models

import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecureHash {
  val iterations = 256
  val salt = System.getenv("PASSWORD_SALT").getBytes
  val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
  val encoder = Base64.getEncoder

  def encode(rowPassword: String):String = {
    val spec = new PBEKeySpec(rowPassword.toCharArray, salt, iterations, 256)
    encoder.encodeToString(factory.generateSecret(spec).getEncoded)
  }
}
