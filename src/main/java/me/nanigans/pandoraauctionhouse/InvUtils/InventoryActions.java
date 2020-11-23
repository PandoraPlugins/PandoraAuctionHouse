package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
@FunctionalInterface
interface Method {
    void execute();
}

public abstract class InventoryActions {

    protected AuctionHouseInventory info;
    protected Map<String, Method> methods = new HashMap<>();


    public InventoryActions(AuctionHouseInventory info){
        this.info = info;
        methods.put("getPlayerListings", this::getPlayerListings);
        methods.put("pageForward", this::pageForward);
        methods.put("pageBackwards", this::pageBackwards);
        methods.put("sortBy", this::sortBy);
        methods.put("searchBy", this::searchBy);
    }

    public abstract void click(String method);
    protected abstract void getPlayerListings();
    protected abstract void pageForward();
    protected abstract void pageBackwards();
    protected abstract void sortBy();
    protected abstract void searchBy();
    public abstract Inventory createInventory();


}
