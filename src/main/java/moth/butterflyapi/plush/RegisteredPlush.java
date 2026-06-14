package moth.butterflyapi.plush;

import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RegisteredPlush {
    private final PlushDefinition definition;
    @Nullable
    private SoundEvent sound;
    @Nullable
    private PlushBlock block;
    @Nullable
    private Item item;

    RegisteredPlush(PlushDefinition definition) {
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    public Identifier id() {
        return definition.id();
    }

    public PlushDefinition definition() {
        return definition;
    }

    public SoundEvent sound() {
        return requireRegistered("sound", sound);
    }

    @Nullable
    SoundEvent soundOrNull() {
        return sound;
    }

    public PlushBlock block() {
        return requireRegistered("block", block);
    }

    public Item item() {
        return requireRegistered("item", item);
    }

    void bind(SoundEvent sound, PlushBlock block, Item item) {
        this.sound = Objects.requireNonNull(sound, "sound");
        this.block = Objects.requireNonNull(block, "block");
        this.item = Objects.requireNonNull(item, "item");
    }

    private <T> T requireRegistered(String type, @Nullable T value) {
        if (value == null) {
            throw new IllegalStateException("Plush " + id() + " has not finished registering its " + type + " yet");
        }
        return value;
    }
}
