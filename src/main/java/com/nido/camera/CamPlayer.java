package com.nido.camera;

import io.papermc.paper.entity.LookAnchor;
import lombok.Getter;
import lombok.Setter;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.entity.Player;

import java.util.*;

public class CamPlayer {
    @Getter
    private Track editing;
    private CameraPlugin plugin = CameraPlugin.getInstance();
    @Getter
    private Player p;
    private CamPlayer following;
    @Getter
    @Setter
    private Camera currentCamera;
    @Getter
    private List<Player> followers = new ArrayList<>();
    @Getter
    @Setter
    private HashMap<Integer, Camera> cameraItems = new HashMap<>();
    @Getter
    private boolean inv = false;
    @Getter
    private List<Integer> disabledCameras = new ArrayList<>();
    private String cameraState;
    @Getter
    private static Set<CamPlayer> editors = new HashSet<>();

    public CamPlayer(Player p) {
        this.p = p;
    }

    public CamPlayer(Player p, List<Integer> disabledCameras){
        this.p = p;
        this.disabledCameras = disabledCameras;
    }

    public static void setEditors(CamPlayer camPlayer, boolean add){
        if (add){
            editors.add(camPlayer);
        } else {
            editors.remove(camPlayer);
        }
    }

    public void addFollower(Player follower) {
        if(!followers.contains(follower)) {
            followers.add(follower);
            CamPlayer camFollower = plugin.getPlayer(follower);
            camFollower.startFollowing(p);
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
    public boolean isEditing() {return editing != null;}
    public void startEditing(Track track) {
        editing = track;
        CamPlayer.setEditors(this, true);
    }
    public void stopEditing() {
        editing = null;
        CamPlayer.setEditors(this, false);
    }

    public void setBestCam(Camera camera) {
        for (Player follower: followers) {
            CamPlayer camPlayer = plugin.getPlayer(follower);
            if(camPlayer.getCurrentCamera() != camera) {
                camera.tpPlayer(follower);
                camPlayer.setCurrentCamera(camera);
                follower.lookAt(p, LookAnchor.EYES, LookAnchor.EYES);
                //camPlayer.setCurrentCamera(camera);
            }
        }
    }

    public void setInv(boolean inventory) {
        this.inv = inventory;
        if(!inventory) {cameraItems.clear();}
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

}
