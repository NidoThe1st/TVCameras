package com.nido.camera;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.UUID;

public class SkullBuilder {
    private ItemStack itemStack;
    private SkullMeta skullMeta;

    public SkullBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.skullMeta = (SkullMeta) this.itemStack.getItemMeta();
    }

    private void updateItemMeta() {
        this.itemStack.setItemMeta(this.skullMeta);
    }

    public SkullBuilder setDisplayName(String name) {
        this.skullMeta.setDisplayName(name);
        return this;
    }

    public SkullBuilder setLore(String... lines) {
        this.skullMeta.setLore(Arrays.asList(lines));
        return this;
    }
    public SkullBuilder setAmmount(int ammount) {
        this.itemStack.setAmount(ammount);
        return this;
    }

    public SkullBuilder setOwner(String uuid){
        this.skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        return this;
    }

    public ItemStack build() {
        this.updateItemMeta();
        return this.itemStack;
    }

}
