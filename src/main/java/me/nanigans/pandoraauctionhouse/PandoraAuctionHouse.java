package me.nanigans.pandoraauctionhouse;

import me.nanigans.pandoraauctionhouse.Commands.AuctionHouse;
import me.nanigans.pandoraauctionhouse.ConfigUtils.ConfigCreators;
import me.nanigans.pandoraauctionhouse.Classifications.AuctionCategories;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class PandoraAuctionHouse extends JavaPlugin implements Listener {
    public final String path = getDataFolder().getPath()+"/AuctionHouse/";

    @Override
    public void onEnable() {

        getCommand("auctionhouse").setExecutor(new AuctionHouse());
        try {
            for(AuctionCategories category : AuctionCategories.values()){
                ConfigCreators.createAHConfigFolder("Categories/"+ category);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
