package moth.butterflyapi.client.item;

import moth.butterflyapi.item.bar.ItemProgressBar;
import moth.butterflyapi.item.bar.ItemProgressBars;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class ItemProgressBarRenderer {
    private static final int BAR_WIDTH = 13;
    private static final int BACKGROUND_COLOR = 0xFF000000;

    private ItemProgressBarRenderer() {
    }

    public static boolean render(GuiGraphics context, ItemStack stack, int x, int y) {
        List<ItemProgressBar> bars = ItemProgressBars.bars(stack);
        if (bars.isEmpty()) {
            return false;
        }

        int row = 0;
        for (ItemProgressBar bar : bars) {
            if (bar == null) {
                continue;
            }
            renderBar(context, bar, x, y - row * 2);
            row++;
        }
        return row > 0;
    }

    private static void renderBar(GuiGraphics context, ItemProgressBar bar, int x, int y) {
        int barX = x + 2;
        int barY = y + 13;
        context.fill(barX, barY, barX + BAR_WIDTH, barY + 2, BACKGROUND_COLOR);

        double progress = Math.max(0.0D, bar.progress());
        long fullLayer = (long) Math.floor(Math.min(progress, Long.MAX_VALUE));
        double layerProgress = progress - fullLayer;

        if (fullLayer > 0) {
            context.fill(barX, barY, barX + BAR_WIDTH, barY + 1, withAlpha(bar.color(fullLayer - 1)));
        }

        int width = barWidth(layerProgress, bar.pixelSnap());
        if (width > 0) {
            context.fill(barX, barY, barX + width, barY + 1, withAlpha(bar.color(fullLayer)));
        }
    }

    private static int barWidth(double progress, boolean pixelSnap) {
        double clamped = Mth.clamp(progress, 0.0D, 1.0D);
        if (clamped <= 0.0D) {
            return 0;
        }
        if (clamped >= 1.0D) {
            return BAR_WIDTH;
        }
        return pixelSnap
                ? Mth.clamp((int) Math.round(clamped * BAR_WIDTH), 1, BAR_WIDTH)
                : Mth.clamp((int) Math.ceil(clamped * BAR_WIDTH), 1, BAR_WIDTH);
    }

    private static int withAlpha(int color) {
        return 0xFF000000 | (color & 0x00FFFFFF);
    }
}
