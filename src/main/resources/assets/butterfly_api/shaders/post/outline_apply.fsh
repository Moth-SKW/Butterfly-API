#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

uniform sampler2D FieldSampler;
uniform sampler2D MaskSampler;

in vec2 texCoord;
out vec4 fragColor;

ivec2 decodeCoordinate(vec4 encodedPixel) {
    ivec4 bytes = ivec4(round(encodedPixel * 255.0));
    if (all(equal(bytes, ivec4(0)))) {
        return ivec2(-1);
    }
    return ivec2(bytes.x | (bytes.y << 8), bytes.z | (bytes.w << 8)) - ivec2(1);
}

bool inBounds(ivec2 pixel) {
    ivec2 size = ivec2(InSize);
    return all(greaterThanEqual(pixel, ivec2(0)))
        && all(lessThan(pixel, size));
}

vec4 maskAt(ivec2 pixel) {
    return inBounds(pixel) ? texelFetch(MaskSampler, pixel, 0) : vec4(0.0);
}

float coverageForSide(bool inside, float radius, float distanceToBoundary) {
    if (radius <= 0.0) {
        return 0.0;
    }
    return inside
        ? clamp(radius - distanceToBoundary, 0.0, 1.0)
        : clamp(radius - distanceToBoundary + 1.0, 0.0, 1.0);
}

int componentSign(int value) {
    if (value < 0) {
        return -1;
    }
    if (value > 0) {
        return 1;
    }
    return 0;
}

bool maskFilled(ivec2 pixel) {
    return maskAt(pixel).a > 0.5;
}

int packedStyleBits(vec4 mask) {
    ivec3 bytes = ivec3(round(mask.rgb * 255.0));
    return ((bytes.r & 15) << 8) | ((bytes.g & 15) << 4) | (bytes.b & 15);
}

bool customStyle(vec4 mask) {
    int alpha = int(round(mask.a * 255.0));
    return alpha == 254 && (packedStyleBits(mask) & 2048) != 0;
}

float outlineThickness(vec4 mask) {
    return customStyle(mask) ? float((packedStyleBits(mask) & 63) + 1) : 2.0;
}

int outlinePlacement(vec4 mask) {
    return customStyle(mask) ? int((packedStyleBits(mask) >> 6) & 3) : 0;
}

bool hasBlackBorder(vec4 mask) {
    return customStyle(mask) && (packedStyleBits(mask) & 256) != 0;
}

bool preserveInsideInterior(vec4 mask) {
    return customStyle(mask) && (packedStyleBits(mask) & 512) != 0;
}

vec3 visibleOutlineColor(vec4 mask) {
    if (!customStyle(mask)) {
        return mask.rgb;
    }

    ivec3 bytes = ivec3(round(mask.rgb * 255.0));
    return vec3(
        float((bytes.r & 240) | 15),
        float((bytes.g & 240) | 15),
        float((bytes.b & 240) | 15)
    ) / 255.0;
}

float outputAlpha(float alpha, bool custom) {
    return custom ? min(alpha, 253.0 / 255.0) : alpha;
}

bool hasInteriorPastOutline(ivec2 pixel, ivec2 boundaryPixel, float radius) {
    int stepSize = int(ceil(radius)) + 1;
    ivec2 delta = pixel - boundaryPixel;
    ivec2 direction = ivec2(componentSign(delta.x), componentSign(delta.y));
    if (direction != ivec2(0)) {
        return maskFilled(pixel + direction * stepSize);
    }

    return maskFilled(pixel + ivec2(stepSize, 0))
        || maskFilled(pixel - ivec2(stepSize, 0))
        || maskFilled(pixel + ivec2(0, stepSize))
        || maskFilled(pixel - ivec2(0, stepSize))
        || maskFilled(pixel + ivec2(stepSize, stepSize))
        || maskFilled(pixel + ivec2(stepSize, -stepSize))
        || maskFilled(pixel + ivec2(-stepSize, stepSize))
        || maskFilled(pixel - ivec2(stepSize, stepSize));
}

void main() {
    ivec2 pixel = clamp(ivec2(texCoord * InSize), ivec2(0), ivec2(InSize) - ivec2(1));
    vec4 destinationMask = maskAt(pixel);
    bool inside = destinationMask.a > 0.5;

    ivec2 boundary = decodeCoordinate(texelFetch(FieldSampler, pixel, 0));
    if (boundary.x < 0) {
        fragColor = vec4(0.0);
        return;
    }

    vec4 boundaryMask = maskAt(boundary);
    bool customBoundary = customStyle(boundaryMask);
    float thickness = outlineThickness(boundaryMask);
    int placement = outlinePlacement(boundaryMask);
    placement = min(2, placement);
    float distanceToBoundary = distance(vec2(pixel), vec2(boundary));
    float radius = 0.0;
    if (placement == 0) {
        radius = thickness * 0.5;
    } else if (placement == 1 && !inside) {
        radius = thickness;
    } else if (placement == 2 && inside) {
        radius = thickness;
    }
    float colorCoverage = coverageForSide(inside, radius, distanceToBoundary);
    if (inside
            && placement == 2
            && preserveInsideInterior(destinationMask)
            && colorCoverage > 0.0
            && !hasInteriorPastOutline(pixel, boundary, radius)) {
        colorCoverage = 0.0;
    }

    bool blackBorder = hasBlackBorder(boundaryMask);
    float borderCoverage = 0.0;
    if (blackBorder && !inside) {
        float outerRadius = placement == 2 ? 0.0 : radius;
        borderCoverage = max(
                coverageForSide(false, outerRadius + 1.0, distanceToBoundary)
                        - coverageForSide(false, outerRadius, distanceToBoundary),
                0.0
        );
    }

    vec3 outlineColor = inside ? visibleOutlineColor(destinationMask) : visibleOutlineColor(boundaryMask);
    if (!blackBorder) {
        if (colorCoverage <= 0.0) {
            fragColor = vec4(0.0);
            return;
        }
        fragColor = vec4(outlineColor, outputAlpha(colorCoverage, customBoundary));
        return;
    }

    float alpha = colorCoverage + borderCoverage * (1.0 - colorCoverage);
    if (alpha <= 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    vec3 color = outlineColor * colorCoverage / alpha;
    fragColor = vec4(color, outputAlpha(alpha, customBoundary));
}
