package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;
import static me.nanigans.pandoraauctionhouse.InvUtils.MainInventoryActions.itemPlaces;

public class InventoryActionUtils {

    /**
     * Replaces the auction house category with a new one
     * @param info auction house information
     */
    public static void replaceCategory(AuctionHouseInventory info){

        info.setPage(0);
        List<String> materialList = InventoryActionUtils.sortByAlphabetical(
                ConfigUtils.getMaterialsFromCategory(info.getCategory(), false), info.getSorted() == Sorted.Z_A);//item listings by material

        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*materialList.size(); i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, NBTEnums.NBT.METHOD+"~openMaterial"));
            else break;
        }

    }

    /**
     * Sorts a list alphabetically or its inverse
     * @param list the list to sort
     * @param inverse weather to flip the list
     * @param <T> must be able to be a string
     * @return a new sorted list
     */

    public static <T> List<String> sortByAlphabetical(List<T> list, boolean inverse){
        List<String> stringified = list.stream().map(Object::toString).sorted().collect(Collectors.toList());
        if(inverse) Collections.reverse(stringified);
        return stringified;
    }

    /**
     * Sorts by display name
     * Will sort it A-Z without inverse
     * @param list the list to sort
     * @param inverse if to inverse the list afterwards
     */
    public static void sortByAlphabetDisplayName(List<ItemStack> list, boolean inverse){

        list.sort(Comparator.comparing(i -> (i.getItemMeta().getDisplayName() == null ? String.valueOf(Character.MAX_VALUE) : i.getItemMeta().getDisplayName())));
        if(inverse) Collections.reverse(list);
    }

    /**
     * Sorts by timestamp
     * Will sort by highest to lowest timestamp
     * @param list item to sort
     * @param inverse if to inverse the list
     */
    public static void sortByTimeStamp(List<ItemStack> list, boolean inverse){

        list.sort((i, j) -> {
            Long time1 = Long.parseLong(NBTData.getNBT(i, NBTEnums.NBT.DATESOLD.toString()));
            Long time2 = Long.parseLong(NBTData.getNBT(j, NBTEnums.NBT.DATESOLD.toString()));
            return time1.compareTo(time2);
        });
        if(inverse) Collections.reverse(list);

    }

    /**
     * Sorts the listings by price
     * @param list the list to sort
     * @param inverse to inverse the list
     */
    public static void sortByPrice(List<ItemStack> list, boolean inverse){

        list.sort(Comparator.comparingDouble(i -> Double.parseDouble(NBTData.getNBT(i, NBTEnums.NBT.PRICE.toString()))));
        if(!inverse) Collections.reverse(list);

    }

    public static String getItemName(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return nmsStack.getItem().a(nmsStack);
    }

    /**
     * Sets the listing board to what was found in the list
     * @param info auction info
     * @param list the list to put in the auction house
     */
    public static void replaceByItems(AuctionHouseInventory info, List<ExtractedResult> list){

        clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*list.size(); i < list.size()*(info.getPage()+1); i++){
            if(list.size() > i && iterator.hasNext()) {
                if(list.get(i).getString().contains("~")){
                    info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(list.get(i).getString().split("~")[1]), null, NBTEnums.NBT.METHOD + "~openMaterial"));
                }else
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(list.get(i).getString()), null, NBTEnums.NBT.METHOD + "~openMaterial"));
            }else break;
        }

        info.swapInvs(info.getInventory());

    }


    public static ItemStack removeNBTFromItem(ItemStack item){
        for(NBTEnums.NBT nbt : NBTEnums.NBT.values()){
            if(NBTData.containsNBT(item, nbt.toString())){
                item = NBTData.removeNBT(item, nbt.toString());
            }
        }
        for(Sorted nbt : Sorted.values()){
            if(NBTData.containsNBT(item, nbt.toString())){
                item = NBTData.removeNBT(item, nbt.toString());
            }
        }
        net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if(stack.hasTag()){
            final NBTTagCompound tag = stack.getTag();
            if(tag.c().size() <= 0)
                stack.setTag(null);
        }
        item = CraftItemStack.asCraftMirror(stack);

        return item;
    }
    /**
     * This will clera the item board with yellow glass
     * @param info
     */
    public static void clearItemBoard(AuctionHouseInventory info){

        for (int itemPlace : itemPlaces) {
            info.getInventory().setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }

    }

    public static List<String> wordWrapLore(String string, String splitter, ChatColor color) {
        StringBuilder sb = new StringBuilder(color+string);

        int i = 0;
        while (i + 35 < sb.length() && (i = sb.lastIndexOf(splitter, i + 35)) != -1) {
            sb.replace(i, i + 1, "\n"+color);
        }
        return Arrays.asList(sb.toString().split("\n"));

    }

}
