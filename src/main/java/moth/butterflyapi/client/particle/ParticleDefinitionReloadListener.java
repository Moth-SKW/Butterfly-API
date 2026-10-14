package moth.butterflyapi.client.particle;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import moth.butterflyapi.ButterflyApi;
import moth.butterflyapi.particle.ParticleDefinition;
import moth.butterflyapi.particle.ParticleDefinitionLoader;
import moth.butterflyapi.particle.ParticleDefinitions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
final class ParticleDefinitionReloadListener extends SimplePreparableReloadListener<Map<Identifier, ParticleDefinition>>
//? if <1.21.11 {
/*
        implements net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
*///?}
{
    private static final String DIRECTORY = "butterfly_particles";

    @Override
    protected Map<Identifier, ParticleDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, ParticleDefinition> definitions = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = manager.listResources(
                DIRECTORY,
                id -> id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier definitionId = definitionId(entry.getKey());
            try (Reader reader = entry.getValue().openAsReader()) {
                definitions.put(definitionId, ParticleDefinitionLoader.parse(definitionId, reader));
            } catch (IOException | RuntimeException exception) {
                ButterflyApi.LOGGER.error("Failed to load particle definition {}", definitionId, exception);
            }
        }
        return Map.copyOf(definitions);
    }

    @Override
    protected void apply(
            Map<Identifier, ParticleDefinition> definitions,
            ResourceManager manager,
            ProfilerFiller profiler
    ) {
        ParticleDefinitions.replace(definitions);
        ClientParticleManager.clear();
        ButterflyApi.LOGGER.info("Loaded {} Butterfly particle definition(s)", definitions.size());
    }

    private static Identifier definitionId(Identifier resourceId) {
        String path = resourceId.getPath();
        String relative = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative);
    }

//? if <1.21.11 {
/*
    @Override
    public Identifier getFabricId() {
        return ButterflyApi.id("particle_definitions");
    }
*///?}
}
