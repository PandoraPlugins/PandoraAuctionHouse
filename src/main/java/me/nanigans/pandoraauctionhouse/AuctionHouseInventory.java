package me.nanigans.pandoraauctionhouse;

import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class AuctionHouseInventory implements Listener {

    private final Player player;
    private final PandoraAuctionHouse plugin;
    private Inventory inventory;
    private boolean swappingInvs = false;
    private int page = 0;
    private byte categoryFirst = 0;
    private AuctionCategories category = AuctionCategories.ALL;

    public AuctionHouseInventory(Player player){

        this.player = player;
        this.plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        Inventory inventory = InventoryCreation.createAuctionHousePage(this);
        player.openInventory(inventory);


    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event){

        if(event.getInventory().equals(this.inventory)){
            event.setCancelled(true);

        }

    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) throws Throwable {
        if(!this.swappingInvs){

            HandlerList.unregisterAll(this);
            this.finalize();

        }
    }

    public void swapInvs(Inventory newInv){

        this.inventory = newInv;
        this.swappingInvs = true;
        this.player.openInventory(newInv);
        this.swappingInvs = false;

    }

    public byte getCategoryFirst() {
        return categoryFirst;
    }

    public void setCategoryFirst(byte categoryFirst) {
        this.categoryFirst = categoryFirst;
    }

    public AuctionCategories getCategory() {
        return category;
    }

    public void setCategory(AuctionCategories category) {
        this.category = category;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public PandoraAuctionHouse getPlugin() {
        return plugin;
    }
}
