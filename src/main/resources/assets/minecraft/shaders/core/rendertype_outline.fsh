#version 330
${shader_preamble}

${shader_dynamic_transforms}

uniform sampler2D Sampler0;

${shader_vertex_color_input} vec4 vertexColor;
${shader_texture_input} vec2 texCoord0;

${shader_output} vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = vec4(ColorModulator.rgb * vertexColor.rgb, vertexColor.a);
}
