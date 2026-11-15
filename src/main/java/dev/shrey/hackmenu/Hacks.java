package dev.shrey.hackmenu;

/** Shared on/off state. Same JVM in single player, so server-side code can read it. */
public final class Hacks {
    public static volatile boolean xray, nightVision, fire, resistance, heal, feather, haste, miner3x3;
    public static volatile int hasteLevel = 3; // 1..5
    private Hacks() {}
}
