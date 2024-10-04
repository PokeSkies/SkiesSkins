package com.pokeskies.skiesskins.utils

import ca.landonjw.gooeylibs2.api.button.GooeyButton
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.*
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.Registry
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.lang.reflect.Type
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

object Utils {
    val miniMessage: MiniMessage = MiniMessage.miniMessage()

    fun deserializeText(text: String): Component {
        return SkiesSkins.INSTANCE.adventure!!.toNative(miniMessage.deserialize(text))
    }

    fun parsePlaceholders(player: ServerPlayer, text: String): String {
        return SkiesSkins.INSTANCE.placeholderManager.parse(player, text)
    }

    fun parseSkinString(string: String, player: ServerPlayer, skin: SkinConfig): Component {
        var parsed = string.replace("%name%", skin.name)
            .replace("%species%", PokemonSpecies.getByIdentifier(skin.species)?.name ?: "Invalid Species")

        if (skin.scrapping != null) {
            parsed = parsed.replace("%scrap_value%", skin.scrapping.value.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(it.amount > 1)}" })
        } else {
            parsed = parsed.replace("%scrap_value%", "No Value")
        }

        return deserializeText(parsePlaceholders(player, parsed))
    }

    fun parseSkinStringList(list: List<String>, player: ServerPlayer, skin: SkinConfig): List<Component> {
        val newList: MutableList<Component> = mutableListOf()
        for (line in list) {
            var initialParse = line.replace("%name%", skin.name)
                .replace("%species%", PokemonSpecies.getByIdentifier(skin.species)?.name ?: "Invalid Species")

            if (skin.scrapping != null) {
                initialParse = initialParse.replace("%scrap_value%", skin.scrapping.value.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(it.amount > 1)}" })
            } else {
                initialParse = initialParse.replace("%scrap_value%", "No Value")
            }

            val parsed = parsePlaceholders(player, initialParse)

            if (parsed.contains("%description%", true)) {
                for (dLine in skin.description) {
                    newList.add(deserializeText(parsed.replace("%description%", dLine)))
                }
            } else {
                newList.add(deserializeText(parsed))
            }
        }
        return newList
    }

    fun parsePokemonString(string: String, player: ServerPlayer, pokemon: Pokemon?): Component {
        var result = parsePlaceholders(player, string)
        if (pokemon != null) {
            result = result.replace("%pokemon_skin_name%",
                SkiesSkinsAPI.getPokemonSkin(pokemon)?.second?.name ?: "None"
            )
        }
        return deserializeText(result)
    }

    fun printDebug(message: String?, bypassCheck: Boolean = false) {
        if (bypassCheck || ConfigManager.CONFIG.debug)
            SkiesSkins.LOGGER.info("[SkiesSkins] DEBUG: $message")
    }

    fun printError(message: String?) {
        SkiesSkins.LOGGER.error("[SkiesSkins] ERROR: $message")
    }

    fun printInfo(message: String?) {
        SkiesSkins.LOGGER.info("[SkiesSkins] $message")
    }

    fun processItemStack(itemStack: ItemStack): ItemStack {
        itemStack.setHoverName(SkiesSkins.INSTANCE.adventure!!
            .toNative(MiniMessage.miniMessage().deserialize(itemStack.displayName.string)))

        val displayNBT = itemStack.getTagElement(ItemStack.TAG_DISPLAY)
        if (displayNBT != null && displayNBT.contains(ItemStack.TAG_LORE)) {
            val nbtList = displayNBT.getList(ItemStack.TAG_LORE, 8)
            for (i in 0 until nbtList.size) {
                val text: Component? = Component.Serializer.fromJson(nbtList.getString(i))
                if (text != null) {
                    nbtList[i] = StringTag.valueOf(Component.Serializer.toJson(SkiesSkins.INSTANCE.adventure!!
                        .toNative(MiniMessage.miniMessage().deserialize(text.string))))
                }
            }
            displayNBT.put(ItemStack.TAG_LORE, nbtList)
            itemStack.addTagElement(ItemStack.TAG_DISPLAY, displayNBT)
        }

        return itemStack
    }

