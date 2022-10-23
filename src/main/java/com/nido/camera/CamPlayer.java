package com.nido.camera;

import io.papermc.paper.entity.LookAnchor;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CamPlayer {
    Track editing;
    Camera plugin = Camera.getInstance();
    Player p;
    CamPlayer following;
    Location selection1;
    Location selection2;
    Cam currentCamera;
    ArrayList<Player> followers;
    public CamPlayer(Player p) {
        this.p = p;
    }
    public ArrayList<Player> getFollowers() {
        return followers;
    }
    public void addFollower(Player p) {
        if(!followers.contains(p)) {
            followers.add(p);
            CamPlayer follower = plugin.getPlayer(p);
            follower.startFollowing(this.p);
        }
    }
    public void removeFollower(Player p) {
        followers.remove(p);
    }
    public void startFollowing(Player p) {
        if(following != null) {stopFollowing();}
        CamPlayer followed = plugin.getPlayer(p);
        followed.addFollower(p);
        following = followed;
    }
    public void stopFollowing() {
        if(following != null) {
            following.removeFollower(p);
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

}
