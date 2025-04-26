package com.nido.camera;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import javax.swing.plaf.synth.Region;
import java.sql.SQLException;
import java.util.*;

public class Tasks {

    public Tasks(){}

    public void startParticleSpawner(Camera plugin){
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : CamPlayer.getEditors()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                Track track = CamPlayer.getEditorTracks(uuid).orElseThrow();

                //insert getting regions on that track and calling setParticles
                List<DbRow> regions;
                try {
                    regions = DB.getResults("SELECT `REGION` FROM `Cameras` WHERE `TRACKID` = '" + track + "';");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                for (DbRow dbRow : regions) {
                    String regionMinMax = dbRow.getString("REGION");
                    String[] regionMinAndMax = regionMinMax.split(":");
                    Location min = Utils.stringToLocation(regionMinAndMax[0]);
                    Location max = Utils.stringToLocation(regionMinAndMax[1]);

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
