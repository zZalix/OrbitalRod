package it.zZalix.orbitalstrike;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class OrbitalStrikePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Registrazione degli eventi
        getServer().getPluginManager().registerEvents(new FishingRodListener(this), this);

        // Registrazione dei comandi e dell'autocompilazione
        if (getCommand("fishingrod") != null) {
            getCommand("fishingrod").setExecutor(new FishingRodCommand(this));
            getCommand("fishingrod").setTabCompleter(new FishingRodTabCompleter());
        }

        getLogger().info("Plugin OrbitalStrike abilitato correttamente!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin OrbitalStrike disabilitato.");
    }

    // Metodo helper per generare le canne da pesca con PDC integrato
    public ItemStack createCustomRod(String type, int amount, String extraData) {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();
        if (meta == null) return rod;

        meta.setDisplayName("§d§lOrbital Rod: " + type.toUpperCase());
        List<String> lore = new ArrayList<>();
        lore.add("§7Tipo di attacco: §e" + type.toUpperCase());
        lore.add("§7Cariche/Quantità: §a" + amount);
        if (extraData != null && !extraData.isEmpty()) {
            lore.add("§7Dettagli: §b" + extraData);
        }
        lore.add("");
        lore.add("§dLancia e riavvolgi la lenza per fare fuoco!");
        meta.setLore(lore);

        // Salvataggio dei dati all'interno del PersistentDataContainer dell'oggetto
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(this, "orbital_type"), PersistentDataType.STRING, type.toUpperCase());
        pdc.set(new NamespacedKey(this, "orbital_amount"), PersistentDataType.INTEGER, amount);
        if (extraData != null) {
            pdc.set(new NamespacedKey(this, "orbital_extra"), PersistentDataType.STRING, extraData);
        }

        rod.setItemMeta(meta);
        return rod;
    }
}