package com.pokeskies.skiesskins

import ca.landonjw.gooeylibs2.api.UIManager
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.gui.ShopGui
import com.pokeskies.skiesskins.utils.Utils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
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
    private var ticks = 0

    companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    init {
        reload(ConfigManager.CONFIG.ticksPerUpdate)
    }

    fun reload(ticksPerUpdate: Int = 20) {
        timezone = ZoneId.of(ConfigManager.CONFIG.timezone)
        updateResetTimes()

        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { _ ->
            ticks++
            if (ticks % ticksPerUpdate == 0) {
                val now = ZonedDateTime.now(timezone)
                for (time in resetTimes.toMutableMap()) {
                    for (pair in time.value) {
                        if (now.isAfter(pair.value?.second ?: continue)) {
                            updateResetTimes()
                            break
                        }
                    }
                }
            }
        })
    }

    fun userNeedsReset(shopId: String, setId: String, time: Long): Boolean {
        var timePair = resetTimes[shopId]?.get(setId) ?: return false
        val now = ZonedDateTime.now(timezone)
        if (now.isAfter(timePair.second)) {
            updateResetTimes()
            timePair = resetTimes[shopId]?.get(setId) ?: return false
        }

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), timezone).isBefore(timePair.first)
    }

    fun getShopSetResetTime(shopId: String, setId: String): Long? {
        return resetTimes[shopId]?.get(setId)?.second?.toInstant()?.toEpochMilli()
    }

    private fun updateResetTimes() {
        resetTimes.clear()
        for ((shopId, config) in ConfigManager.SHOPS) {
            val sets: MutableMap<String, Pair<ZonedDateTime, ZonedDateTime>?> = mutableMapOf()
            for ((setId, set) in config.skins.random) {
                val time = getResetTimes(set.resetTimes)
                Utils.printDebug("Shop '$shopId' has set '$setId' with reset times: LastReset=${time?.first} NextReset=${time?.second}")
                sets[setId] = time
            }
            resetTimes[shopId] = sets
        }

        SkiesSkins.INSTANCE.inventoryControllers.toMap().forEach { (_, controller) ->
            if (controller is ShopGui) {
                controller.refresh()
            }
        }
    }

    // This will attempt to find the last time that there should have been a reset and
    // the next time that there should be a reset. This could probably be improved 10x
    private fun getResetTimes(resetTimes: List<String>): Pair<ZonedDateTime, ZonedDateTime>? {
        val now = ZonedDateTime.now(timezone)
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
}