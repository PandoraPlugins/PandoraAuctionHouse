package me.nanigans.pandoraauctionhouse.InvUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigUtils;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;
import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.itemPlaces;

public class InventoryActionUtils {

    /**
     * Replaces the auction house category with a new one
     * @param info auction house information
     */
    public static void replaceCategory(AuctionHouseInventory info){

        for (int itemPlace : itemPlaces) {
            info.getInventory().setItem(itemPlace, createItem("160/4", "Empty Slot"));
        }
        List<Material> materialList = ConfigUtils.getMaterialsFromCategory(info.getCategory());//item listings by material

        Iterator<Integer> iterator = Arrays.stream(itemPlaces).iterator();
        for(int i = info.getPage()*materialList.size(); i < materialList.size()*(info.getPage()+1); i++){
            if(materialList.size() > i && iterator.hasNext())
                info.getInventory().setItem(iterator.next(), createItem(materialList.get(i), null, "METHOD~openMaterial"));
            else break;
        }

    }

}
