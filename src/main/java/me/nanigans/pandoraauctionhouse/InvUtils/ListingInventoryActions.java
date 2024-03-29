package me.nanigans.pandoraauctionhouse.InvUtils;

import com.avaje.ebean.validation.NotNull;
import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.Commands.DateParser;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ConfigUtils.YamlGenerator;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;

@FunctionalInterface
interface ListingMethods {
    void execute();
}

public class ListingInventoryActions extends InventoryActions{
    private final HashMap<String, ListingMethods> listingsMethods = new HashMap<>();
    private String invPath;
    private final static int invSize = 45;
    private final static Sorted[] sortTypes = {Sorted.NEWEST, Sorted.OLDEST, Sorted.A_Z, Sorted.Z_A, Sorted.CHEAPEST, Sorted.EXPENSIVE};
    private byte sortedIndex = 0;
    public ListingInventoryActions(AuctionHouseInventory info) {
        super(info);
        listingsMethods.put("confirmPurchase", this::confirmPurchase);
        listingsMethods.put("back", this::back);

    }

    protected void confirmPurchase(){

        if(!isOwnItem(info.getPlayer(), info.getLastClicked()) && isNotExpired(info.getLastClicked())) {
            info.getConfirmInventory().setItem(info.getLastClicked());
            info.getConfirmInventory().setItemWithData(info.getLastClicked());
            info.setInvType(InventoryType.CONFIRM);
            Inventory inv = info.getConfirmInventory().createInventory();
            new BukkitRunnable() {
                @Override
                public void run() {
                    info.swapInvs(inv);
                }
            }.runTask(info.getPlugin());
        }

    }

    private boolean isNotExpired(ItemStack item) {

        final long time = Long.parseLong(NBTData.getNBT(item, NBT.DATEEXPIRE.toString()));
        final long currentTime = new Date().getTime();
        return time > currentTime;
    }

