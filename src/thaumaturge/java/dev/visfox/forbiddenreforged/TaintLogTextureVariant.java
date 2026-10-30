package dev.visfox.forbiddenreforged;

import java.util.Random;
import net.minecraft.core.Direction;

public final class TaintLogTextureVariant {
    public static final int STANDARD = 0;
    public static final int GROSS = 1;
    public static final int GROSS_2 = 2;

    private TaintLogTextureVariant() {
    }

    public static int select(int side, int x, int y, int z) {
        Random random = new Random((long) (side + y + x * z));
        if (random.nextInt(100) < 75) {
            return STANDARD;
        }
        return random.nextBoolean() ? GROSS : GROSS_2;
    }

    public static int variantForDirection(Direction.Axis axis, Direction direction, int x, int y, int z) {
        if (direction.getAxis() == axis) {
            return STANDARD;
        }
        return select(direction.get3DDataValue(), x, y, z);
    }
}
