package com.pokeskies.skiesskins.economy

import com.pokeskies.skiesskins.economy.EconomyType
import com.pokeskies.skiesskins.economy.services.ImpactorEconomyService
import com.pokeskies.skiesskins.economy.services.PebblesEconomyService
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

interface IEconomyService {
    fun balance(player: ServerPlayer, currency: String = "") : Double
    fun withdraw(player: ServerPlayer, amount: Double, currency: String = "") : Boolean
    fun deposit(player: ServerPlayer, amount: Double, currency: String = "") : Boolean
    fun set(player: ServerPlayer, amount: Double, currency: String = "") : Boolean
    fun getCurrencyFormatted(currency: String, singular: Boolean = true): String {
        return currency
    }
}