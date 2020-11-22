package me.nanigans.pandoraauctionhouse.ConfigUtils;

import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import me.nanigans.pandoraauctionhouse.PandoraAuctionHouse;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigCreators {

    public static void createAHConfigFolder(String path) throws IOException {
        PandoraAuctionHouse plugin = PandoraAuctionHouse.getPlugin(PandoraAuctionHouse.class);

        File file = new File(plugin.path+path);
        if(!file.exists()) {
            Path paths = Paths.get(plugin.path+path);
            Files.createDirectories(paths);
        }

    }

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
