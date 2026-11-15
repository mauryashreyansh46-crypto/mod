package dev.shrey.hackmenu;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Client side: key H opens the menu, effects are applied with /effect commands (needs cheats / OP). */
public class HackMenuClient implements ClientModInitializer {
    static final String PACK_FILE = "XrayPack.zip";
    static final String PACK_ID = "file/" + PACK_FILE;
    private static KeyMapping openKey;
    private static int tick = 0;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.hackmenu.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, KeyMapping.Category.MISC));

        // copy bundled xray pack into resourcepacks/ so the menu can toggle it
        try {
            Minecraft mc = Minecraft.getInstance();
            File dir = new File(mc.gameDirectory, "resourcepacks");
            dir.mkdirs();
            try (InputStream in = HackMenuClient.class.getResourceAsStream("/assets/hackmenu/" + PACK_FILE)) {
                if (in != null) Files.copy(in, new File(dir, PACK_FILE).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("[HackMenu] could not copy xray pack: " + e);
        }

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.player != null && mc.screen == null) mc.setScreen(new HackScreen());
            }
            if (mc.player == null) return;
            tick++;
            if (tick % 1000 == 0) applyAll(mc);
            if (Hacks.heal && tick % 20 == 0 && mc.player.getHealth() < mc.player.getMaxHealth()) {
                cmd(mc, "effect give @s minecraft:instant_health 1 1 true");
            }
        });
    }

    static void cmd(Minecraft mc, String c) {
        if (mc.player != null) mc.player.connection.sendCommand(c);
    }

    /** Give (or clear) one effect depending on its toggle. */
    static void effect(Minecraft mc, boolean on, String id, int amp) {
        if (on) cmd(mc, "effect give @s minecraft:" + id + " 120 " + amp + " true");
        else cmd(mc, "effect clear @s minecraft:" + id);
    }

    static void applyAll(Minecraft mc) {
        if (Hacks.nightVision) effect(mc, true, "night_vision", 0);
        if (Hacks.fire) effect(mc, true, "fire_resistance", 0);
        if (Hacks.resistance) effect(mc, true, "resistance", 4);
        if (Hacks.feather) effect(mc, true, "slow_falling", 0);
        if (Hacks.haste) effect(mc, true, "haste", Hacks.hasteLevel - 1);
    }

    static void setXray(Minecraft mc, boolean on) {
        Hacks.xray = on;
        PackRepository repo = mc.getResourcePackRepository();
        repo.reload();
        List<String> sel = new ArrayList<>(repo.getSelectedIds());
        sel.remove(PACK_ID);
        if (on) sel.add(PACK_ID);
        repo.setSelected(sel);
        mc.options.updateResourcePacks(repo); // reloads resources (few seconds)
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("X-ray " + (on ? "ON" : "OFF")), true);
    }
}
