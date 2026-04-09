package com.humayapp.scout.core.util

import com.google.crypto.tink.subtle.Base64
import java.security.MessageDigest
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    fun generateSalt(): ByteArray {
        val random = java.security.SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    fun hashPassword(password: String, salt: ByteArray): String {
        val spec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }

    fun verify(password: String, salt: ByteArray, storedHash: String): Boolean {
        val newHashString = hashPassword(password, salt)
        val newHashBytes = Base64.decode(newHashString, Base64.DEFAULT)
        val storedHashBytes = Base64.decode(storedHash, Base64.DEFAULT)
        return MessageDigest.isEqual(newHashBytes, storedHashBytes)
    }
}
