package com.pokeskies.skiesskins.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class ReloadCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("reload")
            .requires(Permissions.require("skiesskins.command.reload", 4))
            .executes(Companion::reload)
            .build()
    }

    companion object {
        fun reload(ctx: CommandContext<CommandSourceStack>): Int {
            SkiesSkins.INSTANCE.reload()
            ctx.source.sendMessage(Component.text("Reloaded SkiesSkins").color(NamedTextColor.GREEN))
            return 1
        }
    }
}