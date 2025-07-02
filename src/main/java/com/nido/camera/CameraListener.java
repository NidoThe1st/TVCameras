package com.nido.camera;

import co.aikar.idb.DB;
import me.makkuusen.timing.system.api.TimingSystemAPI;
import me.makkuusen.timing.system.api.events.heat.HeatLoadEvent;
import me.makkuusen.timing.system.event.Participant;
import me.makkuusen.timing.system.event.heat.Heat;
import me.makkuusen.timing.system.tplayer.TPlayer;
import me.makkuusen.timing.system.track.Track;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class CameraListener implements Listener {
    CameraPlugin plugin = CameraPlugin.getInstance();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        TPlayer tPlayer = TimingSystemAPI.getTPlayer(p.getUniqueId());
        CamPlayer camPlayer = plugin.getPlayer(p);
        //check if player is inside a boat
        if (tPlayer.getParticipant().isPresent()){
            Participant participant = tPlayer.getParticipant().get();
            Track track = participant.getEvent().getTrack();
            if(p.isInsideVehicle() && p.getVehicle() instanceof Boat) {
                if(!camPlayer.getFollowers().isEmpty()) {
                    for (Camera camera : plugin.getCameras()) {
                        if (camera.getTrack() == track) {
                            if (camera.isInsideRegion(p)) {
                                if (camera.getRegionType().equals("onboard")){
                                    for (Player follower : camPlayer.getFollowers()){
                                        follower.setSpectatorTarget(p);
                                    }
                                } else if (camera.getRegionType().equals("static")) {
                                    for (Player follower : camPlayer.getFollowers()){
                                        if (follower.getSpectatorTarget() != null){
                                            follower.setSpectatorTarget(null);
                                        }
                                    }
                                    camPlayer.setBestCam(camera);
                                }
                            }
                        }
                    }
                }
            } else{
                for (Camera camera : plugin.getCameras()){
                    if (camera.getTrack() == track){
                        if (camera.isInsideRegion(p)){
                            if (camera.getRegionType().equals("podium")){
                                camPlayer.setBestCam(camera);
                            }
                        }
                    }
                }
            }
        } else if (camPlayer.isEditing()) {
            for (Camera camera : plugin.getCameras()){
                if (camera.getTrack() == camPlayer.getEditing()){
                    if (camera.isInsideRegion(p)){
                        p.sendActionBar(Component.text("Index: " + camera.getIndex() + " | " + "Region Type: " + camera.getRegionType()).color(NamedTextColor.AQUA));
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        plugin.onQuit(p);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        try {
            var playerRow = DB.getFirstRow("SELECT * FROM Camera_Players WHERE UUID = '" + p.getUniqueId() + "';");
            if(playerRow != null) {
                plugin.addCamPlayer(p, playerRow);
            } else {plugin.newCamPlayer(p);}
        } catch (SQLException s) {s.printStackTrace();}
    }
    @EventHandler
    public void onLClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();
        CamPlayer camPlayer = plugin.getPlayer(p);
        HashMap<Integer, Camera> cameraItems = camPlayer.getCameraItems();
        if (camPlayer.isInv()){
            e.setCancelled(true);
            if (e.getClick() == ClickType.LEFT){
                if (cameraItems.containsKey(e.getSlot())) {
                    Camera camera = cameraItems.get(e.getSlot());
                    camera.tpPlayer(p);
                    e.getInventory().close();
                }
            }
        }
    }
    // --NEW--
    @EventHandler
    public void onRClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();
        CamPlayer camPlayer = plugin.getPlayer(p);
        HashMap<Integer, Camera> cameraItems = camPlayer.getCameraItems();
        if (camPlayer.isInv()){
            e.setCancelled(true);
            if (e.getClick() == ClickType.RIGHT){
                if (cameraItems.containsKey(e.getSlot())) {
                    Camera camera = cameraItems.get(e.getSlot());
                    if (camPlayer.isCameraDisabled(camera.getId())){
                        camPlayer.enableCamera(camera.getId());
                    }else {
                        camPlayer.disableCamera(camera.getId());
                    }
                    e.getInventory().close();
                    Utils.openMenu(p);
                }
            }
        }
    }
    @EventHandler
    public void onDrag(InventoryDragEvent e){
        Player p = (Player) e.getWhoClicked();
        CamPlayer camPlayer = plugin.getPlayer(p);
        if (camPlayer.isInv()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        CamPlayer camPlayer = plugin.getPlayer(p);
        if(e.getInventory().getType() == InventoryType.CHEST) {
            if (camPlayer.isInv()) {
                camPlayer.setInv(false);
            }
        }
    }
    @EventHandler
    public void onHeatLoad(HeatLoadEvent e){
        Heat heat = e.getHeat();
        for (UUID uuid : heat.getDrivers().keySet()){
            Player p = Bukkit.getPlayer(uuid);
            CamPlayer camPlayer = plugin.getPlayer(p);
            if (camPlayer != null){
                if (!camPlayer.getFollowers().isEmpty()){
                    for (Camera camera : plugin.getCameras()){
                        if (camera.getTrack() == heat.getEvent().getTrack()){
                            if (camera.getRegionType().equals("grid")){
                                camPlayer.setBestCam(camera);
                            }
                        }
                    }
                }
            }
        }
    }
}