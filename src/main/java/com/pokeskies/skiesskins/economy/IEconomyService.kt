package com.pokeskies.skiesskins.economy

import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

abstract class IEconomyService {
    abstract fun balance(player: ServerPlayer, currency: String = "") : Double
    abstract fun withdraw(player: ServerPlayer, amount: Double, currency: String = "") : Boolean
    abstract fun deposit(player: ServerPlayer, amount: Double, currency: String = "") : Boolean
    abstract fun set(player: ServerPlayer, amount: Double, currency: String = "") : Boolean
    open fun getCurrencyFormatted(currency: String, singular: Boolean = true): String {
        return Utils.titleCase(currency)
    }
}
