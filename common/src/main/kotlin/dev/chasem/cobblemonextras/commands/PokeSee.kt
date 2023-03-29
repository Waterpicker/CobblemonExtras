package dev.chasem.cobblemonextras.commands

import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.chasem.cobblemonextras.CobblemonExtras
import dev.chasem.cobblemonextras.permissions.CobblemonExtrasPermissions
import dev.chasem.cobblemonextras.screen.PokeSeeHandlerFactory
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

object PokeSee {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("pokesee")
                .permission(CobblemonExtrasPermissions.POKESEE_PERMISSION)
                .executes { ctx: CommandContext<ServerCommandSource> -> self(ctx) }
        )
        dispatcher.register(
            CommandManager.literal("pokeseeother")
                .permission(CobblemonExtrasPermissions.POKESEE_OTHER_PERMISSION)
                .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                        .executes { ctx: CommandContext<ServerCommandSource> -> other(ctx) })
        )
    }

    private fun self(ctx: CommandContext<ServerCommandSource>): Int {
        if (ctx.source.player != null) {
            val player = ctx.source.player
            player!!.openHandledScreen(PokeSeeHandlerFactory())
        }
        return 1
    }

    private fun other(ctx: CommandContext<ServerCommandSource>): Int {
        if (ctx.source.player != null) {
            val player = ctx.source.player
            val otherPlayerName = ctx.input.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1] // ctx.getArgument("player", String.class);
            val otherPlayer = ctx.source.server.playerManager.getPlayer(otherPlayerName)
            if (otherPlayer != null) {
                player!!.openHandledScreen(PokeSeeHandlerFactory(otherPlayer))
            }
        }
        return 1
    }
}