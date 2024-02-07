package com.pokeskies.skiesskins.economy.services

import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.economy.IEconomyService
import com.pokeskies.skiesskins.utils.Utils
import net.impactdev.impactor.api.economy.EconomyService
import net.impactdev.impactor.api.economy.accounts.Account
import net.impactdev.impactor.api.economy.currency.Currency
import net.kyori.adventure.key.Key
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.sin

class ImpactorEconomyService : IEconomyService {
    init {
        Utils.printInfo("Impactor Economy Service has been found and loaded for any Currency actions!")
    }

    override fun balance(player: ServerPlayer, currency: String) : Double {
        return getAccount(player.uuid, currency)?.thenCompose(Account::balanceAsync)?.join()?.toDouble() ?: 0.0
    }

    override fun withdraw(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        return getAccount(player.uuid, currency)?.join()?.withdrawAsync(BigDecimal(amount))?.join()?.successful() ?: false
    }

    override fun deposit(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        return getAccount(player.uuid, currency)?.join()?.depositAsync(BigDecimal(amount))?.join()?.successful() ?: false
    }

    override fun set(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        return getAccount(player.uuid, currency)?.join()?.setAsync(BigDecimal(amount))?.join()?.successful() ?: false
    }

    override fun getCurrencyFormatted(currency: String, singular: Boolean): String {
        val c = getCurrency(currency) ?: return currency
        return SkiesSkins.INSTANCE.adventure!!.toNative(if (singular) c.singular() else c.plural()).string
    }

    private fun getAccount(uuid: UUID, currency: String): CompletableFuture<Account>? {
        return getCurrency(currency)?.let { c ->
            EconomyService.instance().account(c, uuid)
        }
    }

    private fun getCurrency(id: String) : Currency? {
        if (id.isEmpty()) {
            return EconomyService.instance().currencies().primary()
        }

        val currency: Optional<Currency> = EconomyService.instance().currencies().currency(Key.key(id))
        if (currency.isEmpty) {
            Utils.printError(
                "Could not find a currency by the ID $id! Valid currencies: " +
                        "${EconomyService.instance().currencies().registered().map { it.key() }}"
            )
            return null
        }

        return currency.get()
    }
}