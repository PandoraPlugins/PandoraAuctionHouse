package me.nanigans.pandoraauctionhouse.ItemUtils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemData {

    public static Map<Enchantment, Integer> serializeEnchantment(ItemStack item){

        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : item.getEnchantments().entrySet()) {
            enchants.put(enchantmentIntegerEntry.getKey(), enchantmentIntegerEntry.getValue());
        }
        return enchants;
    }

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
