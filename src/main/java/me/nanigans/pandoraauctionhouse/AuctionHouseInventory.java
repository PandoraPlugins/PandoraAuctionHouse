package me.nanigans.pandoraauctionhouse;

import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.Classifications.ItemType;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.InvUtils.ListingInventoryActions;
import me.nanigans.pandoraauctionhouse.InvUtils.MainInventoryActions;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
    private Material viewingMaterial;
    private final MainInventoryActions mainInventory = new MainInventoryActions(this);
    private final ListingInventoryActions listingInventory = new ListingInventoryActions(this);

    public AuctionHouseInventory(Player player){

        this.player = player;
        this.plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        Inventory inventory = this.mainInventory.createInventory();
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

                        if(this.invType == InventoryType.MAIN)
                            this.mainInventory.click(method);
                        else if(this.invType == InventoryType.LISTINGS)
                            this.listingInventory.click(method);

                    }
                }
            }else if(event.getAction().toString().contains("DROP")){
                if(currentItem != null){

                    if(NBTData.containsNBT(currentItem, "SETCATEGORY")){
                        String data = NBTData.getNBT(currentItem, "SETCATEGORY");
                        AuctionCategories category = AuctionCategories.valueOf(data);
                        if(category == AuctionCategories.ALL){
                            for(AuctionCategories cate : AuctionCategories.values())
                            ConfigUtils.removePlayerListing(this, cate);
                        }else {
                            String msg = ConfigUtils.removePlayerListing(this, category);
                            if(msg != null) this.player.sendMessage(msg);
                        }
                    }else if(NBTData.containsNBT(currentItem, "METHOD")){
                        String data = NBTData.getNBT(currentItem, "METHOD");
                        if(data.equals("openMaterial")){
                            if(this.getCategory() != AuctionCategories.ALL) {
                                ConfigUtils.removePlayerListing(this, this.getCategory(), currentItem.getType());
                                ConfigUtils.removePlayerListing(this, AuctionCategories.ALL, currentItem.getType());
                            }else{
                                final AuctionCategories itemCategory = ItemType.getItemCategory(currentItem);
                                ConfigUtils.removePlayerListing(this, this.getCategory(), currentItem.getType());
                                ConfigUtils.removePlayerListing(this, itemCategory, currentItem.getType());

                            }
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
        if(newInv == null) {
            this.player.closeInventory();
            this.player.sendMessage(ChatColor.RED+"Nothing is here");
        }else {
            this.inventory = newInv;
            this.swappingInvs = true;
            this.player.openInventory(newInv);
            this.swappingInvs = false;
        }

    }

    public MainInventoryActions getMainInventory() {
        return mainInventory;
    }

    public ListingInventoryActions getListingInventory() {
        return listingInventory;
    }

    public Material getViewingMaterial() {
        return viewingMaterial;
    }

    public void setViewingMaterial(Material viewingMaterial) {
        this.viewingMaterial = viewingMaterial;
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
        this.categoryFirst = (byte) Math.max(Math.min(MainInventoryActions.itemCategories.size()-4, categoryFirst), 0);
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
