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

    companion object {
        fun getEconomyService(economyType: EconomyType) : IEconomyService? {
            if (!economyType.isModPresent()) return null

            return try {
                when (economyType) {
                    EconomyType.IMPACTOR -> ImpactorEconomyService()
                    EconomyType.PEBBLES -> PebblesEconomyService()
                }
            } catch (ex: Exception) {
                Utils.printError("There was an exception while initializing the Economy Service: ${economyType}. Is it loaded?")
                ex.printStackTrace()
                null
            }
        }
    }
}