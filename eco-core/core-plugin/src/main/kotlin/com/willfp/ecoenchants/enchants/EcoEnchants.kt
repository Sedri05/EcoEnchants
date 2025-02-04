package com.willfp.ecoenchants.enchants

import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.ecoenchants.EcoEnchantsPlugin
import com.willfp.ecoenchants.integrations.EnchantRegistrations
import com.willfp.ecoenchants.rarity.EnchantmentRarities
import com.willfp.ecoenchants.target.EnchantmentTargets
import com.willfp.ecoenchants.type.EnchantmentTypes
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

@Suppress("UNUSED")
object EcoEnchants {
    private val BY_KEY = HashBiMap.create<String, EcoEnchant>()
    private val BY_NAME = HashBiMap.create<String, EcoEnchant>()

    /**
     * Get all registered [EcoEnchant]s.
     *
     * @return A list of all [EcoEnchant]s.
     */
    @JvmStatic
    fun values(): Set<EcoEnchant> {
        return ImmutableSet.copyOf(BY_KEY.values)
    }

    /**
     * Get [String]s for all registered [EcoEnchant]s.
     *
     * @return A list of all [EcoEnchant]s.
     */
    @JvmStatic
    fun keySet(): Set<String> {
        return ImmutableSet.copyOf(BY_KEY.keys)
    }

    /**
     * Get [EcoEnchant] matching key.
     *
     * @param key The key to search for.
     * @return The matching [EcoEnchant], or null if not found.
     */
    @JvmStatic
    fun getByKey(key: String?): EcoEnchant? {
        return if (key == null) {
            null
        } else BY_KEY[key]
    }

    /**
     * Get [EcoEnchant] matching name.
     *
     * @param name The name to search for.
     * @return The matching [EcoEnchant], or null if not found.
     */
    @JvmStatic
    fun getByName(name: String?): EcoEnchant? {
        return if (name == null) {
            null
        } else BY_NAME[name]
    }

    /**
     * Update all [EcoEnchant]s.
     *
     * @param plugin Instance of EcoEnchants.
     */
    @JvmStatic
    @ConfigUpdater
    fun update(plugin: EcoEnchantsPlugin) {
        EnchantmentRarities.update(plugin)
        EnchantmentTargets.update(plugin)
        EnchantmentTypes.update(plugin)

        for (enchant in values()) {
            removeEnchant(enchant)
        }

        for ((id, config) in plugin.fetchConfigs("enchants")) {
            if (config.has("effects")) {
                LibReforgeEcoEnchant(id, config, plugin)
            }
        }
    }

    /**
     * Remove [EcoEnchant] from EcoEnchants.
     *
     * @param enchant The [EcoEnchant] to remove.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    fun removeEnchant(enchant: EcoEnchant) {
        BY_KEY.remove(enchant.id)
        BY_NAME.remove(enchant.id)

        Enchantment::class.java.getDeclaredField("byKey")
            .apply {
                isAccessible = true
                (get(null) as MutableMap<NamespacedKey, Enchantment>).apply { remove(enchant.key) }
            }

        Enchantment::class.java.getDeclaredField("byName")
            .apply {
                isAccessible = true
                (get(null) as MutableMap<String, Enchantment>).apply { remove(enchant.name) }
            }

        EnchantRegistrations.removeEnchant(enchant)
    }

    /**
     * Add new [EcoEnchant] to EcoEnchants.
     *
     * Only for internal use, enchants are automatically added in the constructor.
     *
     * @param enchant The [EcoEnchant] to add.
     */
    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    internal fun addNewEnchant(enchant: EcoEnchant) {
        BY_KEY[enchant.id] = enchant
        BY_NAME[ChatColor.stripColor(enchant.displayName)] = enchant

        register(enchant)
    }

    /**
     * Register a new [Enchantment] with the server.
     *
     * @param enchantment The [Enchantment] to add.
     */
    @JvmStatic
    fun register(enchantment: Enchantment) {
        Enchantment::class.java.getDeclaredField("acceptingNew")
            .apply {
                isAccessible = true
                set(null, true)
            }

        Enchantment.registerEnchantment(enchantment)
        EnchantRegistrations.registerEnchantments()
    }
}
