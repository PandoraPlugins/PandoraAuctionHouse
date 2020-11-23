package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.ConfigUtils.YamlGenerator;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;

public class ListingInventoryActions extends InventoryActions{


    public ListingInventoryActions(AuctionHouseInventory info) {
        super(info);
    }

    @Override
    public void click(String method) throws NoSuchMethodException {
        try {
            this.getClass().getMethod(method).invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getPlayerListings() {

    }

    @Override
    public void pageForward() {

    }

    @Override
    public void pageBackwards() {

    }

    @Override
    public void sortBy() {

    }

    @Override
    public void searchBy() {

    }


    @Override
    public Inventory createInventory(){

        try {
            int invListingSize = 45;
            Inventory inv = Bukkit.createInventory(info.getPlayer(), invListingSize + 9, "Player Listings");

            inv.setItem(inv.getSize() - 9, createItem(Material.BARRIER, ChatColor.RED + "Back", "METHOD~back"));
            inv.setItem(inv.getSize() - 8, createItem(Material.PAPER, "Balance: "+ChatColor.GOLD+"$" + Essentials.getPlugin(Essentials.class).getUser(info.getPlayer()).getMoney()));
            ItemStack item = ItemData.createPlaySkull(info.getPlayer());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Your Listings");
            item.setItemMeta(meta);
            inv.setItem(inv.getSize()-5, NBTData.setNBT(item, "METHOD~getPlayerListings"));
            inv.setItem(inv.getSize()-6, createItem(Material.COMPASS, "Page Backwards", "METHOD~pageBackwards"));
            inv.setItem(inv.getSize()-4, createItem(Material.COMPASS, "Page Forward", "METHOD~pageForward"));
            ItemStack sortBy = createItem(Material.DIAMOND, "Sort By:", "METHOD~sortBy");
            meta = sortBy.getItemMeta();
            meta.setLore(Arrays.asList(ChatColor.GOLD+"Newest", ChatColor.GRAY+"Oldest", ChatColor.GRAY+"A-Z",
                    ChatColor.GRAY+"Z-A", ChatColor.GRAY+"Cheapest", ChatColor.GRAY+"Expensive"));
            sortBy.setItemMeta(meta);

            inv.setItem(inv.getSize()-2, sortBy);
            inv.setItem(inv.getSize()-1, createItem(Material.NAME_TAG, "Search by player", "searchBy"));

            File matFile = new File(info.getPlugin().path + "Categories/" + info.getCategory() + "/" + info.getViewingMaterial());
            if (matFile.exists()) {

                final File[] files = matFile.listFiles();
                for (File file : files) {
                    if(file.getAbsolutePath().endsWith(".yml")) {

                        YamlGenerator yaml = new YamlGenerator(file.getAbsolutePath());
                        final FileConfiguration data = yaml.getData();
                        final List<ItemStack> selling = (List<ItemStack>) data.getList("selling");
                        short invPlace = 0;

                        for (ItemStack itemStack : selling) {
                            if (invPlace < invListingSize) {
                                String enchantData = NBTData.getNBT(itemStack, NBT.ENCHANTS.toString());
                                if (enchantData != null) {
                                    Map<Enchantment, Integer> enchants = ItemData.parseEnchantNBT(enchantData);
                                    itemStack.addEnchantments(enchants);
                                }
                                createItemInformation(itemStack);

                                inv.setItem(invPlace, itemStack);
                                invPlace++;
                            } else break;

                        }

                    }
                }
                return inv;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static void createItemInformation(ItemStack item){

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.add(ChatColor.GRAY+"Price: "+ChatColor.GOLD+"$"+NBTData.getNBT(item, NBT.PRICE.toString()));

        final long time = Long.parseLong(NBTData.getNBT(item, NBT.DATEEXPIRE.toString()));
        final long currentTime = new Date().getTime();
        if(time > currentTime) {
            final long days = TimeUnit.DAYS.convert(time - currentTime, TimeUnit.MILLISECONDS);
            final long hours = TimeUnit.HOURS.convert((time - currentTime)%86400000, TimeUnit.MILLISECONDS);
            final long minutes = (time-currentTime)/(60 * 1000) % 60;
            lore.add(ChatColor.GRAY + "Expires: " + ChatColor.WHITE + days+"D " + hours+"H "+minutes+"M");//ItemData.formatTime(time));
        }else lore.add(ChatColor.GRAY+"Expires: " + ChatColor.DARK_RED+"EXPIRED");

        OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(NBTData.getNBT(item, NBT.SELLER.toString())));
        lore.add(ChatColor.GRAY+"Seller: "+ChatColor.GOLD+seller.getName());
        meta.setLore(lore);
        item.setItemMeta(meta);

    }

}
