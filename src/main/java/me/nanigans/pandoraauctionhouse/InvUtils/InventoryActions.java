package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.Title;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.stream.Collectors;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;
import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.itemPlaces;

public class InventoryActions {


    public static void playerListings(AuctionHouseInventory info){



    }

    /**
     * Allows the player to seach for an item by its name
     * @param info auctionhouse info
     */
    public static void searchByItem(AuctionHouseInventory info){

        info.setSwappingInvs(true);
        info.getPlayer().closeInventory();

        new Title().send(info.getPlayer(), ChatColor.GOLD+"Enter an item name", ChatColor.WHITE+"20 seconds",
                10, 100, 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis()+10000;
                info.setTyping(true);
                while(System.currentTimeMillis() < time){

                    if(info.getMessage() != null){

                        String message = info.getMessage();//TODO: possibly figure out how to get it by the actual item name and not material
                        final List<ExtractedResult> result = FuzzySearch.extractAll(message, ConfigUtils.getMaterialsFromCategory(info.getCategory())
                                .stream().map(Enum::toString).collect(Collectors.toList())).stream().filter(i -> i.getScore() > 70).collect(Collectors.toList());
                        info.setMessage(null);
                        info.setTyping(false);

                        if(result.size() > 0){

                            InventoryActionUtils.replaceByItems(info, result);

                        }else{
                            info.getPlayer().sendMessage(ChatColor.RED+"Couldn't find any listed items in the current category matching your query");
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    info.getPlayer().openInventory(info.getInventory());
                                }
                            }.runTaskLaterAsynchronously(info.getPlugin(), 40);
                        }

                        return;
                    }

                }
                info.setMessage(null);
                info.setTyping(false);
                info.getPlayer().openInventory(info.getInventory());

            }
        }.runTaskAsynchronously(info.getPlugin());


    }

    /**
     * Changes the sort method between A-Z and Z-A
     * @param info auction house info
     */
    public static void sortBy(AuctionHouseInventory info){
        if(info.getSorted() == Sorted.A_Z) {
            info.setSorted(Sorted.Z_A);
        }
        else info.setSorted(Sorted.A_Z);
        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBTEnums.NBT.SORTBY+"~"+ Sorted.A_Z, "METHOD~sortBy");

        ItemMeta itemMeta = filters.getItemMeta();
        itemMeta.setLore(Arrays.asList((info.getSorted() == Sorted.A_Z ? ChatColor.GOLD : ChatColor.GRAY)+"A-Z",
                (info.getSorted() == Sorted.Z_A ? ChatColor.GOLD : ChatColor.GRAY)+"Z-A"));
        filters.setItemMeta(itemMeta);
        info.getInventory().setItem(25, filters);
        InventoryActionUtils.replaceCategory(info);

    }

    /**
     * Changes the category topic to the item clicked
     * @param info auction house info
     */
    public static void categoryChange(AuctionHouseInventory info){
        InventoryActionUtils.clearItemBoard(info);
        ItemStack item = info.getLastClicked();
        if(item != null){
            String data = NBTData.getNBT(item, "SETCATEGORY");
            if(data != null){
                final AuctionCategories auctionCategories = AuctionCategories.valueOf(data);
                //if(auctionCategories != info.getCategory()) {
                    info.setCategory(auctionCategories);
                    InventoryActionUtils.replaceCategory(info);
                //}
            }
        }
    }
    /**
     * Moves the category list down
     * @param info info about auctionhouse
     */
    public static void categoryDown(AuctionHouseInventory info){
    info.setCategoryFirst((byte) (info.getCategoryFirst()+1));
    PrimitiveIterator.OfInt iterator = Arrays.stream(InventoryCreation.categoryPlaces).iterator();
    final Object[] objects = InventoryCreation.itemCategories.values().toArray();
    for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+InventoryCreation.categoryPlaces.length; i++) {//category items
        info.getInventory().setItem(iterator.next(), (ItemStack) objects[i]);
    }

}

    /**
     * Moves the category list up
     * @param info info about auctionhouse
     */
    public static void categoryUp(AuctionHouseInventory info){

        info.setCategoryFirst((byte) (info.getCategoryFirst()-1));

        PrimitiveIterator.OfInt iterator = Arrays.stream(InventoryCreation.categoryPlaces).iterator();
        final Object[] objects = InventoryCreation.itemCategories.values().toArray();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+InventoryCreation.categoryPlaces.length; i++) {//category items
            info.getInventory().setItem(iterator.next(), (ItemStack) objects[i]);
        }
    }

    /**
     * Moves the page forward by one in the main AH inventory
     * @param info auction house info
     */
    public static void pageForward(AuctionHouseInventory info){
        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material

        info.setPage((int) Math.min(Math.floor((double)materialList.size()/itemPlaces.length), info.getPage()+1));

        InventoryActionUtils.clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*itemPlaces.length; i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }
    }

    /**
     * Moves the page backwards one in the main AH inventory
     * @param info auction house info
     */
    public static void pageBack(AuctionHouseInventory info){
        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material
        info.setPage(info.getPage()-1);

        InventoryActionUtils.clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*itemPlaces.length; i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }

    }

}
