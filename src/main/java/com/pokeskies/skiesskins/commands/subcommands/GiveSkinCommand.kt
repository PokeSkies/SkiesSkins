package com.pokeskies.skiesskins.commands.subcommands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument

class GiveSkinCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("giveskin")
            .requires(Permissions.require("skiesskins.command.giveskin", 4))
            .then(Commands.argument("skin_id", StringArgumentType.string())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggest(ConfigManager.SKINS.keys.stream(), builder)
                }
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .then(Commands.argument("player", EntityArgument.players())
                        .executes(GiveSkinCommand::giveOther)
                    )
                    .requires { obj: CommandSourceStack -> obj.isPlayer }
                    .executes(GiveSkinCommand::giveSelf)
                )
            )
            .build()
    }

    companion object {
        private fun giveSelf(ctx: CommandContext<CommandSourceStack>): Int {
            try {
                val player = ctx.source.player
                if (player != null) {
                    val skinId = StringArgumentType.getString(ctx, "skin_id")
                    val amount = IntegerArgumentType.getInteger(ctx, "amount")

                    val skinEntry = ConfigManager.SKINS[skinId]
                    if (skinEntry == null) {
                        ctx.source.sendMessage(
                            Component.text("Cannot find a skin entry from the ID: $skinId")
                            .color(NamedTextColor.RED))
                        return 1
                    }

                    SkiesSkinsAPI.giveUserSkin(player, skinId, amount)
                    ctx.source.sendMessage(
                        Component.text("You gave yourself $amount skins of id $skinId")
                        .color(NamedTextColor.GREEN))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return 1
        }

        private fun giveOther(ctx: CommandContext<CommandSourceStack>): Int {
            val players = EntityArgument.getPlayers(ctx, "player")
            val skinId = StringArgumentType.getString(ctx, "skin_id")
            val amount = IntegerArgumentType.getInteger(ctx, "amount")

            val skinEntry = ConfigManager.SKINS[skinId]
            if (skinEntry == null) {
                ctx.source.sendMessage(
                    Component.text("Cannot find a skin entry from the ID: $skinId")
                    .color(NamedTextColor.RED))
                return 1
            }

            var given = 0
            for (player in players) {
                SkiesSkinsAPI.giveUserSkin(player, skinId, amount)
                given++
            }
            ctx.source.sendMessage(
                Component.text("You gave $amount skins of id $skinId to $given players!")
                .color(NamedTextColor.GREEN))
            return 1
        }
    }
}