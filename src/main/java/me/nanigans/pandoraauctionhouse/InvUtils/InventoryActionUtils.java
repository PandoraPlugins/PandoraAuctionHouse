package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.Sorted;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
                ConfigUtils.getMaterialsFromCategory(info.getCategory()), info.getSorted() == Sorted.Z_A);//item listings by material

        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*materialList.size(); i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(materialList.get(i)), null, "METHOD~openMaterial"));
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
     * Sets the listing board to what was found in the list
     * @param info auction info
     * @param list the list to put in the auction house
     */
    public static void replaceByItems(AuctionHouseInventory info, List<ExtractedResult> list){

        clearItemBoard(info);
        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*list.size(); i < list.size()*(info.getPage()+1); i++){
            if(list.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(Material.valueOf(list.get(i).getString()), null, "METHOD~openMaterial"));
            else break;
        }

        info.swapInvs(info.getInventory());

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

}
