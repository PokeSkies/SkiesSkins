package com.pokeskies.skiesskins.commands.subcommands

import ca.landonjw.gooeylibs2.api.UIManager
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.gui.RemoverGui
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

class RemoverCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("remover")
            .requires(Permissions.require("skiesskins.command.remover", 4))
            .then(Commands.argument("player", EntityArgument.player())
                .requires(Permissions.require("skiesskins.command.remover.other", 4))
                .executes { ctx ->
                    remove(ctx, EntityArgument.getPlayer(ctx, "player"))
                }
            )
            .executes { ctx ->
                remove(ctx, ctx.source.playerOrException)
            }
            .build()
    }

    companion object {
        private fun remove(
            ctx: CommandContext<CommandSourceStack>,
            player: ServerPlayer
        ): Int {
            UIManager.openUIForcefully(player, RemoverGui(player))
            return 1
        }
    }
}