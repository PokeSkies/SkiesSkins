package com.pokeskies.skiesskins

import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.data.UserData
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ShopManager {
    private lateinit var timezone: ZoneId
    private var lastReset: ZonedDateTime? = null
    private var nextReset: ZonedDateTime? = null

    companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    init {
        reload()

        ServerPlayConnectionEvents.JOIN.register { handler, packet, server ->
            val user = SkiesSkinsAPI.getUserDataOrNull(handler.player)
            println("user=$user")

            if (user != null) {
//                if (user.shopData.resetTime.isEmpty() || checkForReset(ZonedDateTime.parse(user.shopData.resetTime, dateFormatter))) {
//                    user.shopData = generateShopData()
//                    handler.player.sendMessage(Component.text("Your Skin Shop has been reset!"))
//                }
            }
        }
    }

    fun reload() {
        timezone = ZoneId.of(ConfigManager.CONFIG.timezone)
        updateResetTimes()
    }

    fun checkForReset(time: ZonedDateTime): Boolean {
        return (time >= lastReset && time <= nextReset)
    }

    fun generateShopData(): UserData.ShopData {
        val entries: MutableList<String> = mutableListOf()

//        if (ConfigManager.CONFIG.shop.skins.random.isNotEmpty()) {
//            val rc: RandomCollection<String> = RandomCollection()
//
//            for (entry in SkiesSkins.INSTANCE.configManager.config.shop.skins.random) {
//                rc.add(entry.value.weight.toDouble(), entry.key)
//            }
//
//            for (i in 0 until SkiesSkins.INSTANCE.configManager.config.shop.skins.amount) {
//                if (rc.size() <= 0) break
//                entries.add(rc.next(true))
//            }
//        }

        return UserData.ShopData(
            ZonedDateTime.now(timezone).format(dateFormatter),
            entries
        )
    }

    // This will attempt to find the last time that there should have been a reset and
    // the next time that there should be a reset. This could probably be improved 10x
    private fun updateResetTimes() {
        val resetTimes = emptyList<String>() // TODO: FIX
        val now = ZonedDateTime.now()

        // This loop will first try to find the last time TODAY that it was one of the resetTimes. There may not be a
        // time TODAY that it was one of the resetTimes, so check if there was a time YESTERDAY if there is not
        for (time in resetTimes) {
            val splitTime = time.split(":")
            var zonedDateTime = now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt()))
            zonedDateTime = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), timezone)

            val comparison = now.compareTo(zonedDateTime)

            // If NOW is AFTER compared time AND the last reset time is EITHER: null OR after compared time
            // THEN set the last reset time
            if (comparison >= 0 && (lastReset == null || zonedDateTime.isAfter(lastReset))) {
                lastReset = zonedDateTime
            }
        }

        // If lastReset was not found, it means that we need to shift back a day and check for the last reset yesterday
        if (lastReset == null) {
            for (time in resetTimes) {
                val splitTime = time.split(":")
                var zonedDateTime = now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt()))
                zonedDateTime = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), timezone)
                zonedDateTime = zonedDateTime.minusDays(1)

                val comparison = now.compareTo(zonedDateTime)

                // If NOW is AFTER compared time AND the last reset time is EITHER: null OR after compared time
                // THEN set the last reset time
                if (lastReset == null || zonedDateTime.isAfter(lastReset)) {
                    lastReset = zonedDateTime
                }
            }
        }

        var tempNextReset: ZonedDateTime? = null

        if (lastReset == null)
            return

        // This loop will first try to find the earliest reset time TODAY that is AFTER now. There may not be a
        // resetTime time TODAY that was AFTER now, so check if there was a time TOMORROW if there is not
        for (time in resetTimes) {
            val splitTime = time.split(":")
            var zonedDateTime = now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt()))
            zonedDateTime = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), timezone)

            val comparison = now!!.compareTo(zonedDateTime)

            // If lastReset is AFTER compared time
            if (comparison < 0) {
                tempNextReset = zonedDateTime
            }
        }

        // If tempNextReset was not found, it means that we need to shift forward a day and check for the next
        // reset TOMORROW
        if (tempNextReset == null) {
            for (time in resetTimes) {
                val splitTime = time.split(":")
                var zonedDateTime = now.with(LocalTime.of(splitTime[0].toInt(), splitTime[1].toInt()))
                zonedDateTime = ZonedDateTime.of(zonedDateTime.toLocalDateTime(), timezone)
                zonedDateTime = zonedDateTime.plusDays(1)

                // If NOW is AFTER compared time AND the last reset time is EITHER: null OR after compared time
                // THEN set the last reset time
                if (tempNextReset == null || zonedDateTime.isBefore(tempNextReset)) {
                    tempNextReset = zonedDateTime
                }
            }
        }

        this.nextReset = tempNextReset
    }
}