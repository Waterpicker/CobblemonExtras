package dev.chasem.cobblemonextras.commands

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLink
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager.addLink
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.chasem.cobblemonextras.CobblemonExtras
import dev.chasem.cobblemonextras.permissions.CobblemonExtrasPermissions
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.function.Predicate

object PC {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("pc")
                .permission(CobblemonExtrasPermissions.PC_PERMISSION)
                .executes { ctx: CommandContext<ServerCommandSource> -> execute(ctx) }
        )
    }

    private fun execute(ctx: CommandContext<ServerCommandSource>): Int {
        if (ctx.source.player != null) {
            val player = ctx.source.player
            val playerPc: PCStore? = try {
                storage.getPC(player!!.uuid)
            } catch (e: NoPokemonStoreException) {
                player!!.sendMessage(Text.of("Error accessing PC..."))
                return -1
            }
            addLink(PCLink(playerPc!!, player.uuid))
            OpenPCPacket(playerPc.uuid).sendToPlayer(player)
        } else {
            ctx.source.sendError(Text.of("Sorry, this is only for players."))
        }
        return 1
    }
}