    @Override
    public void click(String method) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(methods.containsKey(method))
                    methods.get(method).execute();
                else if(listingsMethods.containsKey(method))
                    listingsMethods.get(method).execute();
            }
        }.runTaskAsynchronously(info.getPlugin());
    }


    @Override
    protected void getPlayerListings() {

        File file = new File(this.invPath+"/"+info.getPlayer().getUniqueId());
        if(file.exists()){

            YamlGenerator yaml = new YamlGenerator(file.getAbsolutePath());
            final FileConfiguration data = yaml.getData();
            final List<ItemStack> selling = (List<ItemStack>) data.getList("selling");
            clearItems(45);
            displayItems(selling);

        }

    }

    @Override
    protected void pageForward() {

        List<ItemStack> listings = getAllListings();
        info.setPage((int) Math.min(Math.floor((double)listings.size()/invSize), info.getPage()+1));
        clearItems(invSize);
        displayItems(listings, NBTEnums.NBT.METHOD+"~confirmPurchase");

    }

    @Override
    protected void pageBackwards() {
        List<ItemStack> listings = getAllListings();
        info.setPage(info.getPage()-1);
        clearItems(invSize);
        displayItems(listings, NBTEnums.NBT.METHOD+"~confirmPurchase");
    }

    @Override
    protected void sortBy() {

        Sorted type = goThroughSorting();
        info.setSorted(type);
        displayItems(getAllListings(), NBTEnums.NBT.METHOD+"~confirmPurchase");

    }

    @Override
    protected void searchBy() {
        info.setSwappingInvs(true);
        info.getPlayer().closeInventory();

        new Title().send(info.getPlayer(), ChatColor.GOLD+"Enter a players name", ChatColor.WHITE+"20 seconds",
                10, 100, 10);

                long time = System.currentTimeMillis()+10000;
                info.setTyping(true);
                while(System.currentTimeMillis() < time){

                    if(info.getMessage() != null){

                        String message = info.getMessage();
                        final UUID uniqueId = Bukkit.getOfflinePlayer(message).getUniqueId();
                        if(uniqueId != null){
                            if(Bukkit.getOfflinePlayer(uniqueId).getPlayer() != null){

                                YamlGenerator yaml = new YamlGenerator(invPath+"/"+uniqueId.toString()+".yml");
                                final FileConfiguration data = yaml.getData();
                                final List<ItemStack> selling = (List<ItemStack>) data.getList("selling");
                                clearItems(invSize);
                                displayItems(selling, NBTEnums.NBT.METHOD+"~confirmPurchase");
                                info.swapInvs(info.getInventory());
                            }else{
                                info.getPlayer().sendMessage(ChatColor.RED+"Couldn't find that player");
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        info.getPlayer().openInventory(info.getInventory());
                                    }
                                }.runTaskLater(info.getPlugin(), 40);
                            }
                        }

                        info.setMessage(null);
                        info.setTyping(false);

                        return;
                    }

                }
                info.setMessage(null);
                info.setTyping(false);
                info.getPlayer().openInventory(info.getInventory());


    }

    protected void back(){
        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inv = info.getMainInventory().createInventory();
                info.swapInvs(inv);
                info.setInvType(InventoryType.MAIN);
            }
        }.runTask(info.getPlugin());
    }

    /**
     * This will go through the sorting list and highligh the current one to the player
     * @return the sorting type
     */
    private Sorted goThroughSorting(){

        if(info.getClickType().isLeftClick())
            if(sortedIndex == sortTypes.length-1)
                sortedIndex = 0;
            else sortedIndex = (byte) Math.min(sortedIndex+1, sortTypes.length-1);
        else if(info.getClickType().isRightClick())
            if(sortedIndex == 0)
                sortedIndex = (byte) (sortTypes.length-1);
            else sortedIndex = (byte) Math.max(0, sortedIndex-1);

        ItemStack sortBy = createItem(Material.DIAMOND, "Sort By:", NBTEnums.NBT.METHOD+"~sortBy");
        ItemMeta sMeta = sortBy.getItemMeta();
        List<String> lore = Arrays.asList(ChatColor.GRAY+"Newest", ChatColor.GRAY+"Oldest", ChatColor.GRAY+"A-Z",
                ChatColor.GRAY+"Z-A", ChatColor.GRAY+"Cheapest", ChatColor.GRAY+"Expensive");
        lore.set(sortedIndex, ChatColor.GOLD+lore.get(sortedIndex).substring(2));
        sMeta.setLore(lore);
        sortBy.setItemMeta(sMeta);
        this.info.getInventory().setItem(info.getInventory().getSize()-2, sortBy);
        return sortTypes[sortedIndex];
    }

    /**
     * Gets all the player listings of a material
     * @return a list of the items being listed
     */
    private  @NotNull List<ItemStack> getAllListings(){

        File file = new File(this.invPath);
        List<ItemStack> items = new ArrayList<>();
        if(file.exists()){

            File[] files = file.listFiles();
            for (File file1 : files) {
                if(file1.getAbsolutePath().endsWith(".yml")) {
                    YamlGenerator yaml = new YamlGenerator(file1.getPath());
                    final FileConfiguration data = yaml.getData();
                    final Map<String, ItemStack> selling = ConfigUtils.getConfigSectionValue(data.get("selling"), false);

                    if (selling != null && selling.size() > 0)
                        items.addAll((selling.values()));
                }
            }

        }
        sortListings(items);
        return items;
    }

    /**
     * Figures out how to sort the list of items
     * @param listings the items to sort
     */
    private void sortListings(List<ItemStack> listings){

        switch (info.getSorted()){
            case A_Z:
                InventoryActionUtils.sortByAlphabetDisplayName(listings, false);
                break;
            case Z_A:
                InventoryActionUtils.sortByAlphabetDisplayName(listings, true);
                break;
            case OLDEST:
                InventoryActionUtils.sortByTimeStamp(listings, false);
                break;
            case NEWEST:
                InventoryActionUtils.sortByTimeStamp(listings, true);
            case EXPENSIVE:
                InventoryActionUtils.sortByPrice(listings, false);
                break;
            case CHEAPEST:
                InventoryActionUtils.sortByPrice(listings, true);
        }

    }

    @Override
    public Inventory createInventory(){

        try {
            this.invPath = info.getPlugin().path+"Categories/"+info.getCategory()+"/"+info.getViewingMaterial();
            this.info.setSorted(Sorted.NEWEST);
            this.sortedIndex = 0;
            Inventory inv = Bukkit.createInventory(info.getPlayer(), invSize + 9, "Player Listings");
            info.setInventory(inv);

            inv.setItem(inv.getSize() - 9, createItem(Material.BARRIER, ChatColor.RED + "Back", NBTEnums.NBT.METHOD+"~back"));
            inv.setItem(inv.getSize() - 8, createItem(Material.PAPER, "Balance: "+ChatColor.GOLD+"$" + Essentials.getPlugin(Essentials.class).getUser(info.getPlayer()).getMoney()));
            ItemStack item = ItemData.createPlaySkull(info.getPlayer());
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setDisplayName("Your Listings");
            item.setItemMeta(meta);

            inv.setItem(inv.getSize()-6, createItem(Material.COMPASS, "Page Backwards", NBTEnums.NBT.METHOD+"~pageBackwards"));
            inv.setItem(inv.getSize()-5, NBTData.setNBT(item, NBTEnums.NBT.METHOD+"~getPlayerListings"));
            inv.setItem(inv.getSize()-4, createItem(Material.COMPASS, "Page Forward", NBTEnums.NBT.METHOD+"~pageForward"));

            ItemStack sortBy = createItem(Material.DIAMOND, "Sort By:", NBTEnums.NBT.METHOD+"~sortBy");
            ItemMeta sMeta = sortBy.getItemMeta();
            sMeta.setLore(Arrays.asList(ChatColor.GOLD+"Newest", ChatColor.GRAY+"Oldest", ChatColor.GRAY+"A-Z",
                    ChatColor.GRAY+"Z-A", ChatColor.GRAY+"Cheapest", ChatColor.GRAY+"Expensive"));
            sortBy.setItemMeta(sMeta);

            inv.setItem(inv.getSize()-2, sortBy);
            inv.setItem(inv.getSize()-1, createItem(Material.NAME_TAG, "Search By Player Name", NBTEnums.NBT.METHOD+"~searchBy"));

            File matFile = new File(info.getPlugin().path + "Categories/" + info.getCategory() + "/" + info.getViewingMaterial());
            if (matFile.exists()) {

                final List<ItemStack> allListings = getAllListings();
                displayItems(allListings, NBTEnums.NBT.METHOD+"~confirmPurchase");
                return inv;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void clearItems(int howMany){
        for (int i = 0; i < howMany; i++) {
            this.info.getInventory().setItem(i, null);

        }
    }

    /**
     * Places items in their slots
     * @param selling the items being shown
     * @param nbt any nbt to put on each item
     */
    private void displayItems(List<ItemStack> selling, String... nbt){

        short place = 0;
        for(int i = info.getPage()*invSize; i < invSize*(info.getPage()+1); i++){

            if(selling.size() > i && place < invSize) {
                createItemInformation(selling.get(i));
                info.getInventory().setItem(place, NBTData.setNBT(selling.get(i), nbt));
                place++;
            }else break;
        }

    }

    /**
     * Displays item information as lore
     * @param item the item to display
     */
    private void createItemInformation(ItemStack item){

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.add(ChatColor.GRAY+"Price: "+ChatColor.GOLD+"$"+NBTData.getNBT(item, NBT.PRICE.toString()));

        final long time = Long.parseLong(NBTData.getNBT(item, NBT.DATEEXPIRE.toString()));
        final long currentTime = new Date().getTime();
        if(time > currentTime) {
            lore.add(ChatColor.GRAY + "Expires: " + ChatColor.WHITE + DateParser.formatDateDiff(time));//ItemData.formatTime(time));
        }else{
            lore.add(ChatColor.GRAY+"Expires: " + ChatColor.DARK_RED+"EXPIRED");
            item = NBTData.removeNBT(item, NBTEnums.NBT.METHOD.toString());
        }

        OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(NBTData.getNBT(item, NBT.SELLER.toString())));
        lore.add(ChatColor.GRAY+"Seller: "+ChatColor.GOLD+seller.getName());

        if(isOwnItem(info.getPlayer(), item)){
            lore.add(ChatColor.RED+"You cannot purchase this item");
            lore.add("Press Q to delete this listing");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

    }

    public static boolean isOwnItem(Player player, ItemStack item){

        final String nbt = NBTData.getNBT(item, NBT.SELLER.toString());
        if(nbt != null){
            return nbt.equals(player.getUniqueId().toString());
        }
        return false;
    }

    public static ItemStack removeItemInformation(ItemStack item){

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if(lore != null){

            lore = lore.stream().filter(i -> !i.contains(ChatColor.GRAY+"Price: ") && !i.contains(ChatColor.GRAY+"Expires: ") &&
                    !i.contains(ChatColor.GRAY+"Seller: ") && !i.contains("Press Q to delete this listing") && !i.contains(ChatColor.RED+"You cannot purchase this item")).collect(Collectors.toList());
            meta.setLore(lore);
            item.setItemMeta(meta);

        }
        return item;
    }

}
