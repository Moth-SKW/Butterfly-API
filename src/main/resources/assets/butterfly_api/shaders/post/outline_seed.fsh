#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

bool inBounds(ivec2 pixel) {
    ivec2 size = ivec2(InSize);
    return all(greaterThanEqual(pixel, ivec2(0)))
        && all(lessThan(pixel, size));
}

int packedStyleBits(vec4 mask) {
    ivec3 bytes = ivec3(round(mask.rgb * 255.0));
    return ((bytes.r & 15) << 8) | ((bytes.g & 15) << 4) | (bytes.b & 15);
}

bool butterflyMask(vec4 mask) {
    int alpha = int(round(mask.a * 255.0));
    return alpha == 254 && (packedStyleBits(mask) & 2048) != 0;
}

bool vanillaMask(vec4 mask) {
    return int(round(mask.a * 255.0)) == 255;
}

bool filled(ivec2 pixel) {
    if (!inBounds(pixel)) {
        return false;
    }
    vec4 mask = texelFetch(InSampler, pixel, 0);
    return butterflyMask(mask) || vanillaMask(mask);
}

vec4 encodeCoordinate(ivec2 pixel) {
    int x = pixel.x + 1;
    int y = pixel.y + 1;
    return vec4(
        float(x & 255) / 255.0,
        float((x >> 8) & 255) / 255.0,
        float(y & 255) / 255.0,
        float((y >> 8) & 255) / 255.0
    );
}

void main() {
    ivec2 pixel = clamp(ivec2(texCoord * InSize), ivec2(0), ivec2(InSize) - ivec2(1));
    if (!filled(pixel)) {
        fragColor = vec4(0.0);
        return;
    }

    ivec2 size = ivec2(InSize);
    bool boundary = pixel.x == 0
        || pixel.y == 0
        || pixel.x == size.x - 1
        || pixel.y == size.y - 1;
    for (int y = -1; y <= 1 && !boundary; ++y) {
        for (int x = -1; x <= 1; ++x) {
            if ((x != 0 || y != 0) && !filled(pixel + ivec2(x, y))) {
                boundary = true;
                break;
            }
        }
    }

    fragColor = boundary ? encodeCoordinate(pixel) : vec4(0.0);
}
