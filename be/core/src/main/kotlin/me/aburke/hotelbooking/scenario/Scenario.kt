package me.aburke.hotelbooking.scenario

interface Scenario<D : Scenario.Details, R : Scenario.Result> {

    interface Details
    interface Result

    fun run(details: D): R
}
