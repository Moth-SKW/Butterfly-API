#version 330
${shader_preamble}

${shader_dynamic_transforms}

uniform sampler2D Sampler0;

${shader_gui_vertex_color_input} vec4 vertexColor;
${shader_gui_texture_input} vec2 texCoord0;

${shader_output} vec4 fragColor;

void main() {
    float alpha = texture(Sampler0, texCoord0).a;
    if (alpha < 0.1) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb * alpha, vertexColor.a * alpha);
}
