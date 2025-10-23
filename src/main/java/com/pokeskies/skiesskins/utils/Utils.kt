package com.pokeskies.skiesskins.utils

import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.*
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
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

    fun parsePlaceholders(player: ServerPlayer?, text: String): String {
        if (player == null) return text
        return SkiesSkins.INSTANCE.placeholderManager.parse(player, text)
    }

    fun parsePokemonString(string: String, player: ServerPlayer?, pokemon: Pokemon?): Component {
        var result = parsePlaceholders(player, string)
        if (pokemon != null) {
            result = result.replace("%pokemon_skin_name%",
                SkiesSkinsAPI.getPokemonSkin(pokemon)?.name ?: "None"
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

    fun getErrorButton(text: String): GuiElementBuilder {
        return GuiElementBuilder
            .from(ItemStack(Items.BARRIER).also {
                it.applyComponents(DataComponentPatch.builder()
                    .set(DataComponents.ITEM_NAME, Utils.deserializeText(text))
                    .build())
            })
    }

    fun getRandomRanged(min: Int, max: Int): Int {
        return if (min > max || min == max) min else Random().nextInt(max - min + 1) + min
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
                codec.decode(JsonOps.INSTANCE, json).orThrow.first
            } catch (e: Throwable) {
                printError("There was an error while deserializing a Codec: $codec")
                null
            }
        }

        override fun serialize(src: T?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return try {
                if (src != null)
                    codec.encodeStart(JsonOps.INSTANCE, src).orThrow
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