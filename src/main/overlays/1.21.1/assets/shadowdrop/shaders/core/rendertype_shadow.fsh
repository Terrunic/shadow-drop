#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    if (texColor.a == 0.0) {
        discard;
    }

    vec4 color = vec4(vertexColor.rgb * ColorModulator.rgb, texColor.a * vertexColor.a * ColorModulator.a);
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
