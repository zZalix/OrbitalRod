package it.zZalix.orbitalstrike;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FishingRodTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("nuke", "stab", "wolf", "stasis", "custom"), args[0]);
        }

        if (args.length == 2) {
            String type = args[0].toLowerCase();
            if (type.equals("nuke") || type.equals("stab") || type.equals("wolf")) {
                return Arrays.asList("1", "5", "10");
            }
            if (type.equals("custom")) {
                List<String> mobTypes = Arrays.stream(EntityType.values())
                        .filter(EntityType::isSpawnable)
                        .map(e -> e.name().toLowerCase())
                        .collect(Collectors.toList());
                return filter(mobTypes, args[1]);
            }
            if (type.equals("stasis")) {
                return Collections.singletonList("~");
            }
        }

        if (args.length == 3) {
            String type = args[0].toLowerCase();
            if (type.equals("stasis")) {
                return Collections.singletonList("~");
            }
            if (type.equals("custom")) {
                return Arrays.asList("1", "5", "10");
            }
        }

        if (args.length == 4) {
            String type = args[0].toLowerCase();
            if (type.equals("stasis")) {
                return Collections.singletonList("~");
            }
        }

        if (args.length == 5) {
            String type = args[0].toLowerCase();
            if (type.equals("stasis")) {
                return Arrays.asList("1", "5", "10");
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        String lowerInput = input.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}