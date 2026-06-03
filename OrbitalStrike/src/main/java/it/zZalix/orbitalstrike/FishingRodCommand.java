package it.zZalix.orbitalstrike;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FishingRodCommand implements CommandExecutor {

    private final OrbitalStrikePlugin plugin;

    public FishingRodCommand(OrbitalStrikePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cQuesto comando può essere eseguito solo in gioco!");
            return true;
        }

        String type = args.length > 0 ? args[0].toLowerCase() : "nuke";
        int amountOfRods = 1; // Di default consegna 1 canna da pesca

        if (type.equals("nuke") || type.equals("stab") || type.equals("wolf")) {
            if (args.length >= 2) {
                try {
                    amountOfRods = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cLa quantità di canne deve essere un numero valido!");
                    return true;
                }
            }
            ItemStack rod = plugin.createCustomRod(type, 1, ""); // Ogni canna ha 1 carica/uso
            giveItems(player, rod, amountOfRods);
            player.sendMessage("§aHai ricevuto " + amountOfRods + " canna/e da pesca " + type.toUpperCase() + "!");
            return true;
        }

        if (type.equals("stasis")) {
            if (args.length < 4) {
                player.sendMessage("§cUso corretto: /fishingrod stasis <x> <y> <z> [quantità_canne]");
                return true;
            }
            double x, y, z;
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
                if (args.length >= 5) {
                    amountOfRods = Integer.parseInt(args[4]);
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cCoordinate e quantità devono essere numeri validi!");
                return true;
            }
            String coords = x + "," + y + "," + z;
            ItemStack rod = plugin.createCustomRod(type, 1, coords);
            giveItems(player, rod, amountOfRods);
            player.sendMessage("§aHai ricevuto " + amountOfRods + " canna/e STASIS collegate a " + coords + "!");
            return true;
        }

        if (type.equals("custom")) {
            if (args.length < 2) {
                player.sendMessage("§cUso corretto: /fishingrod custom <mob_id> [quantità_canne]");
                return true;
            }
            String mobId = args[1].toLowerCase();
            if (args.length >= 3) {
                try {
                    amountOfRods = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cLa quantità di canne deve essere un numero!");
                    return true;
                }
            }
            ItemStack rod = plugin.createCustomRod(type, 1, mobId);
            giveItems(player, rod, amountOfRods);
            player.sendMessage("§aHai ricevuto " + amountOfRods + " canna/e CUSTOM per " + mobId + "!");
            return true;
        }

        player.sendMessage("§cTipo non valido! Scegli tra: nuke, stab, wolf, stasis, custom.");
        return true;
    }

    private void giveItems(Player player, ItemStack item, int amount) {
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(item.clone());
        }
    }
}