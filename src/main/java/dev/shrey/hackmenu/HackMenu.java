package dev.shrey.hackmenu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/** Server side (integrated server in single player): 3x3 miner. */
public class HackMenu implements ModInitializer {
    private static boolean busy = false;

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, be) -> {
            if (!Hacks.miner3x3 || busy || !(player instanceof ServerPlayer sp)) return;
            Direction dir = sp.getDirection();
            float pitch = sp.getXRot();
            busy = true;
            try {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        BlockPos p;
                        if (Math.abs(pitch) > 55f) p = pos.offset(i, 0, j);               // looking up/down
                        else if (dir.getAxis() == Direction.Axis.X) p = pos.offset(0, i, j); // facing east/west
                        else p = pos.offset(i, j, 0);                                      // facing north/south
                        BlockState s = level.getBlockState(p);
                        if (s.isAir() || !s.getFluidState().isEmpty()) continue;
                        if (s.getDestroySpeed(level, p) < 0) continue; // bedrock etc.
                        sp.gameMode.destroyBlock(p);
                    }
                }
            } finally {
                busy = false;
            }
        });
    }
}
