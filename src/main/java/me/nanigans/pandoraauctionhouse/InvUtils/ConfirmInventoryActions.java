package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.Classifications.ItemType;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;
@FunctionalInterface
interface Confirm{
    void execute();
}
public class ConfirmInventoryActions extends InventoryActions{
    private final HashMap<String, Confirm> listingsMethods = new HashMap<>();
    private ItemStack item;//without data nbts
    private ItemStack itemWithData;
    private double price = 0;
    private UUID seller;
    private String itemUUID;

    public ConfirmInventoryActions(AuctionHouseInventory info) {
        super(info);
        listingsMethods.put("denyPurchase", this::denyPurchase);
        listingsMethods.put("confirmPurchase", this::confirmPurchase);
    }

    protected void denyPurchase(){
        info.setInvType(InventoryType.LISTINGS);
        final Inventory inventory = info.getListingInventory().createInventory();
        info.swapInvs(inventory);
    }

    protected void confirmPurchase(){
        User buyer = Essentials.getPlugin(Essentials.class).getUser(info.getPlayer());
        final BigDecimal cost = BigDecimal.valueOf(price);
        if(buyer.canAfford(cost)) {
            if (seller != null) {
                User selling = Essentials.getPlugin(Essentials.class).getUser(seller);
                try {

                    selling.giveMoney(cost);
                    buyer.takeMoney(cost);

                    final AuctionCategories itemCategory1 = ItemType.getItemCategory(item);
                    ConfigUtils.giveItemsBackToPlayer(info.getPlayer(),
                            info.getPlugin().path+"Categories/"+itemCategory1+"/"+item.getType()+"/"+seller.toString()+".yml",
                            itemCategory1, itemUUID);

                    if(info.getCategory() != AuctionCategories.ALL) {
                        ConfigUtils.removeItemFromPlayerListing(itemUUID, seller.toString(), info.getCategory(), item.getType());
                    }else{
                        final AuctionCategories itemCategory = ItemType.getItemCategory(item);
                        ConfigUtils.removeItemFromPlayerListing(itemUUID, seller.toString(), itemCategory, item.getType());
                    }
                    ConfigUtils.removeItemFromPlayerListing(itemUUID, seller.toString(), AuctionCategories.ALL, item.getType());

                    if(Bukkit.getOfflinePlayer(seller).isOnline()){
                        selling.sendMessage(ChatColor.GREEN+"A player has just bought an item from you in the auction house!");
                    }else{
                        selling.addMail("A player has bought an item from you in the auction house!");
                    }
                    info.getPlayer().closeInventory();
                }catch(MaxMoneyException err){
                    info.getPlayer().closeInventory();
                    info.getPlayer().sendMessage(ChatColor.RED+"Cannot purchase this item due to the seller having a the maximum balance allowed");
                }catch(Exception err){
                    info.getPlayer().sendMessage(ChatColor.RED+"Something went wrong when trying to purchase this item");
                    info.getPlayer().closeInventory();
                    err.printStackTrace();
                }
            }
        }else{
            info.getPlayer().sendMessage(ChatColor.RED+"You cannot afford this item");
            info.getPlayer().closeInventory();
        }

    }

    public void setItem(ItemStack item) {
        String priceStr = NBTData.getNBT(item, NBTEnums.NBT.PRICE.toString());
        if(priceStr != null){
            this.itemUUID = NBTData.getNBT(item, "UUID");

            price = Double.parseDouble(priceStr);
            seller = UUID.fromString(NBTData.getNBT(item, NBTEnums.NBT.SELLER.toString()));

            this.item = item;
        }else{
            info.getPlayer().closeInventory();
            info.getPlayer().sendMessage(ChatColor.RED+"This item cannot be bough");
        }
    }

    public void setItemWithData(ItemStack itemWithData) {
        this.itemWithData = NBTData.removeNBT(ListingInventoryActions.removeItemInformation(itemWithData), NBTEnums.NBT.METHOD.toString());
    }

    @Override
    public void click(String method) {
        listingsMethods.get(method).execute();
    }

    @Override
    protected void getPlayerListings() {

    }

    @Override
    protected void pageForward() {

    }

    @Override
    protected void pageBackwards() {

    }

    @Override
    protected void sortBy() {

    }

    @Override
    protected void searchBy() {

    }

    @Override
    public Inventory createInventory() {

        Inventory inv = Bukkit.createInventory(info.getPlayer(), 27, "Confirm Purchase");
        inv.setItem(4, createItem(Material.PAPER, "Purchase for: "+
                ChatColor.GOLD+"$"+NBTData.getNBT(info.getLastClicked(), NBTEnums.NBT.PRICE.toString())));

        for(int[] i : new int[][]{{0,1,2},{9,10,11},{18,19,20}}){
            for(int j : i){
                inv.setItem(j, createItem("160/14", ChatColor.RED+""+ ChatColor.BOLD+"Deny", NBTEnums.NBT.METHOD+"~denyPurchase"));
            }
        }
        for(int[]i : new int[][]{{6,7,8},{15, 16, 17},{24,25,26}}){
            for(int j : i){
                inv.setItem(j, createItem("160/5", ChatColor.GREEN+""+ChatColor.BOLD+"Confirm", NBTEnums.NBT.METHOD+"~confirmPurchase"));
            }
        }

        inv.setItem(13, ListingInventoryActions.removeItemInformation(this.item));

        return inv;
    }
}
