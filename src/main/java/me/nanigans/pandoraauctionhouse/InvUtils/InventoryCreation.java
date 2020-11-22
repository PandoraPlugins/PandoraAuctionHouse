package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class InventoryCreation {

    private static Map<AuctionCategories, ItemStack> itemCategories = new HashMap<AuctionCategories, ItemStack>(){{
        put(AuctionCategories.ALL, createItem(Material.NETHER_STAR, "All"));
        put(AuctionCategories.BUILDINGBLOCKS, createItem(Material.BRICK, "Building Blocks"));
        put(AuctionCategories.DECORATIONS, createItem("175/5", "Decorations"));
        put(AuctionCategories.REDSTONE, createItem(Material.REDSTONE, "Redstone"));
        put(AuctionCategories.TRANSPORTATION, createItem(Material.POWERED_RAIL, "Transportation"));
        put(AuctionCategories.FOOD, createItem(Material.APPLE, "Food"));
        put(AuctionCategories.TOOLS, createItem(Material.IRON_AXE, "Tools"));
        put(AuctionCategories.COMBAT, createItem(Material.GOLD_SWORD, "Combat"));
        put(AuctionCategories.BREWING, createItem(Material.POTION, "Brewing"));
        put(AuctionCategories.MATERIALS, createItem(Material.STICK, "Materials"));
        put(AuctionCategories.MISC, createItem(Material.LAVA_BUCKET, "Miscellaneous"));

    }};
    public static final int[] itemPlaces = {11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41};
    public static final int[] categoryPlaces = {9, 18, 27, 36};


    public static Inventory createAuctionHousePage(AuctionHouseInventory info){

        Inventory inventory = Bukkit.createInventory(info.getPlayer(), 54, "Auction House");
        inventory.setItem(0, createItem("160/14", "Up", NBT.CATEGORYUP+"~1"));
        inventory.setItem(45, createItem("160/5", "Down", NBT.CATEGORYDOWN+"~1"));

        inventory.setItem(47, createItem("160/14", "Back", NBT.PAGEBACK+"~1"));
        inventory.setItem(50, createItem("160/5", "Forward", NBT.PAGEFORWARD+"~1"));
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        meta.setOwner(info.getPlayer().getName());
        meta.setDisplayName("Your listings");
        head.setItemMeta(meta);
        inventory.setItem(16, head);
        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBT.SORTBY+"~"+NBT.NEWEST);
        ItemMeta itemMeta = filters.getItemMeta();
        meta.setLore(Arrays.asList(ChatColor.GOLD+"Newest", ChatColor.GRAY+"Oldest"));
        filters.setItemMeta(itemMeta);
        inventory.setItem(25, filters);
        inventory.setItem(26, createItem(Material.NAME_TAG, "Search by Name", NBT.SEARCH+"~"+NBT.NAME));
        inventory.setItem(34, createItem(Material.BOOKSHELF, ChatColor.AQUA+"Auction Information"));
        inventory.setItem(53, createItem(Material.PAPER, "Balance: "+
                ChatColor.GREEN+"$" +Essentials.getPlugin(Essentials.class).getUser(info.getPlayer()).getMoney()));

        final Iterator<ItemStack> iterator = Arrays.asList(itemCategories.get(AuctionCategories.ALL), itemCategories.get(AuctionCategories.BUILDINGBLOCKS),
                itemCategories.get(AuctionCategories.DECORATIONS), itemCategories.get(AuctionCategories.REDSTONE)).iterator();
        for (int categoryPlace : categoryPlaces) {
            inventory.setItem(categoryPlace, iterator.next());
        }

        for (int itemPlace : itemPlaces) {
            inventory.setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }
        for (int i = 0; i < inventory.getContents().length; i++) {
            if(inventory.getContents()[i] == null)
                inventory.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15));
        }

        return inventory;
    }

    public static ItemStack createItem(String material, String name, String... nbt){

        ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(material.split("/")[0])),
                1, Byte.parseByte(material.split("/")[1]));

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        item = NBTData.setNBT(item, nbt);
        return item;

    }

    public static ItemStack createItem(Material material, String name, String... nbt){

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        item = NBTData.setNBT(item, nbt);
        return item;

    }

}
