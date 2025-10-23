package com.pokeskies.skiesskins.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import com.pokeskies.skiesskins.utils.setSlots
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

class ApplyGui(
    player: ServerPlayer,
    val skinData: UserSkinData,
    val skin: SkinConfig
) : IRefreshableGui(
    ConfigManager.APPLY_GUI.type.type, player, false
) {
    private var shouldClose = false

    init {
        SkiesSkins.INSTANCE.inventoryInstances[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        for ((_, item) in ConfigManager.APPLY_GUI.items) {
            setSlots(item.slots, GuiElementBuilder.from(item.createItemStack(player))
                .setCallback { click ->
                    for (actionEntry in item.clickActions) {
                        val action = actionEntry.value
                        if (action.matchesClick(click)) {
                            action.executeAction(player, this)
                        }
                    }
                }
            )
        }

        val party: PlayerPartyStore = Cobblemon.storage.getParty(player)
        for (i in 0..5) {
            val slotItem = ConfigManager.APPLY_GUI.partySlots[i + 1] ?: continue
            val pokemon: Pokemon? = party.get(i)
            val button = GuiElementBuilder.from(
                Utils.getOrRunOther(
                    pokemon,
                    { slotItem.filled.createItemStack(player, pokemon) },
                    { slotItem.empty.createItemStack(player, pokemon) }
                )
            )
            .setCallback { type ->
                val pokemon = Cobblemon.storage.getParty(player).get(i)
                if (pokemon != null) {
                    SkiesSkinsAPI.getPokemonSkin(pokemon)?.let {
                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Utils.deserializeText("<red>This Pokemon already has the skin ${it.second.name}<reset> <red>applied!"))
                        return@setCallback
                    }

                    // Check if correct species
                    val species = PokemonSpecies.getByIdentifier(skin.species)!!
                    if (pokemon.species != species) {
                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("This skin can only be applied on ${species.name}!")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                        return@setCallback
                    }

                    // Check if the Pokemon contains ALL required aspects
                    if (skin.aspects.required.isNotEmpty() &&
                        skin.aspects.required.stream().noneMatch { pokemon.aspects.contains(it) }) {
                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("This skin requires aspects that are not applied to this Pokemon!")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                        return@setCallback
                    }

                    // Check if the Pokemon contains ANY blacklisted aspects
                    if (skin.aspects.blacklist.isNotEmpty() &&
                        skin.aspects.blacklist.stream().anyMatch { pokemon.aspects.contains(it) }) {
                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("This Pokemon contains aspects that are blacklisted!")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                        return@setCallback
                    }

                    if (!skin.infinite) {
                        val user = SkiesSkinsAPI.getUserData(player)
                        if (!user.inventory.remove(skinData) || !SkiesSkinsAPI.saveUserData(player, user)) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("There was an error while applying this skin!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                            shouldClose = true
                            close()
                            return@setCallback
                        }
                    }

                    for (aspect in skin.aspects.apply) {
                        PokemonProperties.parse(aspect).apply(pokemon)
                    }
                    pokemon.persistentData.putString(SkiesSkinsAPI.TAG_SKIN_DATA, skinData.id)
                    if (skin.untradable ?: ConfigManager.CONFIG.untradable) {
                        pokemon.tradeable = false
                    }

                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.15F, 1.0F)
                    player.sendMessage(Component.literal("Successfully applied the skin!")
                        .withStyle { it.withColor(ChatFormatting.GREEN) })
                    shouldClose = true
                    close()
                }
            }

            for (slot in (if (pokemon != null) slotItem.filled else slotItem.empty).slots) {
                setSlot(slot, button)
            }
        }
    }

    override fun getTitle(): Component {
        return skin.parse(ConfigManager.APPLY_GUI.title, player)
    }

    override fun onClose() {
        SkiesSkins.INSTANCE.inventoryInstances.remove(player.uuid, this)
        if (!shouldClose) {
            SkiesSkinsAPI.openSkinInventory(player)
        }
    }
}
