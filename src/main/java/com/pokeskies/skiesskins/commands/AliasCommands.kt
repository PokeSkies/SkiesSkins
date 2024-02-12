package com.pokeskies.skiesskins.commands

import ca.landonjw.gooeylibs2.api.UIManager
import com.mojang.brigadier.CommandDispatcher
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.gui.ShopGui
import com.pokeskies.skiesskins.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class AliasCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        for ((shopId, shop) in ConfigManager.SHOPS) {
            if (shop.options.enabled) {
                for (command in shop.options.aliases) {
                    dispatcher.register(
                        Commands.literal(command)
                            .requires { obj: CommandSourceStack -> obj.isPlayer }
                            .requires(Permissions.require("skiesskins.open.$shopId"))
                            .executes { ctx ->
                                val player = ctx.source.player
                                if (player == null) {
                                    ctx.source.sendMessage(
                                        Component.text("Must be a player to run this command!")
                                            .color(NamedTextColor.RED)
                                    )
                                    return@executes 1
                                }
                                if (!Permissions.check(player, "skiesskins.open.$shopId")) {
                                    ctx.source.sendMessage(
                                        Component.text("You don't have permission to run this command!")
                                            .color(NamedTextColor.RED)
                                    )
                                    return@executes 1
                                }

                                val shopConfig = ConfigManager.SHOPS[shopId]

                                if (shopConfig == null) {
                                    Utils.printError("There was an error while running the command '$command' for player '${player.name.string}'! " +
                                            "The Shop '$shopId' returned null. Was it deleted, renamed, or changed?")
                                    ctx.source.sendMessage(
                                        Component.text("Error while attempting to open this shop! Check the console for more information.")
                                            .color(NamedTextColor.RED)
                                    )
                                    return@executes 1
                                }

                                UIManager.openUIForcefully(player, ShopGui(player, shopId, shopConfig))
                                return@executes 1
                            }
                    )
                }
            }
        }
    }
}