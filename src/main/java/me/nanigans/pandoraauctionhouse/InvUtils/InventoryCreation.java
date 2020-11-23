package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ConfigUtils.YamlGenerator;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class InventoryCreation {

    /*************************************
     *
     *    PLAYER LISTING INVENTORY
     *
     ************************************/

    public static final short invListingSize = 45;

    public static Inventory createListingInventory(AuctionHouseInventory info){

        try {
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

    public static ItemStack createItemInformation(ItemStack item){

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
        return item;

    }

    /*******************************************************
     *
     *     MAIN AUCTION HOUSE INVENTORY
     *
     ******************************************************/
    public static final LinkedHashMap<AuctionCategories, ItemStack> itemCategories = new LinkedHashMap<AuctionCategories, ItemStack>(){{
        ItemStack item = createItem(Material.NETHER_STAR, "All", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.ALL);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList("Press Q on any category", "to remove all your listings", "under it"));
        item.setItemMeta(meta);
                put(AuctionCategories.ALL, item);
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

        inventory.setItem(47, createItem("160/14", "Back", "METHOD~pageBackwards"));//
        inventory.setItem(50, createItem("160/5", "Forward", "METHOD~pageForward"));//
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        meta.setOwner(info.getPlayer().getName());
        meta.setDisplayName("Your listings");
        head.setItemMeta(meta);
        inventory.setItem(16, NBTData.setNBT(head, "METHOD~getPlayerListings"));

        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBT.SORTBY+"~"+Sorted.A_Z, "METHOD~sortBy");

        ItemMeta itemMeta = filters.getItemMeta();
        itemMeta.setLore(Arrays.asList((info.getSorted() == Sorted.A_Z ? ChatColor.GOLD : ChatColor.GRAY)+"A-Z",
                (info.getSorted() == Sorted.Z_A ? ChatColor.GOLD : ChatColor.GRAY)+"Z-A"));
        filters.setItemMeta(itemMeta);
        inventory.setItem(25, filters);

        inventory.setItem(26, createItem(Material.NAME_TAG, "Search By Item Name", "METHOD~searchBy"));
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
