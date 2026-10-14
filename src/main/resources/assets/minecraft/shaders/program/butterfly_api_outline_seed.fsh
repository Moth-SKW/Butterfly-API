#version 150

uniform vec2 InSize;
uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

bool inBounds(ivec2 pixel) {
    ivec2 size = ivec2(InSize);
    return all(greaterThanEqual(pixel, ivec2(0))) && all(lessThan(pixel, size));
}

bool filled(ivec2 pixel) {
    if (!inBounds(pixel)) return false;
    int alpha = int(round(texelFetch(DiffuseSampler, pixel, 0).a * 255.0));
    return alpha > 127 && alpha != 253;
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
    bool boundary = pixel.x == 0 || pixel.y == 0 || pixel.x == size.x - 1 || pixel.y == size.y - 1;
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
