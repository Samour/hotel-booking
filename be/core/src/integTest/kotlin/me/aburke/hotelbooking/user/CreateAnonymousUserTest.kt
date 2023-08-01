package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

class CreateAnonymousUserTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var underTest: CreateAnonymousUserScenario

    @BeforeEach
    fun init() {
        app = stubs.make()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create anonymous user`() {
        val result = underTest.run(CreateAnonymousUserScenario.Detail)

        assertSoftly { s ->
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).containsExactly(result.userId)
            s.assertThat(stubs.userRepositoryStub.getUsers()).isEmpty()
        }
    }
}
