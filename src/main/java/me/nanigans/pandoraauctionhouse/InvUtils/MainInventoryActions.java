package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.Title;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;
import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.itemPlaces;

public class MainInventoryActions extends InventoryActions{

    public MainInventoryActions(AuctionHouseInventory info) {
        super(info);
    }

    @Override
    public void click(String method) throws NoSuchMethodException {
        try {
            this.getClass().getMethod(method).invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public void openMaterial(){
        info.setViewingMaterial(info.getLastClicked().getType());
        Inventory inv = InventoryCreation.createListingInventory(info);
        info.swapInvs(inv);
    }

    /**
     * Changes the category topic to the item clicked
     */
    public void categoryChange(){
        InventoryActionUtils.clearItemBoard(info);
        ItemStack item = info.getLastClicked();
        if(item != null){
            String data = NBTData.getNBT(item, "SETCATEGORY");
            if(data != null){
                final AuctionCategories auctionCategories = AuctionCategories.valueOf(data);
                //if(auctionCategories != info.getCategory()) {
                info.setCategory(auctionCategories);
                InventoryActionUtils.replaceCategory(info);
                //}
            }
        }
    }
    /**
     * Moves the category list down
     */
    public void categoryDown(){
        info.setCategoryFirst((byte) (info.getCategoryFirst()+1));
        PrimitiveIterator.OfInt iterator = Arrays.stream(InventoryCreation.categoryPlaces).iterator();
        final Object[] objects = InventoryCreation.itemCategories.values().toArray();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+InventoryCreation.categoryPlaces.length; i++) {//category items
            info.getInventory().setItem(iterator.next(), (ItemStack) objects[i]);
        }

    }

    /**
     * Moves the category list up
     */
    public void categoryUp(){

        info.setCategoryFirst((byte) (info.getCategoryFirst()-1));

        PrimitiveIterator.OfInt iterator = Arrays.stream(InventoryCreation.categoryPlaces).iterator();
        final Object[] objects = InventoryCreation.itemCategories.values().toArray();
        for (int i = info.getCategoryFirst(); i < info.getCategoryFirst()+InventoryCreation.categoryPlaces.length; i++) {//category items
            info.getInventory().setItem(iterator.next(), (ItemStack) objects[i]);
        }
    }


    /**
     * Shows all of the listings by the player
     */
    @Override
    public void getPlayerListings() {

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
    public void pageForward() {

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
    public void pageBackwards() {

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
    public void sortBy() {

        if(info.getSorted() == Sorted.A_Z) {
            info.setSorted(Sorted.Z_A);
        }
        else info.setSorted(Sorted.A_Z);
        ItemStack filters = createItem(Material.DIAMOND, "Sort By:", NBTEnums.NBT.SORTBY+"~"+ Sorted.A_Z, "METHOD~sortBy");

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
    public void searchBy() {

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
}