    fun nullPokemonToItem(pokemon: Pokemon?): ItemStack {
        return PokemonItem.from(pokemon!!, 1)
    }

    fun <T> getOrOther(obj: Any?, main: T, other: T): T {
        if (obj != null) {
            return main
        }
        return other
    }

    fun <T> getOrRunOther(obj: Any?, main: () -> T, other: () -> T): T {
        if (obj != null) {
            return main()
        }
        return other()
    }

    fun getErrorButton(text: String): GooeyButton {
        return GooeyButton.builder()
            .display(ItemStack(Items.BARRIER))
            .title(Utils.deserializeText(text))
            .build()
    }

    fun getRandomRanged(min: Int, max: Int): Int {
        return if (min > max || min == max) min else Random().nextInt(max - min + 1) + min
    }

    fun hideFlags(itemStack: ItemStack, vararg parts: ItemStack.TooltipPart): ItemStack {
        for (part in parts) itemStack.hideTooltipPart(part)
        return itemStack
    }

    fun titleCase(input: String): String {
        return Arrays.stream(
            input.lowercase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
            .map { word: String ->
                word[0].titlecaseChar().toString() + word.substring(1)
            }
            .collect(Collectors.joining(" "))
    }


    fun getFormattedTime(time: Long): String {
        if (time <= 0) return "0"
        val timeFormatted: MutableList<String> = ArrayList()
        val days = time / 86400
        val hours = time % 86400 / 3600
        val minutes = time % 86400 % 3600 / 60
        val seconds = time % 86400 % 3600 % 60
        if (days > 0) {
            timeFormatted.add(days.toString() + "d")
        }
        if (hours > 0) {
            timeFormatted.add(hours.toString() + "h")
        }
        if (minutes > 0) {
            timeFormatted.add(minutes.toString() + "m")
        }
        if (seconds > 0) {
            timeFormatted.add(seconds.toString() + "s")
        }
        return java.lang.String.join(" ", timeFormatted)
    }

    // Thank you to Patbox for these wonderful serializers =)
    data class RegistrySerializer<T>(val registry: Registry<T>) : JsonSerializer<T>, JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T? {
            var parsed = if (json.isJsonPrimitive) registry.get(ResourceLocation.tryParse(json.asString)) else null
            if (parsed == null)
                printError("There was an error while deserializing a Registry Type: $registry")
            return parsed
        }
        override fun serialize(src: T, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(registry.getId(src).toString())
        }
    }

    data class CodecSerializer<T>(val codec: Codec<T>) : JsonSerializer<T>, JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): T? {
            return try {
                codec.decode(JsonOps.INSTANCE, json).getOrThrow(false) { }.first
            } catch (e: Throwable) {
                printError("There was an error while deserializing a Codec: $codec")
                null
            }
        }

        override fun serialize(src: T?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return try {
                if (src != null)
                    codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow(false) { }
                else
                    JsonNull.INSTANCE
            } catch (e: Throwable) {
                printError("There was an error while serializing a Codec: $codec")
                JsonNull.INSTANCE
            }
        }
    }

    class ResourceLocationSerializer: JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ResourceLocation? {
            return ResourceLocation.tryParse(json.asString)
        }
        override fun serialize(src: ResourceLocation, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.asString())
        }
    }
}

fun <A, B> Codec<A>.recordCodec(id: String, getter: Function<B, A>): RecordCodecBuilder<B, A> {
    return this.fieldOf(id).forGetter(getter)
}

fun <A, B> Codec<A>.optionalRecordCodec(id: String, getter: Function<B, A>, default: A): RecordCodecBuilder<B, A> {
    return this.fieldOf(id).orElse(default).forGetter(getter)
}