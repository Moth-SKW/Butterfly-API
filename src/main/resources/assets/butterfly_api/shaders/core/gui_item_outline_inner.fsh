#version 330
${shader_preamble}

${shader_dynamic_transforms}

uniform sampler2D Sampler0;

${shader_gui_vertex_color_input} vec4 vertexColor;
${shader_gui_texture_input} vec2 texCoord0;

${shader_output} vec4 fragColor;

#ifndef BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS
#define BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS 1
#endif

const int ITEM_SIZE = 16;

float alphaAt(ivec2 atlasPixel, ivec2 localPixel) {
    if (localPixel.x < 0 || localPixel.x >= ITEM_SIZE || localPixel.y < 0 || localPixel.y >= ITEM_SIZE) {
        return 0.0;
    }
    return texelFetch(Sampler0, atlasPixel + (localPixel - ivec2(
            atlasPixel.x % ITEM_SIZE,
            atlasPixel.y % ITEM_SIZE
    )), 0).a;
}

void main() {
    ivec2 atlasSize = textureSize(Sampler0, 0);
    ivec2 atlasPixel = ivec2(floor(texCoord0 * vec2(atlasSize)));
    ivec2 localPixel = ivec2(atlasPixel.x % ITEM_SIZE, atlasPixel.y % ITEM_SIZE);
    float centerAlpha = texelFetch(Sampler0, atlasPixel, 0).a;
    if (centerAlpha < 0.1) {
        discard;
    }

    bool edge = false;
    for (int offsetX = -BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS;
            offsetX <= BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS;
            offsetX++) {
        for (int offsetY = -BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS;
                offsetY <= BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS;
                offsetY++) {
            if (offsetX * offsetX + offsetY * offsetY
                    > BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS * BUTTERFLY_API_GUI_ITEM_OUTLINE_THICKNESS) {
                continue;
            }
            if (alphaAt(atlasPixel, localPixel + ivec2(offsetX, offsetY)) < 0.1) {
                edge = true;
            }
        }
    }
    if (!edge) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb * centerAlpha, vertexColor.a * centerAlpha);
}
