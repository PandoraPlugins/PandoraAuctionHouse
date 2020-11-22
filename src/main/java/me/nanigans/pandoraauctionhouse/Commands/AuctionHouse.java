package me.nanigans.pandoraauctionhouse.Commands;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.ItemType;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigCreators;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.PandoraAuctionHouse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AuctionHouse implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equals("auctionhouse")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if(args.length > 1 && args[0].equalsIgnoreCase("sell")){

                    if(player.getInventory().getItemInHand() != null) {
                        ItemStack item = player.getInventory().getItemInHand();
                        player.getInventory().removeItem(item);

                        try {
                            double price = Double.parseDouble(args[1]);
                            if(price >= 0) {
                                if (item.getEnchantments().size() > 0) {
                                    item = NBTData.setNBT(item, NBTEnums.NBT.ENCHANTS + "~" + ItemData.serializeEnchantment(item),
                                            NBTEnums.NBT.PRICE + "~" + args[1]);

                                    for (Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : item.getEnchantments().entrySet()) {
                                        item.removeEnchantment(enchantmentIntegerEntry.getKey());
                                    }
                                }
                                AuctionCategories category = ItemType.getItemCategory(item);

                                ConfigCreators.addItemToPlayer(category, player, item);

                                player.sendMessage(ChatColor.GOLD + "You have listed " + ChatColor.WHITE
                                        + item.getAmount() + "x" + item.getType() + " for $" + ChatColor.GREEN + price);

                            }else{
                                player.sendMessage(ChatColor.RED+"Please enter a valid price above 0");
                                return true;
                            }
                        }catch(NumberFormatException e){
                            player.sendMessage(ChatColor.RED+"Please enter a valid price above 0");
                            return true;
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
