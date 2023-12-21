#version 150

#moj_import <fog.glsl>
#moj_import <light.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec4 normal;
out float other;

bool isSlim() {
    vec3 color = texture(Sampler0, vec2(0.84375, 0.3125)).rgb;
    return dot(color, color) == 0.0;
}

float getY() {
    float y = UV0.y;
    if (y < 0.1875) {
        y *= 0.5;
    }

    return y;
}

vec2 bodyUV(int inner) {
    float x = UV0.x * 2 - inner;
    if (x < 0.375) {
        x /= 1.5;
    } else if (x > 0.625 && x < 0.875) {
        x /= 1.125;
    }

    float y = getY();
    return vec2(x * 0.375 + 0.25, y + (inner + 1) * 0.25);
}

vec2 baseArmUV(int inner, bool slim, int face) {
    float x = UV0.x * 2 - inner;
    if (slim) {
        if (x < 0.375) {
            x *= 0.25;
        } else if (x > 0.625 && x < 0.875) {
            if (face == 3) {
                x = 0.15625;
            } else {
                x = 0.171875;
            }
        } else {
            x *= 0.21875;
        }
    } else {
        x *= 0.25;
    }

    return vec2(x + 0.5, getY());
}

vec2 rightArmUV(int inner, bool slim, int face) {
    return baseArmUV(inner, slim, face) + vec2(0.125, (inner + 1) * 0.25);
}

vec2 leftArmUV(int inner, bool slim, int face) {
    return baseArmUV(inner, slim, face) + vec2(inner * 0.25, 0.75);
}

vec2 baseLegUV(int inner) {
    return vec2((UV0.x - inner * 0.5) * 0.5, getY());
}

vec2 rightLegUV(int inner) {
    return baseLegUV(inner) + vec2(0, (inner + 1) * 0.25);
}

vec2 leftLegUV(int inner) {
    return baseLegUV(inner) + vec2((1 - inner) * 0.25, 0.75);
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    other = 0;

    vec2 uv = UV0;
    ivec2 texSize = textureSize(Sampler0, 0);
    ivec2 iuv = ivec2(UV0 * texSize);
    if (texSize != vec2(64, 64)
        || (iuv.x < 8 && iuv.y < 8)
        || iuv % 4 != ivec2(0)
        || (iuv == ivec2(8, 8) && (
            // Fixes piglin heads with mods like Sodium.
            gl_VertexID % 384 == 4 || // Piglin
            gl_VertexID % 360 == 4    // Zombified Piglin
    ))) {
        other = 1;
    } else {
        int index = gl_VertexID / 48 % 6;
        int inner = gl_VertexID / 24 % 2;
        int face = gl_VertexID / 4 % 6;
        bool slim = isSlim();

        if (index == 1) {
            uv = bodyUV(inner);
        } else if (index == 2) {
            uv = rightArmUV(inner, slim, face);
        } else if (index == 3) {
            if (inner == 0 && UV0.x > 0.5) {
                other = 1;
            } else {
                uv = leftArmUV(inner, slim, face);
            }
        } else if (index == 4) {
            uv = rightLegUV(inner);
        } else if (index == 5) {
            uv = leftLegUV(inner);
        }
    }

    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = uv;
    texCoord1 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.);
}
