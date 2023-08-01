package me.aburke.hotelbooking.scenario

interface ScenarioDetails

interface ScenarioResult

interface Scenario<D : ScenarioDetails, R : ScenarioResult> {

    fun run(details: D): R
}
