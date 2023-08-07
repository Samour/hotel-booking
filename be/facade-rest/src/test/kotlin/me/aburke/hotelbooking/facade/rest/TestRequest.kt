package me.aburke.hotelbooking.facade.rest

import org.assertj.core.api.SoftAssertions

abstract class TestRequest<T : Any> {

    protected lateinit var response: T

    fun executeRequest() {
        response = makeRequest()
    }

    protected abstract fun makeRequest(): T

    abstract fun makeAssertions(s: SoftAssertions)
}
