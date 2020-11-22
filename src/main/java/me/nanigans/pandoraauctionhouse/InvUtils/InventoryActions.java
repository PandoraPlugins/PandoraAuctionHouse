package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.PrimitiveIterator;

public class InventoryActions {

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

}
