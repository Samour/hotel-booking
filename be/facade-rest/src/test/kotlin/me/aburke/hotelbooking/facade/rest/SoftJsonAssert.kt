package me.aburke.hotelbooking.facade.rest

import org.assertj.core.api.SoftAssertions
import org.skyscreamer.jsonassert.JSONAssert

interface SoftJsonAssert {
    fun isEqualTo(expect: String)
}

fun SoftAssertions.assertThatJson(actual: String?): SoftJsonAssert = object : SoftJsonAssert {
    override fun isEqualTo(expect: String) = check {
        JSONAssert.assertEquals(expect, actual, true)
    }
}
