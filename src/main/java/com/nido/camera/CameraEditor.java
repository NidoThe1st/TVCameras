package com.nido.camera;

import me.makkuusen.timing.system.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CameraEditor {

    public void startParticleSpawner(CameraPlugin plugin){
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (CamPlayer camPlayer : CamPlayer.getEditors()) {
                Player player = camPlayer.getP();
                if (player == null) continue;
                Track track = camPlayer.getEditing();

                for (Camera camera : CameraPlugin.getInstance().cameras) {
                    if (camera.getTrack().getId() != track.getId()) {
                        continue;
                    }

                    Vector min = camera.getMin();
                    Vector max = camera.getMax();

                    int maxY = max.getBlockY() + 1;
                    int maxX = max.getBlockX() + 1;
                    int maxZ = max.getBlockZ() + 1;

                    drawLineX(player, Particle.HAPPY_VILLAGER, min.getBlockX(), maxX, min.getBlockY(), min.getBlockZ());
                    drawLineX(player, Particle.HAPPY_VILLAGER, min.getBlockX(), maxX, maxY, min.getBlockZ());
                    drawLineX(player, Particle.HAPPY_VILLAGER, min.getBlockX(), maxX, min.getBlockY(), maxZ);
                    drawLineX(player, Particle.HAPPY_VILLAGER, min.getBlockX(), maxX, maxY, maxZ);

                    drawLineY(player, Particle.HAPPY_VILLAGER, min.getBlockX(), min.getBlockY(), maxY, min.getBlockZ());
                    drawLineY(player, Particle.HAPPY_VILLAGER, min.getBlockX(), min.getBlockY(), maxY, maxZ);
                    drawLineY(player, Particle.HAPPY_VILLAGER, maxX, min.getBlockY(), maxY, min.getBlockZ());
                    drawLineY(player, Particle.HAPPY_VILLAGER, maxX, min.getBlockY(), maxY, maxZ);

                    drawLineZ(player, Particle.HAPPY_VILLAGER, min.getBlockX(), min.getBlockY(), min.getBlockZ(), maxZ);
                    drawLineZ(player, Particle.HAPPY_VILLAGER, min.getBlockX(), maxY, min.getBlockZ(), maxZ);
                    drawLineZ(player, Particle.HAPPY_VILLAGER, maxX, min.getBlockY(), min.getBlockZ(), maxZ);
                    drawLineZ(player, Particle.HAPPY_VILLAGER, maxX, maxY, min.getBlockZ(), maxZ);
                }

            }
        }, 0, 10);
    }

    private void drawLineX(Player player, Particle particle, int x1, int x2, int y, int z) {
        for (int x = x1; x <= x2; x++) {
            player.spawnParticle(particle, x, y, z, 10);
        }
    }

    private void drawLineY(Player player, Particle particle, int x, int y1, int y2, int z) {
        for (int y = y1; y <= y2; y++) {
            player.spawnParticle(particle, x, y, z, 10);
        }
    }

    private void drawLineZ(Player player, Particle particle, int x, int y, int z1, int z2) {
        for (int z = z1; z <= z2; z++) {
            player.spawnParticle(particle, x, y, z, 10);
        }
    }
}
