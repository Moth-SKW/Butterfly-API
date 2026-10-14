#version 330
${shader_preamble}

${shader_vertex_color_input} vec4 vertexColor;

${shader_output} vec4 fragColor;

void main() {
    fragColor = vertexColor;
}
