package com.pokeskies.skiesskins.economy

import com.pokeskies.skiesskins.economy.services.ImpactorEconomyService
import com.pokeskies.skiesskins.economy.services.PebblesEconomyService

class EconomyManager {
    private val services: MutableMap<EconomyType, IEconomyService> = mutableMapOf()

    init {
        for (service in EconomyType.values()) {
            if (service.isModPresent()) {
                services[service] = getServiceForType(service)
            }
        }
    }

    fun getService(economyType: EconomyType): IEconomyService? {
        return services[economyType]
    }

    private fun getServiceForType(economyType: EconomyType): IEconomyService {
        return when (economyType) {
            EconomyType.IMPACTOR -> ImpactorEconomyService()
            EconomyType.PEBBLES -> PebblesEconomyService()
        }
    }
}