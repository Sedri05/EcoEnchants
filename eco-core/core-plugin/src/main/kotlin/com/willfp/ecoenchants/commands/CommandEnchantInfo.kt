package com.willfp.ecoenchants.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecoenchants.enchants.EcoEnchants
import com.willfp.ecoenchants.enchants.EnchantInfo
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil


class CommandEnchantInfo(plugin: EcoPlugin) : PluginCommand(
    plugin, "enchantinfo", "ecoenchants.command.enchantinfo", true
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender as Player

        if (args.isEmpty()) {
            sender.sendMessage(this.plugin.langYml.getMessage("missing-enchant"))
            return
        }

        val nameBuilder = StringBuilder()

        args.forEach { arg -> nameBuilder.append(arg).append(" ") }
        var searchName = nameBuilder.toString()
        searchName = searchName.substring(0, searchName.length - 1)

        val enchantment = EcoEnchants.getByName(searchName)

        if (enchantment == null) {
            val message = plugin.langYml.getMessage("not-found").replace("%name%", searchName)
            sender.sendMessage(message)
            return
        }

        EnchantInfo.open(sender, plugin, enchantment)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        val names = EcoEnchants.values().mapNotNull { ChatColor.stripColor(it.displayName) }

        if (args.isEmpty()) {
            // Currently, this case is not ever reached
            return names
        }

        StringUtil.copyPartialMatches(args.joinToString(" "), names, completions)

        if (args.size > 1) { // Remove all previous words from the candidate of completions
            val finishedArgs = args.toMutableList()
            finishedArgs.drop(args.size - 1)
            val prefix = finishedArgs.joinToString(" ")
            completions.clear()
            completions.stream().map { it.removePrefix(prefix).trim() }
        }

        completions.sort()
        return completions
    }
}
