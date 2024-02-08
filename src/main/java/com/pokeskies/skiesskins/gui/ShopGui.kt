package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.page.PageAction
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.SkinOptions
import com.pokeskies.skiesskins.data.shop.RandomShopData
import com.pokeskies.skiesskins.data.shop.RandomShopData.SkinData
import com.pokeskies.skiesskins.data.shop.UserShopData
import com.pokeskies.skiesskins.utils.RandomCollection
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import java.util.HashMap

class ShopGui(
    private val player: ServerPlayer,
    private val shopId: String,
    private val shop: ShopConfig,
) : RefreshableGUI() {
    private val template: ChestTemplate = ChestTemplate.Builder(shop.options.size)
        .build()

    init {
        this.subscribe(this, Runnable { refresh() })
        SkiesSkins.INSTANCE.inventoryControllers[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        if (SkiesSkins.INSTANCE.storage == null) {
            Utils.printDebug("The storage system is not available at the moment. Please try again later.")
            return
        }

        val userData = SkiesSkinsAPI.getUserData(player)

        if (!userData.shopData.containsKey(shopId)) {
            userData.shopData[shopId] = generateShopData()
            SkiesSkinsAPI.saveUserData(player, userData)
        }

        val shopData = userData.shopData[shopId]
        if (shopData == null) {
            Utils.printDebug("There was an error getting your data from the storage system. Please try again later.")
            return
        }

        // Iterate through every set of Random skins in this shop
        for ((setId, set) in shop.skins.random) {
            var randomShopData = shopData.randomData[setId]
            // If the random data does not contain the set, then we have an error
            if (randomShopData == null ||
                SkiesSkins.INSTANCE.shopManager.userNeedsReset(shopId, setId, randomShopData.resetTime)) {
                randomShopData = generateRandomShopData(set)
                shopData.randomData[setId] = randomShopData
                SkiesSkinsAPI.saveUserData(player, userData)
            }

            val slots = set.gui.slots.toMutableList()
            // Iterate through every skin in this set and attempt to add it to the GUI
            for (skinData in randomShopData.skins) {
                val slot = slots.removeFirstOrNull() ?: break
                // Check to ensure that this skin even exists in this shop
                val shopSkinData = set.skins[skinData.id]
                if (shopSkinData == null) {
                    this.template.set(
                        slot,
                        Utils.getErrorButton("<red>Error while fetching shop skin data. It's missing?")
                    )
                } else {
                    // Check to ensure that this skin even exists in the configs
                    val config = ConfigManager.SKINS[skinData.id]
                    if (config == null) {
                        this.template.set(
                            slot,
                            Utils.getErrorButton("<red>Error while fetching skin. It's missing?")
                        )
                    } else {
                        template.set(slot, GooeyButton.builder()
                            .display(set.gui.available.createItemStack(player, shop, shopSkinData, config))
                            .onClick { ctx ->
                                if (SkiesSkins.INSTANCE.shopManager.userNeedsReset(shopId, setId, randomShopData.resetTime)) {
                                    refresh()
                                    return@onClick
                                }
                                if (shopSkinData.cost.all { it.withdraw(player, shop) }) {
                                    SkiesSkinsAPI.giveUserSkin(player, skinData.id, 1)
                                    player.sendMessage(
                                        Component.literal("You purchased a skin!")
                                            .withStyle { it.withColor(ChatFormatting.GREEN) }
                                    )
                                    player.playNotifySound(
                                        SoundEvents.PLAYER_LEVELUP,
                                        SoundSource.MASTER,
                                        0.5f,
                                        0.5f
                                    )
                                } else {
                                    player.sendMessage(
                                        Component.literal("You do not have enough to purchase this skin!")
                                            .withStyle { it.withColor(ChatFormatting.RED) }
                                    )
                                    player.playNotifySound(
                                        SoundEvents.LAVA_EXTINGUISH,
                                        SoundSource.MASTER,
                                        0.5f,
                                        0.5f
                                    )
                                }
                            }
                            .build()
                        )
                    }
                }
            }
        }
    }

    fun generateShopData(): UserShopData {
        return UserShopData(generateRandomData(), HashMap(), HashMap())
    }

    fun generateRandomData(): HashMap<String, RandomShopData> {
        val data = HashMap<String, RandomShopData>()

        for ((setId, set) in shop.skins.random) {
            data[setId] = generateRandomShopData(set)
        }

        return data
    }

    fun generateRandomShopData(set: SkinOptions.RandomSet): RandomShopData {
        val rc: RandomCollection<Pair<String, SkinOptions.RandomSet.RandomSkin>> = RandomCollection()
        for ((skinId, skin) in set.skins) {
            rc.add(skin.weight.toDouble(), Pair(skinId, skin))
        }

        val skins: MutableList<SkinData?> = mutableListOf()

        for (slot in set.gui.slots) {
            if (rc.size() > 0) {
                val skinPair = rc.next(true)
                skins.add(
                    if (ConfigManager.SKINS.containsKey(skinPair.first))
                        SkinData(skinPair.first, 0)
                    else
                        null
                )
            }
        }

        return RandomShopData(System.currentTimeMillis(), skins)
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(shop.options.title)
    }

    override fun onClose(action: PageAction) {
        SkiesSkins.INSTANCE.inventoryControllers.remove(player.uuid, this)
    }
}