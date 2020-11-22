package me.nanigans.pandoraauctionhouse.ConfigUtils;

import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public static void giveItemsBackToPlayer(Player player, String filePath, AuctionCategories category){

        YamlGenerator yaml = new YamlGenerator(filePath);
        final FileConfiguration data = yaml.getData();
        final List<ItemStack> selling = (List<ItemStack>) data.getList("selling");
        for (ItemStack itemStack : selling) {
            if (NBTData.containsNBT(itemStack, NBTEnums.NBT.ENCHANTS.toString())) {

                Map<Enchantment, Integer> enchants = ItemData.parseEnchantNBT(NBTData.getNBT(itemStack, NBTEnums.NBT.ENCHANTS.toString()));
                itemStack.addEnchantments(enchants);
                itemStack = NBTData.removeNBT(itemStack, NBTEnums.NBT.ENCHANTS.toString());
            }
            if (category != AuctionCategories.ALL && !player.getInventory().addItem(itemStack).isEmpty())
                player.getWorld().dropItem(player.getLocation(), itemStack);

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
    public static void addItemToPlayer(AuctionCategories category, Player player, ItemStack item){
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);

        try {
            createAHConfigFolder(plugin.path + "/Categories/" + category.toString() + "/" + item.getType());
        }catch(IOException ignored){}
            YamlGenerator yaml = new YamlGenerator(plugin.path+"/Categories/"+category.toString()+"/"+item.getType()+"/"+player.getUniqueId()+".yml");
            final FileConfiguration data = yaml.getData();

            List<ItemStack> soldItems = data.get("selling") == null ? new ArrayList<>() : (List<ItemStack>) data.getList("selling");
            soldItems.add(item);
            data.set("selling", soldItems);
            if(category != AuctionCategories.ALL){
                addItemToPlayer(AuctionCategories.ALL, player, item);
            }

            yaml.save();

    }

    /**
     * Gets all the materials from a category
     * @param category the category need to get the material list
     * @return a list of materials
     */
    public static List<Material> getMaterialsFromCategory(AuctionCategories category){
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);
        List<Material> list = new ArrayList<>();
        File file = new File(plugin.path+"/Categories/"+category);

        for (String s : file.list()) {
            if(new File(file.getPath()+"/"+s).isDirectory())
                list.add(Material.valueOf(s));
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
    public static Map<String, Object> getConfigSectionValue(Object o, boolean deep) {
        if (o == null) {
            return null;
        }
        Map<String, Object> map;
        if (o instanceof ConfigurationSection) {
            map = ((ConfigurationSection) o).getValues(deep);
        } else if (o instanceof Map) {
            map = (Map<String, Object>) o;
        } else {
            return null;
        }
        return map;
    }

}
