package com.csclub05.outlineguard.client;

import com.csclub05.outlineguard.OutlineGuard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class OutlineGuardClient implements ClientModInitializer {
    private static KeyBinding openGuiKey;
    private static boolean comboWasDown;

    // True only while Outline Guard has temporarily paused the attack key.
    // This lets us resume mining automatically when the player moves onto a safe block
    // while still physically holding left click.
    private static boolean miningPausedByOutlineGuard;

    @Override
    public void onInitializeClient() {
        OutlineGuard.CONFIG.load();

        // Hard safety check: cancel the local block attack before Minecraft can start or
        // continue breaking a protected block. This fixes 1.21.8 cases where simply
        // releasing the attack key in a tick handler was too late and blocks in the
        // marker column could still be mined.
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient() && OutlineGuardClientProtection.shouldBlockMining(pos)) {
                OutlineGuardClientProtection.showBlockedMessage();
                MinecraftClient client = MinecraftClient.getInstance();
                client.options.attackKey.setPressed(false);
                if (client.interactionManager != null) {
                    client.interactionManager.cancelBlockBreaking();
                }
                miningPausedByOutlineGuard = true;
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.outlineguard.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.outlineguard"
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.currentScreen != null) {
                return;
            }

            boolean physicallyAttacking = isLeftMouseHeld(client);
            BlockHitResult blockHit = getBlockHit(client);
            boolean blocked = blockHit != null && OutlineGuardClientProtection.shouldBlockMining(blockHit.getBlockPos());

            if (!physicallyAttacking) {
                miningPausedByOutlineGuard = false;
                return;
            }

            if (blocked) {
                OutlineGuardClientProtection.showBlockedMessage();
                miningPausedByOutlineGuard = true;

                // Pause only while the crosshair is on a protected block.
                // The END tick handler below restores the key as soon as the target is safe again,
                // so the player does not have to release and click again.
                client.options.attackKey.setPressed(false);
                if (client.interactionManager != null) {
                    client.interactionManager.cancelBlockBreaking();
                }
                return;
            }

            // If we previously paused mining, resume it automatically once the player is still
            // holding left click and their crosshair is now on a valid block.
            if (miningPausedByOutlineGuard) {
                client.options.attackKey.setPressed(true);
                miningPausedByOutlineGuard = false;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) {
                return;
            }

            long handle = client.getWindow().getHandle();
            boolean comboIsDown = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_D)
                    && InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_C);

            if (comboIsDown && !comboWasDown && client.currentScreen == null) {
                client.setScreen(new OutlineGuardMarkerScreen());
            }
            comboWasDown = comboIsDown;

            // Also support Minecraft's keybinding system so the key can be changed in Controls.
            // By default this is C. This fallback is useful if another mod or keyboard layout
            // makes the raw D + C combo unreliable.
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new OutlineGuardMarkerScreen());
                }
            }
        });
    }

    private static BlockHitResult getBlockHit(MinecraftClient client) {
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return (BlockHitResult) client.crosshairTarget;
    }

    private static boolean isLeftMouseHeld(MinecraftClient client) {
        if (client.getWindow() == null) {
            return false;
        }
        long handle = client.getWindow().getHandle();
        return GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    public static List<Identifier> markerBlocks() {
        return OutlineGuard.CONFIG.markerBlocks();
    }
}
