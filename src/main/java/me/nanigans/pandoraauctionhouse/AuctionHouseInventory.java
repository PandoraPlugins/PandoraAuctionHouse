package me.nanigans.pandoraauctionhouse;

import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryActions;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.InvocationTargetException;

public class AuctionHouseInventory implements Listener {

    private final Player player;
    private final PandoraAuctionHouse plugin;
    private Inventory inventory;
    private boolean swappingInvs = false;
    private int page = 0;
    private byte categoryFirst = 0;
    private AuctionCategories category = AuctionCategories.ALL;
    private InventoryType invType = InventoryType.MAIN;

    public AuctionHouseInventory(Player player){

        this.player = player;
        this.plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        Inventory inventory = InventoryCreation.createAuctionHousePage(this);
        player.openInventory(inventory);
        this.inventory = inventory;

    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {

        if(event.getInventory().equals(this.inventory)){
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).playSound(this.player.getLocation(), Sound.valueOf("CLICK"), 2, 1);

            if(event.getCurrentItem() != null) {
                if(NBTData.containsNBT(event.getCurrentItem(), "METHOD")) {
                    String method = NBTData.getNBT(event.getCurrentItem(), "METHOD");

                    try {
                        InventoryActions.class.getMethod(method, AuctionHouseInventory.class).invoke(new InventoryActions(), this);
                    }catch(NoSuchMethodException | NoClassDefFoundError | IllegalAccessException | InvocationTargetException ignored){
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }

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

    public InventoryType getInvType() {
        return invType;
    }

    public void setInvType(InventoryType invType) {
        this.invType = invType;
    }

    public byte getCategoryFirst() {
        return categoryFirst;
    }

    public void setCategoryFirst(byte categoryFirst) {
        this.categoryFirst = (byte) Math.max(Math.min(InventoryCreation.itemCategories.size()-4, categoryFirst), 0);
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
        this.page = Math.max(page, 0);
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
