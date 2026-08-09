#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }

    // Vanilla 1.21.11 uses ColorModulator.a here, which prevents custom
    // outline metadata from surviving the native outline pass. The packed
    // style is carried in the vertex alpha by OutlineVertexConsumerProvider.
    fragColor = vec4(ColorModulator.rgb * vertexColor.rgb, vertexColor.a);
}
