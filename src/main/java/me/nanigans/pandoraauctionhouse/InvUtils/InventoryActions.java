package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;
import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.itemPlaces;

public class InventoryActions {

    public static void sortBy(AuctionHouseInventory info){



    }

    /**
     * Changes the category topic to the item clicked
     * @param info auction house info
     */
    public static void categoryChange(AuctionHouseInventory info){
        ItemStack item = info.getLastClicked();
        if(item != null){
            String data = NBTData.getNBT(item, "SETCATEGORY");
            if(data != null){
                final AuctionCategories auctionCategories = AuctionCategories.valueOf(data);
                if(auctionCategories != info.getCategory()) {
                    info.setCategory(auctionCategories);
                    InventoryActionUtils.replaceCategory(info);
                }
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

        for (int itemPlace : itemPlaces) {
            info.getInventory().setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }
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

        for (int itemPlace : itemPlaces) {
            info.getInventory().setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*itemPlaces.length; i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }

    }

}
