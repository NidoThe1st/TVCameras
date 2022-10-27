package com.nido.camera;

import co.aikar.idb.DbRow;
import io.papermc.paper.entity.LookAnchor;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class CamPlayer {
    private Track editing;
    private Camera plugin = Camera.getInstance();
    private Player p;
    private CamPlayer following;
    private Location selection1;
    private Location selection2;
    private Cam currentCamera;
    private ArrayList<Player> followers = new ArrayList<>();
    private HashMap<Integer, Cam> cameraItems = new HashMap<>();
    private boolean inv = false;
    private ArrayList<Integer> disabledCameras = new ArrayList<>();
    private String cameraState;

    public CamPlayer(Player p) {
        this.p = p;
    }

    public CamPlayer(Player p,ArrayList<Integer> disabledCameras){
        this.p = p;
        this.disabledCameras = disabledCameras;

    }
    public ArrayList<Player> getFollowers() {
        return followers;
    }
    public void addFollower(Player follower) {
        if(!followers.contains(follower)) {
            followers.add(follower);
            CamPlayer camfollower = plugin.getPlayer(follower);
            camfollower.startFollowing(p);
        }
    }
    public void removeFollower(Player p) {
        followers.remove(p);
    }
    public void startFollowing(Player followed) {
        if(following != null) {stopFollowing();}
        CamPlayer camFollowed = plugin.getPlayer(followed);
        camFollowed.addFollower(p);
        following = camFollowed;
    }
    public void stopFollowing() {
        if(following != null) {
            following.removeFollower(this.p);
            following = null;
        }
    }
    public void setSelection(int nr, Location loc) {
        if(nr == 1) {
            selection1 = loc;
        } else {
            selection2 = loc;
        }
    }
    public Location getSelection1() {return selection1;}
    public Location getSelection2() {return selection2;}
    public boolean isEditing() {return editing != null;}
    public void startEditing(Track track) {editing = track;}
    public void stopEditing() {editing = null;}
    public void setCurrentCamera(Cam camera) {
        currentCamera = camera;
    }
    public Cam getCurrentCamera() {
        return currentCamera;
    }
    public void setBestCam(Cam camera) {
        for (Player follower: followers) {
            CamPlayer camPlayer = plugin.getPlayer(follower);
            if(camPlayer.getCurrentCamera() != camera) {
                camera.tpPlayer(follower);
                camPlayer.setCurrentCamera(camera);
                follower.lookAt(p, LookAnchor.EYES, LookAnchor.EYES);
                camPlayer.setCurrentCamera(camera);
            }
        }
    }
    public Track getEditing() {
        return editing;
    }
    public boolean isInv() {
        return inv;
    }

    public void setInv(boolean inventory) {
        this.inv = inventory;
        if(!inventory) {cameraItems.clear();}
    }
    public void setCameraItems(HashMap<Integer, Cam> camItems) {
        this.cameraItems = camItems;
    }

    public HashMap<Integer, Cam> getCameraItems(){
        return cameraItems;
    }

    public boolean isCameraDisabled(Integer id) {
        return disabledCameras.contains(id);
    }

    public void disableCamera(Integer id) {
        if(!isCameraDisabled(id)) {
            disabledCameras.add(id);
        }
    }
    public void enableCamera(Integer id) {
        if(isCameraDisabled(id)) {
            disabledCameras.remove(id);
        }
    }
    public ArrayList<Integer> getDisabledCameras() {
        return disabledCameras;
    }
}
