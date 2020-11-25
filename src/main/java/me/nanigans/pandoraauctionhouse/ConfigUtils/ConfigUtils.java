package me.nanigans.pandoraauctionhouse.ConfigUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
import me.nanigans.pandoraauctionhouse.InvUtils.InventoryActionUtils;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import me.nanigans.pandoraauctionhouse.PandoraAuctionHouse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigUtils {

    /**
     * Allows the player to remove all listings under a single category
     * @param info auction house info
     * @param category category to remove from
     * @param mat the material to remove
     */
    public static void removePlayerListing(AuctionHouseInventory info, AuctionCategories category, Material mat){

        File file = new File(info.getPlugin().path+"/Categories/"+category+"/"+mat);
        if(file.exists()){
            final File[] files = file.listFiles();
            if(Arrays.stream(files).anyMatch(i -> i.getName().contains(info.getPlayer().getUniqueId().toString()))){

                final File collect = Arrays.stream(files).filter(i -> i.getName().contains(info.getPlayer().getUniqueId().toString())).collect(Collectors.toList()).get(0);
                if(collect != null){

                    giveItemsBackToPlayer(info.getPlayer(), collect.getAbsolutePath(), category);
                    if(category != AuctionCategories.ALL)
                    info.getPlayer().sendMessage(ChatColor.GREEN+"Successfully removed the listing!");
                    collect.delete();
                    if(file.list() != null && file.list().length == 0)
                        file.delete();
                }

            }else if(category != AuctionCategories.ALL){
                info.getPlayer().closeInventory();
                info.getPlayer().sendMessage(ChatColor.RED+"You are not selling anything in this category");
            }

        }else if(category != AuctionCategories.ALL){
            info.getPlayer().closeInventory();
            info.getPlayer().sendMessage(ChatColor.RED+"Nothing is listed in this category");//this shouldn't really happen
        }

    }

    /**
     * This removes any items the player is selling from a given category
     * @param info auction house info
     * @param category the category to delete
     * @return a message to the player if a listing was found
     */
    public static String removePlayerListing(AuctionHouseInventory info, AuctionCategories category){

        File file = new File(info.getPlugin().path+"/Categories/"+category);
        final File[] materialFiles = file.listFiles();
        if(materialFiles != null){
        for (File materialFile : materialFiles) {//material folders
            if(materialFile.isDirectory()) {
                if (Arrays.stream(materialFile.list()).anyMatch(i -> i.contains(info.getPlayer().getUniqueId().toString()))) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            final List<File> collect = Arrays.stream(materialFile.listFiles())
                                    .filter(i -> i.getName().contains(info.getPlayer().getUniqueId().toString())).collect(Collectors.toList());
                            for (File value : collect) {

                                giveItemsBackToPlayer(info.getPlayer(), value.getAbsolutePath(), category);

                                value.delete();
                                if (materialFile.list() != null && materialFile.list().length == 0)
                                    materialFile.delete();
                            }

                        }
                    }.runTaskAsynchronously(info.getPlugin());
                }else{
                    info.getPlayer().closeInventory();
                    return ChatColor.RED+"You are not selling anything in this category";
                }
            }

            }
        }else{
            info.getPlayer().closeInventory();
            return ChatColor.RED+"Couldn't find any materials in this category";
        }
        return null;

    }

    /**
     * Gets the items back from the listing they removed
     * @param player the player to give to
     * @param filePath the path of the yml file
     * @param category the category to the yml file
     */
    public static void giveItemsBackToPlayer(Player player, String filePath, AuctionCategories category, String... itemUUID){

        YamlGenerator yaml = new YamlGenerator(filePath);
        final FileConfiguration data = yaml.getData();
        final Map<String, ItemStack> selling = getConfigSectionValue(data.get("selling"), false);

        if(itemUUID.length > 0) {

            for (String s : itemUUID) {
                ItemStack item = selling.get(s).clone();
                if (NBTData.containsNBT(item, NBTEnums.NBT.ENCHANTS.toString())) {

                    Map<Enchantment, Integer> enchants = ItemData.parseEnchantNBT(NBTData.getNBT(item, NBTEnums.NBT.ENCHANTS.toString()));
                    item.addEnchantments(enchants);

                }
                item = InventoryActionUtils.removeNBTFromItem(item);
                item = NBTData.removeNBT(item, "UUID");

                if (!player.getInventory().addItem(item).isEmpty())
                    player.getWorld().dropItem(player.getLocation(), item);

                selling.remove(s);
            }

        }else{

            for (Map.Entry<String, ItemStack> entry : selling.entrySet()) {

                ItemStack item = entry.getValue().clone();
                if (NBTData.containsNBT(item, NBTEnums.NBT.ENCHANTS.toString())) {

                    Map<Enchantment, Integer> enchants = ItemData.parseEnchantNBT(NBTData.getNBT(item, NBTEnums.NBT.ENCHANTS.toString()));
                    item.addEnchantments(enchants);

                }
                item = InventoryActionUtils.removeNBTFromItem(item);
                item = NBTData.removeNBT(item, "UUID");

                if (category != AuctionCategories.ALL && !player.getInventory().addItem(item).isEmpty())
                    player.getWorld().dropItem(player.getLocation(), item);

                selling.remove(entry.getKey());
            }

        }

    }

    /**
     * Creates a new directory with respect to the path
     * @param path the path to make the directory
     * @throws IOException error for when it fails
     */
    public static void createAHConfigFolder(String path) throws IOException {
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);

        File file = new File(plugin.path+path);
        if(!file.exists()) {
            Path paths = Paths.get(plugin.path+path);
            Files.createDirectories(paths);
        }

    }

    /**
     * Adds a new listing to the players desired category. This'll add it to the category and the ALL category
     * @param category the category to add to
     * @param player the player adding the listing
     * @param item the item to add to the listing
     */
    public static void addItemToPlayer(AuctionCategories category, Player player, ItemStack item, UUID uuid){
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);

        try {
            createAHConfigFolder(plugin.path + "/Categories/" + category.toString() + "/" + item.getType());
        }catch(IOException ignored){}

            YamlGenerator yaml = new YamlGenerator(plugin.path+"/Categories/"+category.toString()+"/"+item.getType()+"/"+player.getUniqueId()+".yml");
            final FileConfiguration data = yaml.getData();
            Map<String, ItemStack> soldItems = data.get("selling") == null ? new HashMap<>() : getConfigSectionValue(data.get("selling"), false);
            soldItems.put(uuid.toString(), item);
            data.set("selling", soldItems);
            if(category != AuctionCategories.ALL){
                addItemToPlayer(AuctionCategories.ALL, player, item, uuid);
            }

            yaml.save();

    }

    public static boolean removeItemFromPlayerListing(String itemUUID, String playerUUID, AuctionCategories category, Material material){
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        File file = new File(plugin.path+"Categories/"+category+"/"+material+"/"+playerUUID+".yml");

        if(file.exists()) {
            YamlGenerator yaml = new YamlGenerator(file.getAbsolutePath());
            if (yaml.getData() != null) {

                final FileConfiguration data = yaml.getData();
                final Map<String, ItemStack> selling = getConfigSectionValue(data.get("selling"), false);
                selling.remove(itemUUID);
                data.set("selling", selling);
                yaml.save();

                if (selling.size() < 1) {
                    file.delete();
                    if (file.getParentFile().listFiles().length == 0) {
                        file.getParentFile().delete();
                    }
                }
                return true;

            }else return false;
        }else{
            return false;
        }
    }

    /**
     * Gets all the materials from a category
     * @param category the category need to get the material list
     * @param asItemName
     * @return a list of materials
     */
    public static List<String> getMaterialsFromCategory(AuctionCategories category, boolean asItemName){
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        List<String> list = new ArrayList<>();
        File file = new File(plugin.path+"/Categories/"+category);

        for (String s : file.list()) {
            if(new File(file.getPath()+"/"+s).isDirectory()) {
                if(!asItemName)
                list.add(Material.valueOf(s).toString());
                else{
                    list.add(InventoryActionUtils.getItemName(new ItemStack(Material.valueOf(s)))+"~"+s);
                }
            }
        }
        return list;

    }

    /**
     * Returns a {@link Map} representative of the passed Object that represents
     * a section of a YAML file. This method neglects the implementation of the
     * section (whether it be {@link ConfigurationSection} or just a
     * {@link Map}), and returns the appropriate value.
     *
     * @since 0.1.0
     * @version 0.1.0
     *
     * @param o The object to interpret
     * @param deep If an object is a {@link ConfigurationSection}, {@code true} to do a deep search
     * @return A {@link Map} representing the section
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getConfigSectionValue(Object o, boolean deep) {
        if (o == null) {
            return null;
        }
        Map<String, T> map;
        if (o instanceof ConfigurationSection) {
            map = (Map<String, T>) ((ConfigurationSection) o).getValues(deep);
        } else if (o instanceof Map) {
            map = (Map<String, T>) o;
        } else {
            return null;
        }
        return map;
    }

}
