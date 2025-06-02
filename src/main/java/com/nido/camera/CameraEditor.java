package com.nido.camera;

import me.makkuusen.timing.system.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class CameraEditor {

    private static HashMap<String, Track> trackRegions = new HashMap<>();

    public static void setTrackRegions(String minmax, Track track){
        trackRegions.put(minmax, track);
    }

    public static void removeTrackRegions(String minmax){
        trackRegions.remove(minmax);
    }

    public static Map<String, Track> getTrackRegions(){
        return trackRegions;
    }

    public static Set<String> getKeysByValue(Map<String, Track> map, Track value){
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, Track> entry : map.entrySet()){
            if (Objects.equals(entry.getValue(), value)){
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public void startParticleSpawner(Camera plugin){
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : CamPlayer.getEditors()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                Track track = CamPlayer.getEditorTracks(uuid).orElseThrow();
                Set<String> regions = getKeysByValue(trackRegions, track);


                for (String region : regions) {

                    String[] minAndMax = region.split(":");

                    Vector min = Utils.stringToVector(minAndMax[0]);
                    Vector max = Utils.stringToVector(minAndMax[1]);

                    int maxY = max.getBlockY() + 1;
                    int maxX = max.getBlockX() + 1;
                    int maxZ = max.getBlockZ() + 1;

                    drawLineX(player, Particle.HEART, min.getBlockX(), maxX, min.getBlockY(), min.getBlockZ());
                    drawLineX(player, Particle.HEART, min.getBlockX(), maxX, maxY, min.getBlockZ());
                    drawLineX(player, Particle.HEART, min.getBlockX(), maxX, min.getBlockY(), maxZ);
                    drawLineX(player, Particle.HEART, min.getBlockX(), maxX, maxY, maxZ);

                    drawLineY(player, Particle.HEART, min.getBlockX(), min.getBlockY(), maxY, min.getBlockZ());
                    drawLineY(player, Particle.HEART, min.getBlockX(), min.getBlockY(), maxY, maxZ);
                    drawLineY(player, Particle.HEART, maxX, min.getBlockY(), maxY, min.getBlockZ());
                    drawLineY(player, Particle.HEART, maxX, min.getBlockY(), maxY, maxZ);

                    drawLineZ(player, Particle.HEART, min.getBlockX(), min.getBlockY(), min.getBlockZ(), maxZ);
                    drawLineZ(player, Particle.HEART, min.getBlockX(), maxY, min.getBlockZ(), maxZ);
                    drawLineZ(player, Particle.HEART, maxX, min.getBlockY(), min.getBlockZ(), maxZ);
                    drawLineZ(player, Particle.HEART, maxX, maxY, min.getBlockZ(), maxZ);



                }

            }
        }, 0, 10);
    }

    private void drawLineX(Player player, Particle particle, int x1, int x2, int y, int z) {
        for (int x = x1; x <= x2; x++) {
            player.spawnParticle(particle, x, y, z, 20);
        }
    }

    private void drawLineY(Player player, Particle particle, int x, int y1, int y2, int z) {
        for (int y = y1; y <= y2; y++) {
            player.spawnParticle(particle, x, y, z, 20);
        }
    }

    private void drawLineZ(Player player, Particle particle, int x, int y, int z1, int z2) {
        for (int z = z1; z <= z2; z++) {
            player.spawnParticle(particle, x, y, z, 20);
        }
    }
}
