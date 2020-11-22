package me.nanigans.pandoraauctionhouse.ItemUtils;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NBTData {

    public static boolean containsNBT(ItemStack item, String key){

        try {
            net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);

            final List<String> strings = MaterialKeys.Material_Keys.get(Material.STONE);

            if (stack.hasTag()) {
                NBTTagCompound tag = stack.getTag();

                if (tag != null) {

                    return tag.hasKey(key);

                } else return false;
            } else return false;

        }catch(Exception ignored){
            return false;
        }
    }

    public static Object getNBT(ItemStack item, String key){

        if(containsNBT(item, key)){

            net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);

            return stack.getTag().get(key);

        }
        return null;
    }

    /**
     * Sets item nbt data
     * @param item the item to set it to
     * @param keyValuePair a key value pair in the format like KEY~VAULE
     * @return the new itemstack with set nbt
     */
    public static ItemStack setNBT(ItemStack item, String... keyValuePair){

        net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = stack.getTag() != null ? stack.getTag() : new NBTTagCompound();

        for (String s : keyValuePair) {

            String[] nbt = s.split("~");
            tag.setString(nbt[0], nbt[1]);
        }
        stack.setTag((tag));

        item = CraftItemStack.asCraftMirror(stack);

        return item;

    }
}
