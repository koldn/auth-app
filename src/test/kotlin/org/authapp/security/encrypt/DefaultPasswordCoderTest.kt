package org.authapp.security.encrypt

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DefaultPasswordCoderTest {

    @Test
    fun `Presented password should match an encoded one`() {
        val defaultPasswordCoder = DefaultPasswordCoder()
        val encodedPassword = defaultPasswordCoder.encodePassword("12345")
        Assertions.assertTrue(defaultPasswordCoder.matches("12345", encodedPassword))
    }
}