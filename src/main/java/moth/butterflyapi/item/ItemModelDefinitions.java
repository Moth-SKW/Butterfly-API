package moth.butterflyapi.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class ItemModelDefinitions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ItemModelDefinitions() {
    }

    public static SeparateGuiHeldModels separateGuiAndHeld(Identifier itemId) {
        Objects.requireNonNull(itemId, "itemId");
        Identifier guiModel = itemId.withPrefixedPath("item/");
        Identifier heldModel = itemId.withPrefixedPath("item/").withSuffixedPath("_held");
        return separateGuiAndHeld(itemId, guiModel, heldModel);
    }

    public static SeparateGuiHeldModels separateGuiAndHeld(Identifier itemId, Identifier guiModel, Identifier heldModel) {
        return new SeparateGuiHeldModels(
                Objects.requireNonNull(itemId, "itemId"),
                Objects.requireNonNull(guiModel, "guiModel"),
                Objects.requireNonNull(heldModel, "heldModel")
        );
    }

    public static String modelDefinitionJson(Identifier model) {
        JsonObject root = new JsonObject();
        root.add("model", modelObject(model));
        return GSON.toJson(root);
    }

    public static String generatedModelJson(Identifier texture) {
        return texturedModelJson("minecraft:item/generated", texture);
    }

    public static String handheldModelJson(Identifier texture) {
        return texturedModelJson("minecraft:item/handheld", texture);
    }

    public static String texturedModelJson(String parent, Identifier texture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", Objects.requireNonNull(parent, "parent"));

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", Objects.requireNonNull(texture, "texture").toString());
        root.add("textures", textures);
        return GSON.toJson(root);
    }

    private static JsonObject modelObject(Identifier model) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "minecraft:model");
        object.addProperty("model", model.toString());
        return object;
    }

    public record SeparateGuiHeldModels(Identifier itemId, Identifier guiModel, Identifier heldModel) {
        public SeparateGuiHeldModels {
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(guiModel, "guiModel");
            Objects.requireNonNull(heldModel, "heldModel");
        }

        public String itemDefinitionPath() {
            return "assets/" + itemId.getNamespace() + "/items/" + itemId.getPath() + ".json";
        }

        public String guiModelPath() {
            return "assets/" + guiModel.getNamespace() + "/models/" + guiModel.getPath() + ".json";
        }

        public String heldModelPath() {
            return "assets/" + heldModel.getNamespace() + "/models/" + heldModel.getPath() + ".json";
        }

        public String itemDefinitionJson() {
            JsonObject root = new JsonObject();
            JsonObject select = new JsonObject();
            select.addProperty("type", "minecraft:select");
            select.addProperty("property", "minecraft:display_context");

            JsonObject heldCase = new JsonObject();
            JsonArray heldContexts = new JsonArray();
            heldContexts.add("firstperson_lefthand");
            heldContexts.add("firstperson_righthand");
            heldContexts.add("thirdperson_lefthand");
            heldContexts.add("thirdperson_righthand");
            heldCase.add("when", heldContexts);
            heldCase.add("model", modelObject(heldModel));

            JsonArray cases = new JsonArray();
            cases.add(heldCase);
            select.add("cases", cases);
            select.add("fallback", modelObject(guiModel));
            root.add("model", select);
            return GSON.toJson(root);
        }

        public String guiModelJson(Identifier texture) {
            return generatedModelJson(texture);
        }

        public String heldModelJson(Identifier texture) {
            return handheldModelJson(texture);
        }
    }
}
