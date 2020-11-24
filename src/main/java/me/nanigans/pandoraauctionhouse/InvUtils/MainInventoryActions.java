package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.Title;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;

@FunctionalInterface
interface MainMethods {
    void execute();
}

public class MainInventoryActions extends InventoryActions{

    private final HashMap<String, MainMethods> mainMethods = new HashMap<>();
    public static final LinkedHashMap<AuctionCategories, ItemStack> itemCategories = new LinkedHashMap<AuctionCategories, ItemStack>(){{
        ItemStack item = createItem(Material.NETHER_STAR, "All", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.ALL);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList("Press Q on any category", "to remove all your listings", "under it"));
        item.setItemMeta(meta);
        put(AuctionCategories.ALL, item);
        put(AuctionCategories.BUILDINGBLOCKS, createItem(Material.BRICK, "Building Blocks", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.BUILDINGBLOCKS));
        put(AuctionCategories.DECORATIONS, createItem("175/5", "Decorations", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.DECORATIONS));
        put(AuctionCategories.REDSTONE, createItem(Material.REDSTONE, "Redstone", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.REDSTONE));
        put(AuctionCategories.TRANSPORTATION, createItem(Material.POWERED_RAIL, "Transportation", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.TRANSPORTATION));
        put(AuctionCategories.FOOD, createItem(Material.APPLE, "Food", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.FOOD));
        put(AuctionCategories.TOOLS, createItem(Material.IRON_AXE, "Tools", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.TOOLS));
        put(AuctionCategories.COMBAT, createItem(Material.GOLD_SWORD, "Combat", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.COMBAT));
        put(AuctionCategories.BREWING, createItem(Material.POTION, "Brewing", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.BREWING));
        put(AuctionCategories.MATERIALS, createItem(Material.STICK, "Materials", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.MATERIALS));
        put(AuctionCategories.MISC, createItem(Material.LAVA_BUCKET, "Miscellaneous", "METHOD~categoryChange", "SETCATEGORY~"+AuctionCategories.MISC));

    }};
    public static final int[] itemPlaces = {11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41};
    public static final int[] categoryPlaces = {9, 18, 27, 36};


    public MainInventoryActions(AuctionHouseInventory info) {
        super(info);
        mainMethods.put("openMaterial", this::openMaterial);
        mainMethods.put("categoryChange", this::categoryChange);
        mainMethods.put("categoryDown", this::categoryDown);
        mainMethods.put("categoryUp", this::categoryUp);
    }

    @Override
    public void click(String method) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(methods.containsKey(method))
                    methods.get(method).execute();
                else if(mainMethods.containsKey(method))
                    mainMethods.get(method).execute();
            }
        }.runTask(info.getPlugin());
    }

    private void openMaterial(){
        new BukkitRunnable() {
            @Override
            public void run() {
                info.setViewingMaterial(info.getLastClicked().getType());
                Inventory inv = info.getListingInventory().createInventory();
                info.swapInvs(inv);
                info.setInvType(InventoryType.LISTINGS);
            }
        }.runTask(info.getPlugin());
    }

    /**
     * Changes the category topic to the item clicked
     */
    private void categoryChange(){
        InventoryActionUtils.clearItemBoard(info);
        ItemStack item = info.getLastClicked();
        if(item != null){
            String data = NBTData.getNBT(item, "SETCATEGORY");
            if(data != null){
                final AuctionCategories auctionCategories = AuctionCategories.valueOf(data);
                info.setCategory(auctionCategories);
                InventoryActionUtils.replaceCategory(info);
            }
        }
    }
    /**
     * Moves the category list down
     */
    private void categoryDown(){
        info.setCategoryFirst((byte) (info.getCategoryFirst()+1));
        PrimitiveIterator.OfInt iterator = Arrays.stream(categoryPlaces).iterator();
        final Object[] objects = itemCategories.values().toArray();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+categoryPlaces.length; i++) {//category items
            info.getInventory().setItem(iterator.next(), (ItemStack) objects[i]);
        }

    }

    /**
     * Moves the category list up
     */
    private void categoryUp(){

        info.setCategoryFirst((byte) (info.getCategoryFirst()-1));

        PrimitiveIterator.OfInt iterator = Arrays.stream(categoryPlaces).iterator();
        final Object[] objects = itemCategories.values().toArray();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+categoryPlaces.length; i++) {//category items
            info.getInventory().setItem(iterator.next(), (ItemStack) objects[i]);
        }
    }


    /**
     * Shows all of the listings by the player
     */
    @Override
    protected void getPlayerListings() {

        File file = new File(info.getPlugin().path+"/Categories/"+info.getCategory());
        List<Material> playerLists = new ArrayList<>();
        for (File listFile : file.listFiles()) {
            if(listFile.isDirectory()){

                final String[] list = listFile.list();
                if (Arrays.stream(list).anyMatch(i -> i.contains(info.getPlayer().getUniqueId().toString()))) {
                    playerLists.add(Material.valueOf(listFile.getName()));
                }

            }
        }

        InventoryActionUtils.clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*playerLists.size(); i < playerLists.size()*(info.getPage()+1); i++){
            if(playerLists.size() > i && iterator.hasNext()) {
                ItemStack item = createItem(playerLists.get(i), null, "METHOD~openMaterial");
                ItemMeta meta = item.getItemMeta();
                meta.setLore(Arrays.asList("Press Q to delete all your", "listings under this item"));
                item.setItemMeta(meta);
                info.getInventory().setItem(iterator.next(), item);
            }
            else break;
        }

    }
    /**
     * Moves the page forward by one in the main AH inventory
     */
    @Override
    protected void pageForward() {

        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material

        info.setPage((int) Math.min(Math.floor((double)materialList.size()/itemPlaces.length), info.getPage()+1));

        InventoryActionUtils.clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*itemPlaces.length; i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }

    }

    /**
     * Moves the page backwards one in the main AH inventory
     */
    @Override
    protected void pageBackwards() {

        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material
        info.setPage(info.getPage()-1);

        InventoryActionUtils.clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*itemPlaces.length; i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }

    }
    /**
     * Changes the sort method between A-Z and Z-A
     */
    @Override
    protected void sortBy() {

        if(info.getSorted() == Sorted.A_Z) {
            info.setSorted(Sorted.Z_A);
        }
        else info.setSorted(Sorted.A_Z);
        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBT.SORTBY+"~"+ Sorted.A_Z, "METHOD~sortBy");

        ItemMeta itemMeta = filters.getItemMeta();
        itemMeta.setLore(Arrays.asList((info.getSorted() == Sorted.A_Z ? ChatColor.GOLD : ChatColor.GRAY)+"A-Z",
                (info.getSorted() == Sorted.Z_A ? ChatColor.GOLD : ChatColor.GRAY)+"Z-A"));
        filters.setItemMeta(itemMeta);
        info.getInventory().setItem(25, filters);
        InventoryActionUtils.replaceCategory(info);

    }
    /**
     * Allows the player to seach for an item by its name
     */
    @Override
    protected void searchBy() {

        info.setSwappingInvs(true);
        info.getPlayer().closeInventory();

        new Title().send(info.getPlayer(), ChatColor.GOLD+"Enter an item name", ChatColor.WHITE+"20 seconds",
                10, 100, 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis()+10000;
                info.setTyping(true);
                while(System.currentTimeMillis() < time){

                    if(info.getMessage() != null){

                        String message = info.getMessage();//TODO: possibly figure out how to get it by the actual item name and not material
                        final List<ExtractedResult> result = FuzzySearch.extractAll(message, ConfigUtils.getMaterialsFromCategory(info.getCategory())
                                .stream().map(Enum::toString).collect(Collectors.toList())).stream().filter(i -> i.getScore() > 70).collect(Collectors.toList());
                        info.setMessage(null);
                        info.setTyping(false);

                        if(result.size() > 0){

                            InventoryActionUtils.replaceByItems(info, result);

                        }else{
                            info.getPlayer().sendMessage(ChatColor.RED+"Couldn't find any listed items in the current category matching your query");
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    info.getPlayer().openInventory(info.getInventory());
                                }
                            }.runTaskLaterAsynchronously(info.getPlugin(), 40);
                        }

                        return;
                    }

                }
                info.setMessage(null);
                info.setTyping(false);
                info.getPlayer().openInventory(info.getInventory());

            }
        }.runTaskAsynchronously(info.getPlugin());

    }


    /**
     * Creates the home auction house page
     * @return a new auction house inventory
     */
    @Override
    public Inventory createInventory(){
        info.setSorted(Sorted.A_Z);
        Inventory inventory = Bukkit.createInventory(info.getPlayer(), 54, "Auction House");
        inventory.setItem(0, createItem("160/14", "Up", "METHOD~categoryUp"));//
        inventory.setItem(45, createItem("160/5", "Down", "METHOD~categoryDown"));//

        inventory.setItem(47, createItem("160/14", "Back", "METHOD~pageBackwards"));//
        inventory.setItem(50, createItem("160/5", "Forward", "METHOD~pageForward"));//
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        meta.setOwner(info.getPlayer().getName());
        meta.setDisplayName("Your listings");
        head.setItemMeta(meta);
        inventory.setItem(16, NBTData.setNBT(head, "METHOD~getPlayerListings"));

        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBT.SORTBY+"~"+Sorted.A_Z, "METHOD~sortBy");

        ItemMeta itemMeta = filters.getItemMeta();
        itemMeta.setLore(Arrays.asList((info.getSorted() == Sorted.A_Z ? ChatColor.GOLD : ChatColor.GRAY)+"A-Z",
                (info.getSorted() == Sorted.Z_A ? ChatColor.GOLD : ChatColor.GRAY)+"Z-A"));
        filters.setItemMeta(itemMeta);
        inventory.setItem(25, filters);

        inventory.setItem(26, createItem(Material.NAME_TAG, "Search By Item Material", "METHOD~searchBy"));
        inventory.setItem(34, createItem(Material.BOOKSHELF, ChatColor.AQUA+"Auction Information"));
        inventory.setItem(53, createItem(Material.PAPER, "Balance: "+
                ChatColor.GREEN+"$" + Essentials.getPlugin(Essentials.class).getUser(info.getPlayer()).getMoney()));

        PrimitiveIterator.OfInt iterator = Arrays.stream(categoryPlaces).iterator();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+4; i++) {//category items
            inventory.setItem(iterator.next(), (ItemStack) itemCategories.values().toArray()[i]);
        }

        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material

        iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*materialList.size(); i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                inventory.setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
            else break;
        }

        for (int itemPlace : itemPlaces) {//empty slots
            if(inventory.getItem(itemPlace) == null)
                inventory.setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }
        for (int i = 0; i < inventory.getContents().length; i++) {//black border
            if(inventory.getContents()[i] == null)
                inventory.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15));
        }

        return inventory;
    }



}
