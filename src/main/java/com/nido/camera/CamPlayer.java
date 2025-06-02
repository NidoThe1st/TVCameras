package com.nido.camera;

import io.papermc.paper.entity.LookAnchor;
import lombok.Getter;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class CamPlayer {
    private Track editing;
    private Camera plugin = Camera.getInstance();
    @Getter
    private Player p;
    private CamPlayer following;
    private Cam currentCamera;
    private List<Player> followers = new ArrayList<>();
    private HashMap<Integer, Cam> cameraItems = new HashMap<>();
    private boolean inv = false;
    private List<Integer> disabledCameras = new ArrayList<>();
    private String cameraState;
    @Getter
    //list of all players that are editing
    private static Set<CamPlayer> editors = new HashSet<>();
    //list of tracks that players are editing
    private static final HashMap<UUID, Track> editorTracks = new HashMap<>();

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

    public static void setEditorTracks(UUID uuid, Track track){
        editorTracks.put(uuid, track);
    }

    public static void removeEditorTracks(UUID uuid){
        editorTracks.remove(uuid);
    }

    public static Optional<Track> getEditorTracks(UUID uuid){
        return Optional.ofNullable(editorTracks.get(uuid));
    }

    public List<Player> getFollowers() {
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
    public boolean isEditing() {return editing != null;}
    public void startEditing(Track track) {
        editing = track;
        CamPlayer.setEditors(this, true);
    }
    public void stopEditing() {
        editing = null;
        CamPlayer.setEditors(this, false);
    }
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
    public List<Integer> getDisabledCameras() {
        return disabledCameras;
    }

}
