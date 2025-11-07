package com.pokeskies.skiesskins.economy

import com.google.gson.*
import com.pokeskies.skiesskins.utils.Utils
import net.fabricmc.loader.api.FabricLoader
import java.lang.reflect.Type

enum class EconomyType(
    val identifier: String,
    val modId: String
) {
    IMPACTOR("impactor", "impactor"),
    PEBBLES("pebbles", "pebbles-economy"),
    COBBLEDOLLARS("cobbledollars", "cobbledollars"),
    BECONOMY("beconomy", "beconomy");

    fun isModPresent() : Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    companion object {
        fun valueOfAnyCase(name: String): EconomyType? {
            for (type in EconomyType.entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class Adapter : JsonSerializer<EconomyType>, JsonDeserializer<EconomyType> {
        override fun serialize(src: EconomyType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.identifier)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EconomyType {
            val economyType = valueOfAnyCase(json.asString)

            if (economyType == null) {
                Utils.printError("Could not deserialize EconomyType '${json.asString}'! Falling back to IMPACTOR")
                return IMPACTOR
            }

            return economyType
        }
    }
}