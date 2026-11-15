package dev.shrey.hackmenu;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class HackScreen extends Screen {
    public HackScreen() {
        super(Component.literal("Hack Menu"));
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "ON" : "OFF"))
            .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private void toggle(int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        Button b = Button.builder(label(name, get.getAsBoolean()), btn -> {
            boolean v = !get.getAsBoolean();
            set.accept(v);
            btn.setMessage(label(name, v));
        }).bounds(this.width / 2 - 100, y, 200, 20).build();
        this.addRenderableWidget(b);
    }

    private static void effectToggle(String id, int amp, boolean on) {
        HackMenuClient.effect(Minecraft.getInstance(), on, id, amp);
    }

    @Override
    protected void init() {
        int y = Math.max(28, this.height / 2 - 125);
        int step = 22;
        Minecraft mc = Minecraft.getInstance();

        toggle(y, "X-Ray", () -> Hacks.xray, v -> HackMenuClient.setXray(mc, v)); y += step;
        toggle(y, "Night Vision", () -> Hacks.nightVision, v -> { Hacks.nightVision = v; effectToggle("night_vision", 0, v); }); y += step;
        toggle(y, "Fire Resistance", () -> Hacks.fire, v -> { Hacks.fire = v; effectToggle("fire_resistance", 0, v); }); y += step;
        toggle(y, "Resistance", () -> Hacks.resistance, v -> { Hacks.resistance = v; effectToggle("resistance", 4, v); }); y += step;
        toggle(y, "Instant Healing", () -> Hacks.heal, v -> Hacks.heal = v); y += step;
        toggle(y, "Feather Falling", () -> Hacks.feather, v -> { Hacks.feather = v; effectToggle("slow_falling", 0, v); }); y += step;
        toggle(y, "Fast Mining (Haste)", () -> Hacks.haste, v -> { Hacks.haste = v; effectToggle("haste", Hacks.hasteLevel - 1, v); }); y += step;

        Button lvl = Button.builder(Component.literal("Haste Level: " + Hacks.hasteLevel), btn -> {
            Hacks.hasteLevel = Hacks.hasteLevel % 5 + 1;
            btn.setMessage(Component.literal("Haste Level: " + Hacks.hasteLevel));
            if (Hacks.haste) effectToggle("haste", Hacks.hasteLevel - 1, true);
        }).bounds(this.width / 2 - 100, y, 200, 20).build();
        this.addRenderableWidget(lvl); y += step;

        toggle(y, "Miner 3x3", () -> Hacks.miner3x3, v -> Hacks.miner3x3 = v); y += step + 4;

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
            .bounds(this.width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
