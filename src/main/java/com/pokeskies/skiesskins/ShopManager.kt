package com.pokeskies.skiesskins

import com.pokeskies.skiesskins.config.ConfigManager
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.zone.ZoneRulesException

class ShopManager {
    private lateinit var timezone: ZoneId

    // A map of (Shop ID to (Map of random set IDs to (pair of last and next reset times)))
    private var resetTimes: MutableMap<String, MutableMap<String, Pair<ZonedDateTime, ZonedDateTime>?>> = mutableMapOf()

    companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    init {
        reload()
    }

    fun reload() {
        timezone = ZoneId.of(ConfigManager.CONFIG.timezone)
        updateResetTimes()
    }

    fun userNeedsReset(shopId: String, setId: String, time: Long): Boolean {
        var timePair = resetTimes[shopId]?.get(setId) ?: return false
        val now = ZonedDateTime.now()
        if (now.isAfter(timePair.second)) {
            updateResetTimes()
            timePair = resetTimes[shopId]?.get(setId) ?: return false
        }

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), timezone).isBefore(timePair.first)
    }

    private fun updateResetTimes() {
        resetTimes.clear()
        for ((shopId, config) in ConfigManager.SHOPS) {
            val sets: MutableMap<String, Pair<ZonedDateTime, ZonedDateTime>?> = mutableMapOf()
            for ((setId, set) in config.skins.random) {
                println("The result of getResetTimes is: ${getResetTimes(set.resetTimes)}")
                sets[setId] = getResetTimes(set.resetTimes)
            }
            resetTimes[shopId] = sets
        }
    }

    // This will attempt to find the last time that there should have been a reset and
    // the next time that there should be a reset. This could probably be improved 10x
    private fun getResetTimes(resetTimes: List<String>): Pair<ZonedDateTime, ZonedDateTime>? {
        val now = ZonedDateTime.now()
        var lastReset: ZonedDateTime? = null

        // This loop will first try to find the last time TODAY that it was one of the resetTimes. There may not be a
        // time TODAY that it was one of the resetTimes, so check if there was a time YESTERDAY if there is not
        for (time in resetTimes) {
            val splitTime = time.split(":")
            val zonedDateTime = ZonedDateTime.of(
                now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt())).toLocalDateTime(),
                timezone
            )

            // If NOW is AFTER compared time AND the last reset time is EITHER: null OR after compared time
            // THEN set the last reset time
            if (now >= zonedDateTime && (lastReset == null || zonedDateTime.isAfter(lastReset))) {
                lastReset = zonedDateTime
            }
        }

        // If lastReset was not found, it means that we need to shift back a day and check for the last reset yesterday
        if (lastReset == null) {
            for (time in resetTimes) {
                val splitTime = time.split(":")
                val zonedDateTime = ZonedDateTime.of(
                    now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt())).toLocalDateTime(),
                    timezone
                ).minusDays(1)

                // If NOW is AFTER compared time AND the last reset time is EITHER: null OR after compared time
                // THEN set the last reset time
                if (lastReset == null || zonedDateTime.isAfter(lastReset)) {
                    lastReset = zonedDateTime
                }
            }
        }

        if (lastReset == null)
            return null

        var nextReset: ZonedDateTime? = null

        // This loop will first try to find the earliest reset time TODAY that is AFTER now. There may not be a
        // resetTime time TODAY that was AFTER now, so check if there was a time TOMORROW if there is not
        for (time in resetTimes) {
            val splitTime = time.split(":")
            val zonedDateTime = ZonedDateTime.of(
                now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt())).toLocalDateTime(),
                timezone
            )

            // If lastReset is AFTER compared time
            if (now < zonedDateTime) {
                nextReset = zonedDateTime
            }
        }

        // If tempNextReset was not found, it means that we need to shift forward a day and check for the next
        // reset TOMORROW
        if (nextReset == null) {
            for (time in resetTimes) {
                val splitTime = time.split(":")
                var zonedDateTime = now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt()))
                zonedDateTime = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), timezone)
                zonedDateTime = zonedDateTime.plusDays(1)

                // If NOW is AFTER compared time AND the last reset time is EITHER: null OR after compared time
                // THEN set the last reset time
                if (nextReset == null || zonedDateTime.isBefore(nextReset)) {
                    nextReset = zonedDateTime
                }
            }
        }

        if (nextReset == null)
            return null

        return Pair(lastReset, nextReset)
    }

//    fun findRecentAndNextTimestamps(timestamps: List<String>, timeZone: ZoneId): Pair<ZonedDateTime, ZonedDateTime>? {
//        val formatter = DateTimeFormatter.ofPattern("HH:mm")
//
//        // Get the current date and time with the specified time zone
//        val currentDate = ZonedDateTime.now(timeZone)
//
//        var mostRecentTimestamp: ZonedDateTime? = null
//        var nextTimestamp: ZonedDateTime? = null
//
//        for (timestamp in timestamps) {
//            println("1 Timestamp: $timestamp")
//            try {
//                val time = LocalTime.parse(timestamp, formatter)
//                println("2 Time: $time")
//                val timestampToday = currentDate.with(time)
//                println("3 Timestamp today: $timestampToday")
//
//                // Check if the timestamp is in the future or the same day
//                println("4 isEqual=${timestampToday.isEqual(currentDate)} isAfter=${timestampToday.isAfter(currentDate)}")
//                if (timestampToday.isEqual(currentDate) || timestampToday.isAfter(currentDate)) {
//                    println("5 nullCheck=${nextTimestamp == null} isBefore=${nextTimestamp?.let { timestampToday.isBefore(it) }}")
//                    if (nextTimestamp == null || timestampToday.isBefore(nextTimestamp)) {
//                        nextTimestamp = timestampToday
//                    }
//                } else {
//                    // Check if the timestamp is the most recent or the same day
//                    println("7 nullCheck=${mostRecentTimestamp == null} isAfter=${mostRecentTimestamp?.let { timestampToday.isAfter(mostRecentTimestamp) }}")
//                    if (mostRecentTimestamp == null || timestampToday.isAfter(mostRecentTimestamp)) {
//                        mostRecentTimestamp = timestampToday
//                    }
//                }
//            } catch (e: DateTimeParseException) {
//                // Handle invalid timestamp format
//                println("Invalid timestamp format: $timestamp")
//            } catch (e: ZoneRulesException) {
//                // Handle invalid time zone
//                println("Invalid time zone: $timeZone")
//                return null
//            }
//        }
//
//        // If nextTimestamp is still null, it means all timestamps are in the past
//        println("8 nextTimestamp: $nextTimestamp")
//        if (nextTimestamp == null) {
//            nextTimestamp = mostRecentTimestamp!!.plus(1, ChronoUnit.DAYS)
//            println("9 nextTimestamp: $nextTimestamp")
//        }
//
//        // If mostRecentTimestamp is still null, it means all timestamps are in the future
//        println("10 mostRecentTimestamp: $mostRecentTimestamp")
//        if (mostRecentTimestamp == null) {
//            mostRecentTimestamp = nextTimestamp!!.minus(1, ChronoUnit.DAYS)
//            println("11 mostRecentTimestamp: $mostRecentTimestamp")
//        }
//
//        // Return null if either the next or last timestamps are null
//        return if (nextTimestamp != null && mostRecentTimestamp != null) {
//            Pair(mostRecentTimestamp, nextTimestamp)
//        } else {
//            null
//        }
//    }
}