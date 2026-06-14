package moth.butterflyapi.content.items;

import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.content.ContentBootstrap;
import moth.butterflyapi.content.blocks.ModBlocks;
import moth.butterflyapi.content.blocks.Plushes;
import moth.butterflyapi.mod.ModContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModGroups {
    private static final ModContext MOD = ButterflyApi.mod(ContentBootstrap.MOD_ID, ButterflyApi.MOD_NAME);
    private static final Identifier BA_SLOT = new Identifier(
            ContentBootstrap.MOD_ID,
            "textures/butterfly_api_slot.png"
    );
    private static final Identifier BA_CATEGORY = new Identifier(
            ContentBootstrap.MOD_ID,
            "textures/butterfly_api_category.png"
    );

    public static final ItemGroup BUTTERFLY_API_GROUP = MOD.tabBuilder(ContentBootstrap.MOD_ID)
            .translationKey("itemGroup.butterfly_api")
            .icon(() -> new ItemStack(ModBlocks.plush(Plushes.SYNTAX)))
            .backgroundTexture(new Identifier(
                    "minecraft",
                    "textures/gui/container/creative_inventory/tab_item_search.png"
            ))
            .customOverlay(true)
            .slotBackgroundTexture(BA_SLOT)
            .betweenSlotsColor("#0f0e0e")
            .tabNameColor("#544a4a")
            .category("butterfly_api", Text.literal("*' Butterfly API '*"), category -> category
                    .alignLeft()
                    .backgroundTexture(BA_CATEGORY)
                    .borderColor("#0f0e0e")
                    .textColor("#544a4a")
                    .entry(
                            ModBlocks.plush(Plushes.SYNTAX),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.SYNTAX))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.HEX),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.HEX))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.RINGMASTER_HEX),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.RINGMASTER_HEX))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.HEX_MAID),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.HEX_MAID))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.MOTH),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.MOTH))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.CHICKEN),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.CHICKEN))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.CAPOZI),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.CAPOZI))
                    )
                    .entry(
                            ModBlocks.plush(Plushes.DEBUG),
                            entry -> entry.slotTexture(plushSlotTexture(Plushes.DEBUG))
                    )
                    .add(ModBlocks.BIG_RED_BUTTON)
                    .searchOnly(ModItems.MATCHSTICK)
            )
            .build();

    private static Identifier plushSlotTexture(String plushName) {
        return new Identifier(
                ContentBootstrap.MOD_ID,
                "textures/" + plushName + "_slot.png"
        );
    }

    public static void init() {
    }

    private ModGroups() {
    }
}
