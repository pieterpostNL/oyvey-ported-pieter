package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.event.system.Subscribe;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChestHighlight extends Module {
    public Setting<Color> chestColor = color("ChestColor", 255, 255, 0, 255);
    public Setting<Color> enderChestColor = color("EnderChestColor", 255, 0, 255, 255);
    public Setting<Float> lineWidth = num("LineWidth", 2.0f, 0.1f, 5.0f);
    public Setting<Integer> range = num("Range", 50, 10, 150);
    public Setting<Boolean> normalChests = bool("NormalChests", true);
    public Setting<Boolean> enderChests = bool("EnderChests", true);
    public Setting<Boolean> tracers = bool("Tracers", false);

    public ChestHighlight() {
        super("ChestHighlight", "Highlights chests in your render distance", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        Vec3 playerPos = mc.player.getEyePosition(event.getPartialTicks());
        List<BlockEntity> chests = getChestsInRange();

        for (BlockEntity chest : chests) {
            BlockPos pos = chest.getBlockPos();
            
            // Check distance
            if (playerPos.distanceTo(Vec3.atCenterOf(pos)) > range.getValue()) continue;

            // Determine color based on chest type
            Color color;
            if (chest instanceof EnderChestBlockEntity && enderChests.getValue()) {
                color = enderChestColor.getValue();
            } else if (chest instanceof ChestBlockEntity && normalChests.getValue()) {
                color = chestColor.getValue();
            } else {
                continue;
            }

            // Draw box around chest
            AABB box = new AABB(pos);
            RenderUtil.drawBox(event.getMatrix(), box, color, lineWidth.getValue());

            // Draw tracer if enabled
            if (tracers.getValue()) {
                Vec3 chestCenter = Vec3.atCenterOf(pos);
                RenderUtil.drawLine(event.getMatrix(), playerPos, chestCenter, color, lineWidth.getValue());
            }
        }
    }

    private List<BlockEntity> getChestsInRange() {
        List<BlockEntity> chests = new ArrayList<>();
        int range = this.range.getValue();
        BlockPos playerPos = mc.player.blockPosition();

        // Search in a cube around the player
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockEntity blockEntity = mc.level.getBlockEntity(pos);

                    if (blockEntity instanceof ChestBlockEntity || 
                        blockEntity instanceof EnderChestBlockEntity) {
                        chests.add(blockEntity);
                    }
                }
            }
        }

        return chests;
    }
}
