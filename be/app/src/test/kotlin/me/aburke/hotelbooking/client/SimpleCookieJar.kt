package me.aburke.hotelbooking.client

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SimpleCookieJar : CookieJar {

    private val cookies = mutableMapOf<String, Cookie>()

    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies.values.toList()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach {
            this.cookies[it.name] = it
        }
    }

    fun clearAllCookies() = cookies.clear()
}
