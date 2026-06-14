package moth.butterflyapi.content.blocks;

import moth.butterflyapi.plush.PlushDefinition;
import moth.butterflyapi.plush.PlushRegistrar;
import moth.butterflyapi.plush.RegisteredPlush;
import moth.butterflyapi.content.ContentBootstrap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Plushes {
    private static final Map<String, PlushDefinition> ALL = new LinkedHashMap<>();
    private static final Map<String, RegisteredPlush> REGISTERED = new LinkedHashMap<>();

    private Plushes() {
    }

    private static String plush(String id) {
        return plush(PlushDefinition.builder(ContentBootstrap.id(id)).build());
    }

    private static String plush(PlushDefinition definition) {
        ALL.put(definition.id().getPath(), definition);
        return definition.id().getPath();
    }

    public static final String SYNTAX = plush("syntax_plush");
    public static final String HEX = plush("hex_plush");
    public static final String HEX_MAID = plush("hex_maid_plush");
    public static final String MOTH = plush(
            PlushDefinition.geckoBuilder(ContentBootstrap.id("moth_plush"))
                    .loopingAnimation("moth_idle", "moth_plush")
                    .build()
    );
    public static final String CHICKEN = plush("chicken_plush");
    public static final String CAPOZI = plush("capozi_plush");
    public static final String DEBUG = plush("debug_plush");

    public static PlushDefinition definition(String id) {
        PlushDefinition definition = ALL.get(id);
        if (definition == null) {
            throw new IllegalStateException("Unknown plush id: " + id);
        }
        return definition;
    }

    public static void registerAll(PlushRegistrar registrar) {
        for (Map.Entry<String, PlushDefinition> entry : ALL.entrySet()) {
            REGISTERED.put(entry.getKey(), registrar.register(entry.getValue()));
        }
    }

    public static RegisteredPlush registered(String id) {
        RegisteredPlush plush = REGISTERED.get(id);
        if (plush == null) {
            throw new IllegalStateException("Unknown plush id: " + id);
        }
        return plush;
    }

    public static List<String> all() {
        return List.copyOf(ALL.keySet());
    }
}
