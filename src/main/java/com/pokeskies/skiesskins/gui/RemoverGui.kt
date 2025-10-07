package com.pokeskies.skiesskins.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
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

class RemoverGui(
    player: ServerPlayer,
    private val returnGUI: IRefreshableGui? = null
) : IRefreshableGui(
    ConfigManager.REMOVER_GUI.type.type, player, false
) {
    init {
        SkiesSkins.INSTANCE.inventoryInstances[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        for ((_, item) in ConfigManager.REMOVER_GUI.items) {
            setSlots(item.slots, GuiElementBuilder
                .from(item.createItemStack(player))
                .setCallback { type ->
                    for (actionEntry in item.clickActions) {
                        val action = actionEntry.value
                        if (action.matchesClick(type)) {
                            action.executeAction(player, this)
                        }
                    }
                }
            )
        }

        val party: PlayerPartyStore = Cobblemon.storage.getParty(player)
        for (i in 0..5) {
            val slotItem = ConfigManager.REMOVER_GUI.partySlots[i + 1] ?: continue
            val partyPokemon: Pokemon? = party.get(i)
            val button = GuiElementBuilder
                .from(
                    Utils.getOrRunOther(
                        partyPokemon,
                        { slotItem.filled.createItemStack(player, partyPokemon) },
                        { slotItem.empty.createItemStack(player, partyPokemon) }
                    )
                )
                .setCallback { type ->
                    val pokemon = Cobblemon.storage.getParty(player).get(i)
                    if (pokemon != null) {
                        val skinEntry = SkiesSkinsAPI.getPokemonSkin(pokemon)
                        if (skinEntry == null) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Utils.deserializeText("<red>This Pokemon does not have any skin applied!"))
                            return@setCallback
                        }

                        if (!skinEntry.second.infinite) {
                            val user = SkiesSkinsAPI.getUserData(player)
                            val result = user.inventory.add(
                                UserSkinData(
                                    skinEntry.first
                                )
                            )

                            if (!result || !SkiesSkinsAPI.saveUserData(player, user)) {
                                player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                                player.sendMessage(
                                    Component.literal("There was an error while removing this skin!")
                                        .withStyle { it.withColor(ChatFormatting.RED) })
                                close()
                                return@setCallback
                            }
                        }

                        for (aspect in skinEntry.second.aspects.remove) {
                            PokemonProperties.parse(aspect).apply(pokemon)
                        }
                        pokemon.persistentData.remove(SkiesSkinsAPI.TAG_SKIN_DATA)
                        if (ConfigManager.CONFIG.untradable) pokemon.tradeable = true

                        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("Successfully removed the skin!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                        close()
                    }
                }
                .build()

            for (slot in (if (partyPokemon != null) slotItem.filled else slotItem.empty).slots) {
                setSlot(slot, button)
            }
        }
    }

    override fun onClose() {
        SkiesSkins.INSTANCE.inventoryInstances.remove(player.uuid, this)
        returnGUI?.open()
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, ConfigManager.REMOVER_GUI.title))
    }
}
