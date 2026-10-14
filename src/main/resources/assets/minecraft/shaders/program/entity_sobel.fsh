#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main(){
    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 left = texture(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right = texture(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up = texture(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down = texture(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));
    float total = clamp(
        abs(center.a - left.a)
            + abs(center.a - right.a)
            + abs(center.a - up.a)
            + abs(center.a - down.a),
        0.0,
        1.0
    );
    vec4 color = center.a > 0.0 ? center
        : left.a > 0.0 ? left
        : right.a > 0.0 ? right
        : up.a > 0.0 ? up
        : down;
    fragColor = vec4(color.rgb, total);
}
