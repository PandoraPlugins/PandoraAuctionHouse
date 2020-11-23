package me.nanigans.pandoraauctionhouse.Classifications;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.CreativeModeTab;
import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class ItemType {

    public static AuctionCategories getItemCategory(ItemStack item){

        String name = "misc";
        try {
            if (item.getType().isBlock()) {
                CreativeModeTab tab = getBlockTab(Block.class, "creativeTab", CraftMagicNumbers.getBlock(item.getType()));
                name = getField(CreativeModeTab.class, "o", tab);
            } else {
                CreativeModeTab tab = getField(Item.class, "b", CraftMagicNumbers.getItem(item.getType()));
                name = getField(CreativeModeTab.class, "o", tab);
            }
        }catch(Exception ignored){}

        if(name == null) return AuctionCategories.MISC;

        return AuctionCategories.valueOf(name.toUpperCase());
    }

    private static CreativeModeTab getBlockTab(Class<Block> itemClass, String b, Block block) {

        try {
            // get the field that this class declares with name "fieldName" - throws NoSuchFieldException if not found
            final Field field = itemClass.getDeclaredField(b);
            // set it accessible, ignoring private or protected access level
            field.setAccessible(true);
            // return the field-value for a given object - throws IllegalAccessException if this is not possible
            // we cast this to T to be able to work with generics
            // this cast should never fail since we are accessing the object of type T
            return (CreativeModeTab) field.get(block);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    private static CreativeModeTab getField(final @NotNull Class<Item> clazz, final @NotNull String fieldName, @Nullable final Item object) {
        try {
            // get the field that this class declares with name "fieldName" - throws NoSuchFieldException if not found
            final Field field = clazz.getDeclaredField(fieldName);
            // set it accessible, ignoring private or protected access level
            field.setAccessible(true);
            // return the field-value for a given object - throws IllegalAccessException if this is not possible
            // we cast this to T to be able to work with generics
            // this cast should never fail since we are accessing the object of type T
            return (CreativeModeTab) field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String getField(final @NotNull Class<CreativeModeTab> clazz, final @NotNull String fieldName, @Nullable final CreativeModeTab object) {
        try {
            // get the field that this class declares with name "fieldName" - throws NoSuchFieldException if not found
            final Field field = clazz.getDeclaredField(fieldName);
            // set it accessible, ignoring private or protected access level
            field.setAccessible(true);
            // return the field-value for a given object - throws IllegalAccessException if this is not possible
            // we cast this to T to be able to work with generics
            // this cast should never fail since we are accessing the object of type T
            return field.get(object).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
