package me.nanigans.pandoraauctionhouse;

import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryActionUtils;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryActions;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
    private ItemStack lastClicked;
    private Sorted sorted = Sorted.A_Z;
    private volatile String message;
    private boolean isTyping;

    public AuctionHouseInventory(Player player){

        this.player = player;
        this.plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        Inventory inventory = InventoryCreation.createAuctionHousePage(this);
        player.openInventory(inventory);
        this.inventory = inventory;

    }

    /**
     * When inventory is clicked, it'll run the method the item has specified under METHOD NBT
     * @param event InventoryClickEvent
     */
    @EventHandler
    public void onInvClick(InventoryClickEvent event) {

        if(event.getInventory().equals(this.inventory)){
            event.setCancelled(true);
            final ItemStack currentItem = event.getCurrentItem();
            if(event.getAction().toString().contains("PICKUP")) {
                ((Player) event.getWhoClicked()).playSound(this.player.getLocation(), Sound.valueOf("CLICK"), 2, 1);
                if (currentItem != null) {
                    if (NBTData.containsNBT(currentItem, "METHOD")) {
                        String method = NBTData.getNBT(currentItem, "METHOD");
                        this.lastClicked = currentItem;
                        try {
                            InventoryActions.class.getMethod(method, AuctionHouseInventory.class).invoke(new InventoryActions(), this);
                        } catch (NoSuchMethodException | NoClassDefFoundError | IllegalAccessException | InvocationTargetException ignored) {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }else if(event.getAction().toString().contains("DROP")){
                if(currentItem != null){

                    String data = NBTData.getNBT(currentItem, "SETCATEGORY");
                    if(data != null){

                        AuctionCategories category = AuctionCategories.valueOf(data);
                        if(category == AuctionCategories.ALL){
                            for(AuctionCategories cate : AuctionCategories.values())
                            ConfigUtils.removePlayerListing(this, cate);
                        }else {
                            String msg = ConfigUtils.removePlayerListing(this, category);
                            if(msg != null) this.player.sendMessage(msg);
                        }


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

    @EventHandler
    public void chat(AsyncPlayerChatEvent event){

        if(event.getPlayer().getUniqueId().equals(this.getPlayer().getUniqueId()) && this.isTyping){

            this.message = event.getMessage();
            this.isTyping = false;
            event.setCancelled(true);

        }

    }

    public void swapInvs(Inventory newInv){

        this.inventory = newInv;
        this.swappingInvs = true;
        this.player.openInventory(newInv);
        this.swappingInvs = false;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isSwappingInvs() {
        return swappingInvs;
    }

    public void setSwappingInvs(boolean swappingInvs) {
        this.swappingInvs = swappingInvs;
    }

    public Sorted getSorted() {
        return sorted;
    }

    public void setSorted(Sorted sorted) {
        this.sorted = sorted;
    }

    public ItemStack getLastClicked() {
        return lastClicked;
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
