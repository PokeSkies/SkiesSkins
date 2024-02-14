package com.pokeskies.skiesskins.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.stream.Stream

class ResetShopCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("resetshop")
            .requires(Permissions.require("skiesskins.command.resetshop", 4))
            .then(
                Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("shop", StringArgumentType.string())
                        .suggests { _, builder ->
                            SharedSuggestionProvider.suggest(ConfigManager.SHOPS.keys.stream(), builder)
                        }
                        .then(Commands.literal("random")
                            .then(Commands.argument("set", StringArgumentType.string())
                                .suggests { ctx, builder ->
                                    val shop = ConfigManager.SHOPS[StringArgumentType.getString(ctx, "shop")]
                                    SharedSuggestionProvider.suggest(
                                        shop?.skins?.random?.keys?.stream() ?: Stream.empty(),
                                        builder
                                    )
                                }
                                .executes { ctx ->
                                    resetShop(
                                        ctx,
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        StringArgumentType.getString(ctx, "shop"),
                                        ShopType.RANDOM,
                                        StringArgumentType.getString(ctx, "set")
                                    )
                                }
                            )
                            .executes { ctx ->
                                resetShop(
                                    ctx,
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    StringArgumentType.getString(ctx, "shop"),
                                    ShopType.RANDOM,
                                    null
                                )
                            }
                        )
                        .then(Commands.literal("static")
                            .executes { ctx ->
                                resetShop(
                                    ctx,
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    StringArgumentType.getString(ctx, "shop"),
                                    ShopType.STATIC,
                                    null
                                )
                            }
                        )
                        .then(Commands.literal("packages")
                            .executes { ctx ->
                                resetShop(
                                    ctx,
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    StringArgumentType.getString(ctx, "shop"),
                                    ShopType.PACKAGES,
                                    null
                                )
                            }
                        )
                        .executes { ctx ->
                            resetShop(
                                ctx,
                                EntityArgument.getPlayers(ctx, "targets"),
                                StringArgumentType.getString(ctx, "shop"),
                                null,
                                null
                            )
                        }
                    )
                .executes { ctx ->
                    resetAllShops(
                        ctx,
                        EntityArgument.getPlayers(ctx, "targets"),
                    )
                }
            )
            .build()
    }

    companion object {
        private fun resetAllShops(
            ctx: CommandContext<CommandSourceStack>,
            players: Collection<ServerPlayer>
        ): Int {
            if (SkiesSkins.INSTANCE.storage == null) {
                ctx.source.sendMessage(
                    Component.literal("The storage system is not available at the moment. Please try again later.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            if (players.size == 1) {
                val player = players.first()
                val userData = SkiesSkinsAPI.getUserData(player)
                userData.shopData = HashMap()
                SkiesSkinsAPI.saveUserData(player, userData)
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You reset the shop data of ${player.name.string}")
                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            } else {
                var playerCount = 0
                for (player in players) {
                    val userData = SkiesSkinsAPI.getUserData(player)
                    userData.shopData = HashMap()
                    SkiesSkinsAPI.saveUserData(player, userData)
                    playerCount++
                }
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You reset the shop data of $playerCount players")
                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            }
            return 1
        }

        private fun resetShop(
            ctx: CommandContext<CommandSourceStack>,
            players: Collection<ServerPlayer>,
            shop: String,
            type: ShopType?,
            set: String?,
        ): Int {
            if (SkiesSkins.INSTANCE.storage == null) {
                ctx.source.sendMessage(
                    Component.literal("The storage system is not available at the moment. Please try again later.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val shopConfig = ConfigManager.SHOPS[shop]
            if (shopConfig == null) {
                ctx.source.sendMessage(
                    Component.literal("Could not find a Shop configuration by the id: $shop")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val successes: MutableList<ServerPlayer> = mutableListOf()
            // this a little fucked, buttttttt
            when (type) {
                ShopType.RANDOM -> {
                    if (set == null) {
                        for (player in players) {
                            val userData = SkiesSkinsAPI.getUserData(player)
                            val shopData = userData.shopData[shop]
                            if (shopData != null) {
                                shopData.randomData = HashMap()
                                SkiesSkinsAPI.saveUserData(player, userData)
                            }
                            successes.add(player)
                        }
                    } else {
                        val randomSet = shopConfig.skins.random[set]
                        if (randomSet == null) {
                            ctx.source.sendMessage(
                                Component.literal("Could not find a random set within Shop $shop by the id: $set")
                                    .withStyle { it.withColor(ChatFormatting.RED) }
                            )
                            return 1
                        }
                        for (player in players) {
                            val userData = SkiesSkinsAPI.getUserData(player)
                            val shopData = userData.shopData[shop]
                            if (shopData != null) {
                                val randomData = shopData.randomData[set]
                                if (randomData != null) {
                                    shopData.randomData.remove(set)
                                    SkiesSkinsAPI.saveUserData(player, userData)
                                }
                            }
                            successes.add(player)
                        }
                    }
                }
                ShopType.STATIC -> {
                    for (player in players) {
                        val userData = SkiesSkinsAPI.getUserData(player)
                        val shopData = userData.shopData[shop]
                        if (shopData != null) {
                            shopData.staticData = HashMap()
                            SkiesSkinsAPI.saveUserData(player, userData)
                        }
                        successes.add(player)
                    }
                }
                ShopType.PACKAGES -> {
                    for (player in players) {
                        val userData = SkiesSkinsAPI.getUserData(player)
                        val shopData = userData.shopData[shop]
                        if (shopData != null) {
                            shopData.packagesData = HashMap()
                            SkiesSkinsAPI.saveUserData(player, userData)
                        }
                        successes.add(player)
                    }
                }
                else -> {
                    for (player in players) {
                        val userData = SkiesSkinsAPI.getUserData(player)
                        userData.shopData.remove(shop)
                        SkiesSkinsAPI.saveUserData(player, userData)
                        successes.add(player)
                    }
                }
            }

            for (player in players) {
                if (SkiesSkins.INSTANCE.inventoryControllers.containsKey(player.uuid)) {
                    SkiesSkins.INSTANCE.inventoryControllers[player.uuid]!!.refresh()
                }
            }

            if (successes.size == 1) {
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You reset shop data for ${successes.first().name.string}")
                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            } else {
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You reset the shop data of $successes players")
                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            }
            return 1
        }
    }

    enum class ShopType {
        RANDOM, STATIC, PACKAGES
    }
}