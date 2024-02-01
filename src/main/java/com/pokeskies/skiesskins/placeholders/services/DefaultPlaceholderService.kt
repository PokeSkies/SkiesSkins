package com.pokeskies.skiesskins.placeholders.services

import com.pokeskies.skiesskins.placeholders.IPlaceholderService
import net.minecraft.server.level.ServerPlayer

class DefaultPlaceholderService : IPlaceholderService {
    override fun parsePlaceholders(player: ServerPlayer, text: String): String {
        return text
            .replace("%player%", player.name.string)
            .replace("%player_uuid%", player.uuid.toString())
    }
}