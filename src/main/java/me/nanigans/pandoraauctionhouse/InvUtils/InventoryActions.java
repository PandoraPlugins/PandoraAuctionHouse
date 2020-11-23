package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import org.bukkit.inventory.Inventory;

public abstract class InventoryActions {
    protected AuctionHouseInventory info;

    public InventoryActions(AuctionHouseInventory info){
        this.info = info;
    }

    public abstract void click(String method) throws NoSuchMethodException;
    public abstract void getPlayerListings();
    public abstract void pageForward();
    public abstract void pageBackwards();
    public abstract void sortBy();
    public abstract void searchBy();
    public abstract Inventory createInventory();


}
