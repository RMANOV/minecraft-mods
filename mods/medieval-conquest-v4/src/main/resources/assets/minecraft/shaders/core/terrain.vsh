#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp((uv / 256.0) + 0.5 / 16.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;

    // Wind animation for vegetation (biome-tinted blocks have green-dominant non-white Color)
    bool isVegetation = Color.g > Color.r && (Color.r < 0.95 || Color.b < 0.95);
    if (isVegetation) {
        vec3 worldPos = vec3(Position) + vec3(ChunkPosition);
        float blockFracY = fract(Position.y);

        // Cube blocks (leaves): axis-aligned normals -> subtle uniform sway
        // Cross-model plants (grass/fern): diagonal normals -> height-based sway
        bool isCubeBlock = abs(Normal.x) > 0.9 || abs(Normal.y) > 0.9 || abs(Normal.z) > 0.9;
        float heightFactor = isCubeBlock ? 0.25 : blockFracY;

        // GameTime cycles 0->1 in 1200s; multiply for desired wave frequency
        float time = GameTime * 2400.0;
        float phase = worldPos.x * 0.4 + worldPos.z * 0.3 + worldPos.y * 0.1;

        // Primary sway + secondary slower gust
        float windX = sin(time + phase) * 0.07 + sin(time * 0.4 + phase * 0.6) * 0.03;
        float windZ = cos(time * 0.7 + phase * 1.3) * 0.05 + cos(time * 0.3 + phase * 0.8) * 0.02;

        pos.x += windX * heightFactor;
        pos.z += windZ * heightFactor;
    }

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
}
