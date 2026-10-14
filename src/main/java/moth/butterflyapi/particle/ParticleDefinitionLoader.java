package moth.butterflyapi.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class ParticleDefinitionLoader {
    private ParticleDefinitionLoader() {
    }

    public static ParticleDefinition parse(Identifier id, Reader reader) {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (reader == null) {
            throw new NullPointerException("reader");
        }
        try {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) {
                throw invalid(id, "root value must be an object");
            }
            JsonObject root = rootElement.getAsJsonObject();
            requireOnly(id, root, "format_version", "emitter", "particle");
            int formatVersion = requiredInt(id, root, "format_version");
            if (formatVersion != 1) {
                throw invalid(id, "format_version must be 1");
            }
            return new ParticleDefinition(
                    parseEmitterLifetime(id, requiredObject(id, root, "emitter")),
                    parseEmissionRate(id, requiredObject(id, root, "emitter")),
                    parseShape(id, requiredObject(id, root, "emitter")),
                    parseParticleLifetime(id, requiredObject(id, root, "particle")),
                    optionalVector(id, requiredObject(id, root, "particle"), "position", ParticleVector.ZERO),
                    optionalVector(id, requiredObject(id, root, "particle"), "velocity", ParticleVector.ZERO),
                    optionalVector(id, requiredObject(id, root, "particle"), "acceleration", ParticleVector.ZERO),
                    parseColors(id, requiredObject(id, root, "particle"))[0],
                    parseColors(id, requiredObject(id, root, "particle"))[1],
                    parseSizes(id, requiredObject(id, root, "particle"))[0],
                    parseSizes(id, requiredObject(id, root, "particle"))[1],
                    optionalIdentifier(id, requiredObject(id, root, "particle"), "texture")
            );
        } catch (ParticleDefinitionException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ParticleDefinitionException("Invalid particle definition " + id + ": " + exception.getMessage(), exception);
        }
    }

    private static ParticleRange parseEmitterLifetime(Identifier id, JsonObject emitter) {
        requireOnly(id, emitter, "lifetime", "emission_rate", "shape");
        return range(id, emitter, "lifetime");
    }

    private static double parseEmissionRate(Identifier id, JsonObject emitter) {
        return requiredNumber(id, emitter, "emission_rate");
    }

    private static ParticleEmitterShape parseShape(Identifier id, JsonObject emitter) {
        if (!emitter.has("shape")) {
            return ParticleEmitterShape.point();
        }
        JsonElement shapeElement = emitter.get("shape");
        if (shapeElement.isJsonPrimitive() && shapeElement.getAsJsonPrimitive().isString()) {
            return parseShapeType(id, shapeElement.getAsString(), null);
        }
        JsonObject shape = object(id, shapeElement, "shape");
        requireOnly(id, shape, "type", "half_extent", "radius");
        String type = requiredString(id, shape, "type");
        return parseShapeType(id, type, shape);
    }

    private static ParticleEmitterShape parseShapeType(Identifier id, String type, JsonObject shape) {
        return switch (type) {
            case "point" -> ParticleEmitterShape.point();
            case "box" -> {
                if (shape == null) {
                    throw invalid(id, "box emission shape requires half_extent");
                }
                yield ParticleEmitterShape.box(requiredVector(id, shape, "half_extent"));
            }
            case "sphere" -> {
                if (shape == null) {
                    throw invalid(id, "sphere emission shape requires radius");
                }
                yield ParticleEmitterShape.sphere(requiredNumber(id, shape, "radius"));
            }
            default -> throw invalid(id, "unsupported emitter shape '" + type + "'");
        };
    }

    private static ParticleRange parseParticleLifetime(Identifier id, JsonObject particle) {
        requireOnly(id, particle, "lifetime", "position", "velocity", "acceleration", "color", "size", "texture");
        return range(id, particle, "lifetime");
    }

    private static ParticleColor[] parseColors(Identifier id, JsonObject particle) {
        JsonElement element = required(id, particle, "color");
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            ParticleColor color = color(id, element.getAsString());
            return new ParticleColor[] {color, color};
        }
        JsonObject color = object(id, element, "color");
        requireOnly(id, color, "start", "end");
        ParticleColor start = color(id, requiredString(id, color, "start"));
        return new ParticleColor[] {start, color.has("end") ? color(id, requiredString(id, color, "end")) : start};
    }

    private static ParticleRange[] parseSizes(Identifier id, JsonObject particle) {
        JsonElement element = required(id, particle, "size");
        if (element.isJsonPrimitive() || element.isJsonArray()) {
            ParticleRange size = range(id, element, "size");
            return new ParticleRange[] {size, size};
        }
        JsonObject size = object(id, element, "size");
        requireOnly(id, size, "start", "end");
        ParticleRange start = range(id, size, "start");
        return new ParticleRange[] {start, size.has("end") ? range(id, size, "end") : start};
    }

    private static Optional<Identifier> optionalIdentifier(Identifier id, JsonObject object, String name) {
        if (!object.has(name)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Identifier.parse(requiredString(id, object, name)));
        } catch (RuntimeException exception) {
            throw invalid(id, name + " must be a valid namespaced identifier");
        }
    }

    private static ParticleVector optionalVector(Identifier id, JsonObject object, String name, ParticleVector fallback) {
        return object.has(name) ? requiredVector(id, object, name) : fallback;
    }

    private static ParticleVector requiredVector(Identifier id, JsonObject object, String name) {
        JsonElement element = required(id, object, name);
        if (!element.isJsonArray()) {
            throw invalid(id, name + " must be a three-number array");
        }
        JsonArray values = element.getAsJsonArray();
        if (values.size() != 3) {
            throw invalid(id, name + " must contain exactly three numbers");
        }
        return new ParticleVector(number(id, values.get(0), name), number(id, values.get(1), name), number(id, values.get(2), name));
    }

    private static ParticleRange range(Identifier id, JsonObject object, String name) {
        return range(id, required(id, object, name), name);
    }

    private static ParticleRange range(Identifier id, JsonElement element, String name) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return ParticleRange.fixed(number(id, element, name));
        }
        if (!element.isJsonArray()) {
            throw invalid(id, name + " must be a number or a two-number array");
        }
        JsonArray values = element.getAsJsonArray();
        if (values.size() != 2) {
            throw invalid(id, name + " ranges must contain exactly two numbers");
        }
        return new ParticleRange(number(id, values.get(0), name), number(id, values.get(1), name));
    }

    private static ParticleColor color(Identifier id, String value) {
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (normalized.length() != 6 && normalized.length() != 8) {
            throw invalid(id, "colors must use #RRGGBB or #RRGGBBAA");
        }
        try {
            long encoded = Long.parseLong(normalized, 16);
            int rgb = normalized.length() == 6 ? (int) encoded : (int) (encoded >>> 8);
            int alpha = normalized.length() == 6 ? 0xFF : (int) encoded & 0xFF;
            return ParticleColor.fromArgb((alpha << 24) | rgb);
        } catch (NumberFormatException exception) {
            throw invalid(id, "invalid color '" + value + "'");
        }
    }

    private static JsonObject requiredObject(Identifier id, JsonObject object, String name) {
        return object(id, required(id, object, name), name);
    }

    private static JsonObject object(Identifier id, JsonElement element, String name) {
        if (!element.isJsonObject()) {
            throw invalid(id, name + " must be an object");
        }
        return element.getAsJsonObject();
    }

    private static JsonElement required(Identifier id, JsonObject object, String name) {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            throw invalid(id, "missing required field '" + name + "'");
        }
        return object.get(name);
    }

    private static String requiredString(Identifier id, JsonObject object, String name) {
        JsonElement element = required(id, object, name);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw invalid(id, name + " must be a string");
        }
        return element.getAsString();
    }

    private static int requiredInt(Identifier id, JsonObject object, String name) {
        double value = number(id, required(id, object, name), name);
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw invalid(id, name + " must be an integer");
        }
        return (int) value;
    }

    private static double requiredNumber(Identifier id, JsonObject object, String name) {
        return number(id, required(id, object, name), name);
    }

    private static double number(Identifier id, JsonElement element, String name) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw invalid(id, name + " must be a number");
        }
        double value = element.getAsDouble();
        if (!Double.isFinite(value)) {
            throw invalid(id, name + " must be finite");
        }
        return value;
    }

    private static void requireOnly(Identifier id, JsonObject object, String... allowed) {
        for (String key : object.keySet()) {
            boolean supported = false;
            for (String name : allowed) {
                if (name.equals(key)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw invalid(id, "unsupported field '" + key + "'");
            }
        }
    }

    private static ParticleDefinitionException invalid(Identifier id, String message) {
        return new ParticleDefinitionException("Invalid particle definition " + id + ": " + message);
    }
}
