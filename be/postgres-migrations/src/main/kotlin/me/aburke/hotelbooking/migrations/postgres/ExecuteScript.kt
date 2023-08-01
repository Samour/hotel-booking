package me.aburke.hotelbooking.migrations.postgres

import org.apache.ibatis.jdbc.ScriptRunner
import java.sql.Connection

fun Connection.executeScript(fname: String) {
    this::class.java.getResourceAsStream("/sql/$fname").use {
        ScriptRunner(this).apply {
            setSendFullScript(true)
        }.runScript(it.reader())
    }
}
