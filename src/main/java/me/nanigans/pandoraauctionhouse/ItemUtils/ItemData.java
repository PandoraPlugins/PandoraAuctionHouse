package me.nanigans.pandoraauctionhouse.ItemUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Skull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemData {

    public static String formatTime(long time){

        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return formatter.format(new Date(time));
    }

    public static ItemStack createPlaySkull(Player player){

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) item.getItemMeta());
        meta.setOwner(player.getName());
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createPlaySkull(OfflinePlayer player){

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) item.getItemMeta());
        meta.setOwner(player.getName());
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createPlaySkull(UUID uuid){

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) item.getItemMeta());
        meta.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Serealizes any enchantments including the Glow effect
     * @param item the item to serialize
     * @return a map with enchantment and its id
     */
    public static Map<Enchantment, Integer> serializeEnchantment(ItemStack item){

        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : item.getEnchantments().entrySet()) {
            enchants.put(enchantmentIntegerEntry.getKey(), enchantmentIntegerEntry.getValue());
        }
        return enchants;
    }

    /**
     * Converts a string of text into an enchantment
     * @param text the text to parse
     * @return a map with enchantmentand its id
     */
    public static Map<Enchantment, Integer> parseEnchantNBT(String text){

        Map<Enchantment, Integer> map = new LinkedHashMap<>();
        for(String keyValue : text.split(" *& *")) {
            String[] pairs = keyValue.split(" *= *", 2);

            Matcher match = Pattern.compile("[0-9]+").matcher(pairs[0]);
            int enchatnID = -1;
            if (match.find()) {
                enchatnID = Integer.parseInt(match.group(0));
            }

            map.put(Enchantment.getById(enchatnID), pairs.length == 1 ? 1 : Integer.parseInt(pairs[1].charAt(0)+""));
        }
        return map;

    }

}
