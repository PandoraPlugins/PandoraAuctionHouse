package me.nanigans.pandoraauctionhouse.InvUtils;

import com.earth2me.essentials.Essentials;
import me.nanigans.pandoraauctionhouse.AuctionHouseInventory;
import me.nanigans.pandoraauctionhouse.Classifications.InventoryType;
import me.nanigans.pandoraauctionhouse.Classifications.NBTEnums.NBT;
import me.nanigans.pandoraauctionhouse.ConfigUtils.YamlGenerator;
import me.nanigans.pandoraauctionhouse.ItemUtils.ItemData;
import me.nanigans.pandoraauctionhouse.ItemUtils.NBTData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.nanigans.pandoraauctionhouse.InvUtils.InventoryCreation.createItem;

@FunctionalInterface
interface ListingMethods {
    void execute();
}

public class ListingInventoryActions extends InventoryActions{
    private final HashMap<String, ListingMethods> listingsMethods = new HashMap<>();
    private String invPath;

    public ListingInventoryActions(AuctionHouseInventory info) {
        super(info);
        listingsMethods.put("confirmPurchase", this::confirmPurchase);
        listingsMethods.put("back", this::back);

    }

    protected void confirmPurchase(){



    }

    @Override
    public void click(String method) {
        if(methods.containsKey(method))
            methods.get(method).execute();
        else if(listingsMethods.containsKey(method))
            listingsMethods.get(method).execute();
    }


    @Override
    protected void getPlayerListings() {

        File file = new File(this.invPath+"/"+info.getPlayer().getUniqueId());
        if(file.exists()){

            YamlGenerator yaml = new YamlGenerator(file.getAbsolutePath());
            final FileConfiguration data = yaml.getData();
            final List<ItemStack> selling = (List<ItemStack>) data.getList("selling");
            clearItems(45);
            displayItems(selling, 45);

        }

    }

    @Override
    protected void pageForward() {



    }

    @Override
    protected void pageBackwards() {

    }

    @Override
    protected void sortBy() {

    }

    @Override
    protected void searchBy() {

    }

    protected void back(){
        Inventory inv = info.getMainInventory().createInventory();
        info.swapInvs(inv);
        info.setInvType(InventoryType.MAIN);
    }

    @Override
    public Inventory createInventory(){

        try {
            this.invPath = info.getPlugin().path+"Categories/"+info.getCategory()+"/"+info.getViewingMaterial();
            int invListingSize = 45;
            Inventory inv = Bukkit.createInventory(info.getPlayer(), invListingSize + 9, "Player Listings");
            info.setInventory(inv);

            inv.setItem(inv.getSize() - 9, createItem(Material.BARRIER, ChatColor.RED + "Back", "METHOD~back"));
            inv.setItem(inv.getSize() - 8, createItem(Material.PAPER, "Balance: "+ChatColor.GOLD+"$" + Essentials.getPlugin(Essentials.class).getUser(info.getPlayer()).getMoney()));
            ItemStack item = ItemData.createPlaySkull(info.getPlayer());
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setDisplayName("Your Listings");
            item.setItemMeta(meta);
            inv.setItem(inv.getSize()-5, NBTData.setNBT(item, "METHOD~getPlayerListings"));
            inv.setItem(inv.getSize()-6, createItem(Material.COMPASS, "Page Backwards", "METHOD~pageBackwards"));
            inv.setItem(inv.getSize()-4, createItem(Material.COMPASS, "Page Forward", "METHOD~pageForward"));
            ItemStack sortBy = createItem(Material.DIAMOND, "Sort By:", "METHOD~sortBy");
            ItemMeta sMeta = sortBy.getItemMeta();
            sMeta.setLore(Arrays.asList(ChatColor.GOLD+"Newest", ChatColor.GRAY+"Oldest", ChatColor.GRAY+"A-Z",
                    ChatColor.GRAY+"Z-A", ChatColor.GRAY+"Cheapest", ChatColor.GRAY+"Expensive"));
            sortBy.setItemMeta(meta);

            inv.setItem(inv.getSize()-2, sortBy);
            inv.setItem(inv.getSize()-1, createItem(Material.NAME_TAG, "Search by player", "METHOD~searchBy"));

            File matFile = new File(info.getPlugin().path + "Categories/" + info.getCategory() + "/" + info.getViewingMaterial());
            if (matFile.exists()) {

                final File[] files = matFile.listFiles();
                for (File file : files) {
                    if(file.getAbsolutePath().endsWith(".yml")) {

                        YamlGenerator yaml = new YamlGenerator(file.getAbsolutePath());
                        final FileConfiguration data = yaml.getData();
                        final List<ItemStack> selling = (List<ItemStack>) data.getList("selling");
                        System.out.println("selling = " + selling);
                        displayItems(selling, invListingSize, "METHOD~confirmPurchase");

                    }
                }
                return inv;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void clearItems(int howMany){
        for (int i = 0; i < howMany; i++) {
            this.info.getInventory().setItem(i, null);

        }
    }

    public void displayItems(List<ItemStack> selling, int size, String... nbt){
        short invPlace = 0;
        for (ItemStack itemStack : selling) {
            if (invPlace < size) {
                String enchantData = NBTData.getNBT(itemStack, NBT.ENCHANTS.toString());
                if (enchantData != null) {
                    Map<Enchantment, Integer> enchants = ItemData.parseEnchantNBT(enchantData);
                    itemStack.addEnchantments(enchants);
                }

                createItemInformation(itemStack);

                info.getInventory().setItem(invPlace, NBTData.setNBT(itemStack, nbt));
                invPlace++;
            } else break;

        }

    }

    public void createItemInformation(ItemStack item){

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.add(ChatColor.GRAY+"Price: "+ChatColor.GOLD+"$"+NBTData.getNBT(item, NBT.PRICE.toString()));

        final long time = Long.parseLong(NBTData.getNBT(item, NBT.DATEEXPIRE.toString()));
        final long currentTime = new Date().getTime();
        if(time > currentTime) {
            final long days = TimeUnit.DAYS.convert(time - currentTime, TimeUnit.MILLISECONDS);
            final long hours = TimeUnit.HOURS.convert((time - currentTime)%86400000, TimeUnit.MILLISECONDS);
            final long minutes = (time-currentTime)/(60 * 1000) % 60;
            lore.add(ChatColor.GRAY + "Expires: " + ChatColor.WHITE + days+"D " + hours+"H "+minutes+"M");//ItemData.formatTime(time));
        }else lore.add(ChatColor.GRAY+"Expires: " + ChatColor.DARK_RED+"EXPIRED");

        OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(NBTData.getNBT(item, NBT.SELLER.toString())));
        lore.add(ChatColor.GRAY+"Seller: "+ChatColor.GOLD+seller.getName());

        if(NBTData.getNBT(item, NBT.SELLER.toString()).equals(info.getPlayer().getUniqueId().toString())){
            lore.add("Press Q to delete this listing");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

    }

}
