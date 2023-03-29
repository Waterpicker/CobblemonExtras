package dev.chasem.cobblemonextras.permissions

import com.cobblemon.mod.common.api.permission.CobblemonPermission
import com.cobblemon.mod.common.api.permission.PermissionLevel

object CobblemonExtrasPermissions {
    val COMPSEE_PERMISSION = CobblemonPermission("cobblemonextras.command.compsee", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val COMPESEE_OTHER_PERMISSION = CobblemonPermission("cobblemonextras.command.compseeother", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val PC_PERMISSION = CobblemonPermission("cobblemonextras.command.pc", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val POKESEE_PERMISSION = CobblemonPermission("cobblemonextras.command.pokesee", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val POKESEE_OTHER_PERMISSION = CobblemonPermission("cobblemonextras.command.pokeseeother", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val POKESHOUT_PERMISSION = CobblemonPermission("cobblemonextras.command.pokeshout", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val POKETRADE_PERMISSION = CobblemonPermission("cobblemonextras.command.poketrade", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
}