package me.aburke.hotelbooking.data

data class TestUser(
    val userId: String,
    val loginId: String,
    val password: String,
) {
    companion object {
        val admin = TestUser(
            userId = "test-admin-id",
            loginId = "test-admin",
            password = "test-password",
        )
    }
}
