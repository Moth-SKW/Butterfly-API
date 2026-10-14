#version 330
${shader_preamble}

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform JumpConfig {
    float StepSize;
};

uniform sampler2D InSampler;

${shader_post_input} vec2 texCoord;
${shader_output} vec4 fragColor;

ivec2 decodeCoordinate(vec4 encodedPixel) {
    ivec4 bytes = ivec4(round(encodedPixel * 255.0));
    if (all(equal(bytes, ivec4(0)))) {
        return ivec2(-1);
    }
    return ivec2(bytes.x | (bytes.y << 8), bytes.z | (bytes.w << 8)) - ivec2(1);
}

vec4 candidateAt(ivec2 pixel) {
    ivec2 size = ivec2(InSize);
    if (any(lessThan(pixel, ivec2(0))) || any(greaterThanEqual(pixel, size))) {
        return vec4(0.0);
    }
    return texelFetch(InSampler, pixel, 0);
}

float squaredDistance(ivec2 first, ivec2 second) {
    vec2 delta = vec2(first - second);
    return dot(delta, delta);
}

void main() {
    ivec2 pixel = clamp(ivec2(texCoord * InSize), ivec2(0), ivec2(InSize) - ivec2(1));
    vec4 best = candidateAt(pixel);
    ivec2 bestCoordinate = decodeCoordinate(best);
    float bestDistance = bestCoordinate.x < 0
        ? 1.0e30
        : squaredDistance(pixel, bestCoordinate);

    int step = max(1, int(round(StepSize)));
    for (int y = -1; y <= 1; ++y) {
        for (int x = -1; x <= 1; ++x) {
            if (x == 0 && y == 0) {
                continue;
            }
            vec4 candidate = candidateAt(pixel + ivec2(x, y) * step);
            ivec2 coordinate = decodeCoordinate(candidate);
            if (coordinate.x < 0) {
                continue;
            }
            float candidateDistance = squaredDistance(pixel, coordinate);
            if (candidateDistance < bestDistance) {
                best = candidate;
                bestDistance = candidateDistance;
            }
        }
    }

    fragColor = best;
}
