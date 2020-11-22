package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import org.bukkit.inventory.Inventory;

public class InventoryActions {

public static void categoryDown(AuctionHouseInventory info){

    info.setCategoryFirst((byte) (info.getCategoryFirst()+1));
    Inventory inv = InventoryCreation.createAuctionHousePage(info);
    info.swapInvs(inv);

}

    public static void categoryUp(AuctionHouseInventory info){

        info.setCategoryFirst((byte) (info.getCategoryFirst()-1));
        Inventory inv = InventoryCreation.createAuctionHousePage(info);
        info.swapInvs(inv);

    }

}
