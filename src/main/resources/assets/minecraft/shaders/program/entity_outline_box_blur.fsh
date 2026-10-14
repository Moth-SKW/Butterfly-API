#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

void main() {
    vec3 colorSum = vec3(0.0);
    float alphaSum = 0.0;
    float weightSum = 0.0;
    const float radius = 2.0;

    for (float sampleOffset = -radius + 0.5; sampleOffset < radius; sampleOffset += 2.0) {
        vec4 sampled = texture(DiffuseSampler, texCoord + sampleStep * sampleOffset);
        colorSum += sampled.rgb * sampled.a;
        alphaSum += sampled.a;
        weightSum += 1.0;
    }

    vec4 edgeSample = texture(DiffuseSampler, texCoord + sampleStep * radius);
    colorSum += edgeSample.rgb * edgeSample.a * 0.5;
    alphaSum += edgeSample.a * 0.5;
    weightSum += 0.5;

    fragColor = alphaSum > 0.0
            ? vec4(colorSum / alphaSum, alphaSum)
            : vec4(0.0);
}
