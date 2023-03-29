package dev.chasem.cobblemonextras.commands

import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.requiresWithPermission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.chasem.cobblemonextras.CobblemonExtras
import dev.chasem.cobblemonextras.permissions.CobblemonExtrasPermissions
import dev.chasem.cobblemonextras.screen.CompSeeHandlerFactory
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object CompSee {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>): LiteralCommandNode<ServerCommandSource> =
            with(dispatcher) {
                register(
                    CommandManager.literal("compsee").permission(CobblemonExtrasPermissions.COMPSEE_PERMISSION)
                        .executes { ctx: CommandContext<ServerCommandSource> -> self(ctx) }
                )
                register(
                    CommandManager.literal("compseeother")
                        .permission(CobblemonExtrasPermissions.COMPESEE_OTHER_PERMISSION)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes { ctx: CommandContext<ServerCommandSource> -> other(ctx) })
                )
            }

        private fun self(ctx: CommandContext<ServerCommandSource>): Int = with(ctx.source.player) {
            ctx.source.player?.openHandledScreen(CompSeeHandlerFactory());
            1
        }

        private fun other(ctx: CommandContext<ServerCommandSource>): Int = with(ctx.source.player) {
            val otherPlayer = EntityArgumentType.getPlayer(ctx, "player")
            takeIf { otherPlayer != null }?.openHandledScreen(CompSeeHandlerFactory(otherPlayer))
            1
        }
}