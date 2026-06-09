package com.csclub05.outlineguard.client;

import com.csclub05.outlineguard.OutlineGuard;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class OutlineGuardClientProtection {
    private static long lastMessageTimeMs;

    private OutlineGuardClientProtection() {
    }

    public static boolean shouldBlockMining(BlockPos targetPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world == null || client.player == null || targetPos == null) {
            return false;
        }

        BlockState targetState = world.getBlockState(targetPos);

        // Always allow the player to remove or move the outline marker itself.
        if (OutlineGuard.CONFIG.isMarker(targetState.getBlock())) {
            return false;
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int y = world.getBottomY(); y <= world.getTopYInclusive(); y++) {
            mutable.set(targetPos.getX(), y, targetPos.getZ());
            BlockState state = world.getBlockState(mutable);
            if (OutlineGuard.CONFIG.isMarker(state.getBlock())) {
                return true;
            }
        }

        return false;
    }

    public static void showBlockedMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastMessageTimeMs < 750L) {
            return;
        }

        lastMessageTimeMs = now;
        client.player.sendMessage(Text.literal("Outline Guard blocked mining past your marker."), true);
    }
}
