package org.authapp.security.encrypt

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

/**
 * Object to generate password salts.
 * Uses [SecureRandom] internally
 */
object SaltGenerator {
    private const val SALT_LENGTH = 16
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }
}

/**
 * Password encoder. Encodes raw passwords
 * Capable of verifying of presented password matches an hashed one
 */
interface PasswordCoder {
    fun encodePassword(rawPassword: String): String
    fun matches(presentedPassword: String, hashedPassword: String): Boolean
}

/**
 * Default [PasswordCoder]. Uses SHA-512 algorithm to encode passwords
 * Result - String of salt and encoded password concatenated with "$$" sequence
 */
class DefaultPasswordCoder : PasswordCoder {
    private companion object {
        const val DELIMITER = "$$"
    }

    override fun encodePassword(rawPassword: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-512")
        val salt = SaltGenerator.generateSalt()
        messageDigest.update(salt)
        val encoder = Base64.getEncoder()
        val digest = messageDigest.digest(rawPassword.toByteArray())
        return encoder.encodeToString(salt) + DELIMITER + encoder.encodeToString(digest)
    }

    override fun matches(presentedPassword: String, hashedPassword: String): Boolean {
        val split = hashedPassword.split(DELIMITER)
        val extractedSalt = Base64.getDecoder().decode(split[0])
        val extractedHashedPassword = Base64.getDecoder().decode(split[1])
        val presentedBytes = presentedPassword.toByteArray(Charsets.UTF_8)
        val messageDigest = MessageDigest.getInstance("SHA-512")
        messageDigest.update(extractedSalt)
        val digest = messageDigest.digest(presentedBytes)
        return extractedHashedPassword!!.contentEquals(digest)
    }
}