package com.nido.camera;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Camera extends JavaPlugin {

    HashMap<String, Cam> cameras = new HashMap<>();
    public void addCamera(Cam camera, String addedcamera){

        cameras.put(addedcamera, camera);

    }

    public Cam getCamera(String camname){

        return cameras.get(camname);

    }

    public boolean isCamera(String key){

        return cameras.containsKey(key);
    }

    private static Camera instance;

    public static Camera getInstance() {
        return Camera.instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Camera.instance = this;

        getCommand("cam").setExecutor(new cameraCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
