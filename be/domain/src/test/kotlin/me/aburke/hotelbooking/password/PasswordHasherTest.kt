package me.aburke.hotelbooking.password

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource

private const val RAW_PASSWORD = "raw-password"

class PasswordHasherTest {

    private val underTest = PasswordHasher()

    @Test
    fun `should generate password hash`() {
        val hashedPassword = underTest.hashPassword(RAW_PASSWORD)

        assertThat(hashedPassword).isNotBlank().isNotEqualTo(RAW_PASSWORD)

        val passwordCheck = underTest.passwordMatches(RAW_PASSWORD, hashedPassword)

        assertThat(passwordCheck).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["wrong-password"])
    @EmptySource
    fun `incorrect password should not match`(password: String) {
        val hashedPassword = underTest.hashPassword(RAW_PASSWORD)
        val passwordCheck = underTest.passwordMatches(password, hashedPassword)

        assertThat(passwordCheck).isFalse()
    }
}
