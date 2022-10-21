package com.nido.camera;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class Camera extends JavaPlugin {
    HashMap<Player, Cam> currentCamera = new HashMap<>();
    HashMap<Player, ArrayList<Player>> followedPlayers = new HashMap<>();
    HashMap<String, Cam> cameras = new HashMap<>();
    //add a new camera with the name (addedcamera)
    public void addCamera(Cam camera, String addedcamera){

        cameras.put(addedcamera, camera);

    }
    //get camera with the name (camname)
    public Cam getCamera(String camname){

        return cameras.get(camname);

    }
    //check if camera exists with a name (key)
    public boolean isCamera(String key){

        return cameras.containsKey(key);
    }

    public boolean haveCamera(Player key){

        return currentCamera.containsKey(key);
    }
    public void setCurrentCamera(Player p, Cam cam) {
        currentCamera.put(p, cam);
    }
    public Cam getCurrentCamera(Player p) {
        return currentCamera.get(p);
    }
    //get all cameras
    public HashMap<String, Cam> getCameras() {
        return cameras;
    }
    //Add followers to a player (followed)
    public void addFollowed(Player followed, ArrayList<Player> followers){
        //check if there are followers for the player (followed)
        if(followedPlayers.containsKey(followed)) {
            //add new followers and old followers together
            followers.addAll(followedPlayers.get(followed));
        }
        //update the list with the combined followers/new followers
        followedPlayers.put(followed,followers);

    }
    public void removeFollower(Player followed, Player follower) {
        // get current followers
        ArrayList<Player> followers = followedPlayers.get(followed);
        //remove follower if exists
        followers.remove(follower);
        //if no followers left remove it from the hashmap
        if(followers.isEmpty()) {followedPlayers.remove(followed);}
        //sets the new list of followers
        else { followedPlayers.put(followed, followers);}

    }
    //find who is the player (follower) following
    public Player whoFollowed(Player follower) {
        for(Player followed : followedPlayers.keySet()) {
            if(getFollowers(followed).contains(follower)) {
                return followed;
            }
        }
        return null;
    }
    //get the list of followers who are following a player (followed)
    public ArrayList<Player> getFollowers(Player followed){

        return followedPlayers.get(followed);

    }
    //check if a player is being followed
    public boolean isFollowed(Player followed){

        return followedPlayers.containsKey(followed);
    }

    private static Camera instance;

    public static Camera getInstance() {
        return Camera.instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Camera.instance = this;
        getServer().getPluginManager().registerEvents(new cameraListener(), this);
        getCommand("cam").setExecutor(new cameraCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
