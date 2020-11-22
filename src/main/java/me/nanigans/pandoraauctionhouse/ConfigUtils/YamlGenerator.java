package me.nanigans.pandoraauctionhouse.ConfigUtils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class YamlGenerator {
    private FileConfiguration data;
    private File messagesFile;

    private void load() {
        if (!this.messagesFile.exists()) {
            try {
                this.messagesFile.createNewFile();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        this.data = YamlConfiguration.loadConfiguration(this.messagesFile);
    }

    public YamlGenerator(String path) {
        this.messagesFile = new File(path);
        load();
    }

    public FileConfiguration getData() {
        return this.data;
    }

    public void save() {
        try {
            this.data.save(this.messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadData() {
        this.data = YamlConfiguration.loadConfiguration(this.messagesFile);
    }
}