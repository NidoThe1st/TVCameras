package com.nido.camera;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Camera extends JavaPlugin {
    public FileConfiguration config = getConfig();
    HashMap<Player, CamPlayer> cameraPlayers = new HashMap<>();
    List<Cam> cameras = new ArrayList<>();
    //add a new camera with the name (addedcamera)
    public void addCamera(Cam camera){
        if(getCamera(camera.getIndex()) == null) {
            cameras.add(camera);
        } else {
            cameras.remove(getCamera(camera.getIndex()));
            cameras.add(camera);
        }

    }
    //get camera with for the track by the index
    public Cam getCamera(int index){
        for (Cam camera: cameras) {
            if(camera.getIndex() == index) {return camera;}
        }
        return null;
    }

    //get all cameras
    public List<Cam> getCameras() {
        return cameras;
    }

    private static Camera instance;

    public static Camera getInstance() {
        return Camera.instance;
    }

    public void onQuit(Player player){
        //removes player from hashmaps
        CamPlayer camPlayer = getPlayer(player);
        camPlayer.stopFollowing();
        List<Player> followers = new ArrayList<>(camPlayer.getFollowers());
        if(!followers.isEmpty()) {
            for (Player follower : followers) {
                getPlayer(follower).stopFollowing();
            }
        }
        cameraPlayers.remove(player);
    }

    public void saveNewCamera(Cam tempcamera) {

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Camera.instance = this;
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new cameraListener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        // enable brigadier integration for paper servers
        manager.enableUnstableAPI("brigadier");
        manager.registerCommand(new newCamCommand());
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean removeCamera(int regionIndex) {
        if (getCamera(regionIndex) != null) {
            Cam camera = getCamera(regionIndex);
            cameras.remove(camera);
            assert camera != null;
            return true;
        }
        return false;
    }
    public CamPlayer getPlayer(Player p) {
        return cameraPlayers.get(p);
    }
    public void newCamPlayer(Player p) {
        if(!cameraPlayers.containsKey(p)) {
            cameraPlayers.put(p, new CamPlayer(p));
        }
    }
}
