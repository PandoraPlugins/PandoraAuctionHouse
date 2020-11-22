package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
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

    public static final LinkedHashMap<AuctionCategories, ItemStack> itemCategories = new LinkedHashMap<AuctionCategories, ItemStack>(){{
                put(AuctionCategories.ALL, createItem(Material.NETHER_STAR, "All", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.ALL));
                put(AuctionCategories.BUILDINGBLOCKS, createItem(Material.BRICK, "Building Blocks", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.BUILDINGBLOCKS));
                put(AuctionCategories.DECORATIONS, createItem("175/5", "Decorations", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.DECORATIONS));
                put(AuctionCategories.REDSTONE, createItem(Material.REDSTONE, "Redstone", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.REDSTONE));
                put(AuctionCategories.TRANSPORTATION, createItem(Material.POWERED_RAIL, "Transportation", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.TRANSPORTATION));
                put(AuctionCategories.FOOD, createItem(Material.APPLE, "Food", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.FOOD));
                put(AuctionCategories.TOOLS, createItem(Material.IRON_AXE, "Tools", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.TOOLS));
                put(AuctionCategories.COMBAT, createItem(Material.GOLD_SWORD, "Combat", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.COMBAT));
                put(AuctionCategories.BREWING, createItem(Material.POTION, "Brewing", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.BREWING));
                put(AuctionCategories.MATERIALS, createItem(Material.STICK, "Materials", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.MATERIALS));
                put(AuctionCategories.MISC, createItem(Material.LAVA_BUCKET, "Miscellaneous", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.MISC));

            }};
    public static final int[] itemPlaces = {11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41};
    public static final int[] categoryPlaces = {9, 18, 27, 36};


    /**
     * Creates the home auction house page
     * @param info the auction house inventory information
     * @return a new auction house inventory
     */
    public static Inventory createAuctionHousePage(AuctionHouseInventory info){

        Inventory inventory = Bukkit.createInventory(info.getPlayer(), 54, "Auction House");
        inventory.setItem(0, createItem("160/14", "Up", "METHOD~categoryUp"));//
        inventory.setItem(45, createItem("160/5", "Down", "METHOD~categoryDown"));//

        inventory.setItem(47, createItem("160/14", "Back", "METHOD~pageBack"));//
        inventory.setItem(50, createItem("160/5", "Forward", "METHOD~pageForward"));//
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        meta.setOwner(info.getPlayer().getName());
        meta.setDisplayName("Your listings");
        head.setItemMeta(meta);
        inventory.setItem(16, head);

        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBT.SORTBY+"~"+NBT.A_Z, "METHOD~sortBy");

        ItemMeta itemMeta = filters.getItemMeta();
        itemMeta.setLore(Arrays.asList((info.getSorted() == Sorted.A_Z ? ChatColor.GOLD : ChatColor.GRAY)+"A-Z",
                (info.getSorted() == Sorted.Z_A ? ChatColor.GOLD : ChatColor.GRAY)+"Z-A"));
        filters.setItemMeta(itemMeta);
        inventory.setItem(25, filters);

        inventory.setItem(26, createItem(Material.NAME_TAG, "Search by Name", "METHOD~search"));
        inventory.setItem(34, createItem(Material.BOOKSHELF, ChatColor.AQUA+"Auction Information"));
        inventory.setItem(53, createItem(Material.PAPER, "Balance: "+
                ChatColor.GREEN+"$" +Essentials.getPlugin(Essentials.class).getUser(info.getPlayer()).getMoney()));

        PrimitiveIterator.OfInt iterator = Arrays.stream(categoryPlaces).iterator();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+4; i++) {//category items
            inventory.setItem(iterator.next(), (ItemStack) itemCategories.values().toArray()[i]);
        }

        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material

        iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*materialList.size(); i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                inventory.setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }

        for (int itemPlace : itemPlaces) {//empty slots
            if(inventory.getItem(itemPlace) == null)
            inventory.setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }
        for (int i = 0; i < inventory.getContents().length; i++) {//black border
            if(inventory.getContents()[i] == null)
                inventory.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15));
        }

        return inventory;
    }

    /**
     * Creates a new item from a string material id
     * @param material the material of the item "ID/ID"
     * @param name the name of the item
     * @param nbt any nbt values
     * @return a new itemstack
     */
    public static ItemStack createItem(String material, String name, String... nbt){

        ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(material.split("/")[0])),
                1, Byte.parseByte(material.split("/")[1]));

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        item = NBTData.setNBT(item, nbt);
        return item;

    }
    /**
     * Creates a new item from a material
     * @param material the material of the item
     * @param name the name of the item
     * @param nbt any nbt values
     * @return a new itemstack
     */
    public static ItemStack createItem(Material material, String name, String... nbt){

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        item = NBTData.setNBT(item, nbt);
        return item;

    }

}
