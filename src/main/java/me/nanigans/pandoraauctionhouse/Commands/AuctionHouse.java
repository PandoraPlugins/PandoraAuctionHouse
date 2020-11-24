package me.nanigans.pandoraauctionhouse.Commands;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.ItemType;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AuctionHouse implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equals("auctionhouse")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if(args.length > 1 && args[0].equalsIgnoreCase("sell")){

                    if(player.getInventory().getItemInHand() != null) {
                        ItemStack item = player.getInventory().getItemInHand();
                        player.getInventory().setItemInHand(null);

                        try {
                            double price = Double.parseDouble(args[1]);
                            if(price >= 0) {
                                if (item.getEnchantments().size() > 0) {

                                    try{
                                        final String s = item.toString();
                                    }catch(Exception ignored){
                                        item = NBTData.setNBT(item, NBT.ENCHANTS+"~"+ItemData.serializeEnchantment(item));
                                        for (Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : item.getEnchantments().entrySet()) {
                                            item.removeEnchantment(enchantmentIntegerEntry.getKey());
                                        }
                                    }
                                }
                                try{
                                    final String s = item.toString();
                                }catch(Exception ignored){
                                    player.sendMessage(ChatColor.RED+"This item cannot be sold");
                                    return true;
                                }
                                UUID uuid = UUID.randomUUID();
                                item = NBTData.setNBT(item,
                                        NBT.PRICE + "~" + args[1],
                                        NBT.DATEEXPIRE+"~"+(new Date().getTime()+604800000),//one week TODO: change this to cusomizable
                                        NBT.SELLER+"~"+player.getUniqueId(),
                                        NBT.DATESOLD+"~"+new Date().getTime(),
                                        "UUID~"+uuid
                                );

                                AuctionCategories category = ItemType.getItemCategory(item);

                                ConfigUtils.addItemToPlayer(category, player, item, uuid);
                                player.sendMessage(ChatColor.GOLD + "You have listed " + ChatColor.WHITE
                                        + item.getAmount() + "x" + item.getType() + " for $" + ChatColor.GREEN + price);

                            }else{
                                player.sendMessage(ChatColor.RED+"Please enter a valid price above 0");
                            }
                            return true;
                        }catch(NumberFormatException e){
                            player.sendMessage(ChatColor.RED+"Please enter a valid price above 0");
                            return true;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        player.sendMessage(ChatColor.RED+"You are not holding an item in your hand");
                        return true;
                    }

                }else new AuctionHouseInventory(player);

            }else{
                sender.sendMessage(ChatColor.RED+"Only players may use this command");
                return true;
            }

        }
        return false;
    }
}
