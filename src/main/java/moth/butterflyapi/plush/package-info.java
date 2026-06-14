/**
 * Shared plush registration helpers for Butterfly API mods.
 *
 * <p>Typical usage from a {@code butterfly_api:plush} entrypoint:
 *
 * <pre>{@code
 * public final class ExamplePlushes implements PlushEntrypoint {
 *     private static final ModContext MOD = ButterflyApi.mod("example");
 *
 *     @Override
 *     public void registerPlushes(PlushRegistrar registrar) {
 *         registrar.plush(MOD, "plain_plush");
 *
 *         registrar.geckoPlush(MOD, "animated_plush",
 *                 builder -> builder.loopingAnimation("idle"));
 *     }
 * }
 * }</pre>
 *
 * <p>The Gecko helper assumes the usual asset layout:
 * {@code geo/block/<path>.geo.json},
 * {@code textures/block/<path>.png},
 * and {@code animations/block/<path>.animation.json}.
 */
package moth.butterflyapi.plush;
