package com.pokeskies.skiesskins.utils

import net.impactdev.impactor.api.economy.EconomyService
import net.impactdev.impactor.api.economy.accounts.Account
import net.impactdev.impactor.api.economy.currency.Currency
import net.impactdev.impactor.api.economy.transactions.EconomyTransaction
import net.kyori.adventure.key.Key
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture


class ImpactorService {
    private val economyService = EconomyService.instance()
    private var currency: Currency

    init {
        val optCurrency = economyService.currencies()
            .currency(Key.key("default")) // TODO: FIX
        if (optCurrency.isEmpty)
            throw IllegalArgumentException()
        currency = optCurrency.get()
    }

    private fun getAccount(uuid: UUID): CompletableFuture<Account> {
        return economyService.account(uuid)
    }

    fun getBalance(uuid: UUID): CompletableFuture<BigDecimal> {
        return getAccount(uuid).thenCompose(Account::balanceAsync)
    }

    fun withdraw(uuid: UUID, amount: BigDecimal?): CompletableFuture<Boolean> {
        return getAccount(uuid).thenCompose { account: Account ->
            account.withdrawAsync(
                amount
            ).thenApply(EconomyTransaction::successful)
        }
    }

    fun deposit(uuid: UUID, amount: BigDecimal?): CompletableFuture<Boolean> {
        return getAccount(uuid).thenCompose { account: Account ->
            account.depositAsync(
                amount
            ).thenApply(EconomyTransaction::successful)
        }
    }
}