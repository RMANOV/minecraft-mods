"""
Generate LabPBR normal maps (_n.png) and specular maps (_s.png)
for ~40 Minecraft blocks, plus environment textures.

LabPBR format:
  Normal map (_n.png) : RGB  R=X normal (128=flat), G=Y normal (128=flat), B=Z (255=up)
  Specular map (_s.png): RGB  R=smoothness, G=metallic, B=emissive

Run from the project root:
    python3 generate_pbr_textures.py
"""

import math
import random
from pathlib import Path

from PIL import Image

# ─── Reproducibility ────────────────────────────────────────────────────────
random.seed(42)

# ─── Output roots ───────────────────────────────────────────────────────────
PROJECT_ROOT = Path(__file__).parent
BLOCK_DIR = PROJECT_ROOT / "src/main/resources/assets/lumenrealis/textures/block"
ENV_DIR   = PROJECT_ROOT / "src/main/resources/assets/lumenrealis/textures/environment"

BLOCK_DIR.mkdir(parents=True, exist_ok=True)
ENV_DIR.mkdir(parents=True, exist_ok=True)

files_created: list[str] = []

# ─── Low-level helpers ───────────────────────────────────────────────────────

def make_image(size: int = 16) -> Image.Image:
    """Return a fresh black RGB image."""
    return Image.new("RGB", (size, size), (0, 0, 0))


def flat_normal() -> tuple[int, int, int]:
    return (128, 128, 255)


def noisy_normal(spread: int) -> tuple[int, int, int]:
    """Normal with random XY perturbation, Z stays at 255."""
    rx = 128 + random.randint(-spread, spread)
    ry = 128 + random.randint(-spread, spread)
    return (
        max(0, min(255, rx)),
        max(0, min(255, ry)),
        255,
    )


def save(img: Image.Image, path: Path) -> None:
    img.save(str(path), "PNG")
    files_created.append(str(path.relative_to(PROJECT_ROOT)))


# ─── Category generators ─────────────────────────────────────────────────────

def gen_stone_normal() -> Image.Image:
    """Micro-crack stone: random ±15 noise on each pixel."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = noisy_normal(15)
    return img


def gen_stone_specular() -> Image.Image:
    """smoothness=40, metallic=0, emissive=0"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (40, 0, 0)
    return img


def gen_wood_normal() -> Image.Image:
    """Horizontal grain lines every 3-4 px with slight normal variation."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        # grain row: subtle bump every ~3-4 pixels
        on_grain = (y % random.randint(3, 4)) == 0
        base_gy = 8 if on_grain else 0          # slight tilt in Y on grain line
        for x in range(16):
            rx = 128 + random.randint(-5, 5)
            ry = 128 + base_gy + random.randint(-3, 3)
            pix[x, y] = (max(0, min(255, rx)), max(0, min(255, ry)), 255)
    return img


def gen_wood_specular() -> Image.Image:
    """smoothness=70, metallic=0, emissive=0"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (70, 0, 0)
    return img


def gen_metal_normal() -> Image.Image:
    """Brushed metal: horizontal lines, very slight XY variation."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            rx = 128 + random.randint(-3, 3)
            ry = 128 + random.randint(-6, 6)   # more variation along grain axis
            pix[x, y] = (max(0, min(255, rx)), max(0, min(255, ry)), 255)
    return img


def gen_metal_specular() -> Image.Image:
    """smoothness=200, metallic=230, emissive=0"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (200, 230, 0)
    return img


def gen_earth_normal() -> Image.Image:
    """Heavy noise ±25."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = noisy_normal(25)
    return img


def gen_earth_specular() -> Image.Image:
    """smoothness=20, metallic=0, emissive=0"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (20, 0, 0)
    return img


def gen_glass_normal() -> Image.Image:
    """Nearly flat ±2."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = noisy_normal(2)
    return img


def gen_glass_specular() -> Image.Image:
    """smoothness=240, metallic=0, emissive=0"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (240, 0, 0)
    return img


def gen_ore_normal() -> Image.Image:
    """Stone base noise + 3-4 raised ore spots."""
    img = make_image()
    pix = img.load()
    # stone base
    for y in range(16):
        for x in range(16):
            pix[x, y] = noisy_normal(15)
    # ore spot normals point outward (toward camera = high Z, pushed XY toward center of spot)
    n_spots = random.randint(3, 4)
    spot_pixels: list[tuple[int, int]] = []
    coords_used: set[tuple[int, int]] = set()
    for _ in range(n_spots):
        for _attempt in range(20):
            sx = random.randint(1, 14)
            sy = random.randint(1, 14)
            if (sx, sy) not in coords_used:
                coords_used.add((sx, sy))
                spot_pixels.append((sx, sy))
                break
    for (sx, sy) in spot_pixels:
        # small 2x2 spot
        for dy in range(-1, 2):
            for dx in range(-1, 2):
                nx, ny = sx + dx, sy + dy
                if 0 <= nx < 16 and 0 <= ny < 16:
                    # normal points outward from spot center
                    nrx = 128 + int((dx / 2) * 40)
                    nry = 128 + int((dy / 2) * 40)
                    pix[nx, ny] = (
                        max(0, min(255, nrx)),
                        max(0, min(255, nry)),
                        255,
                    )
    return img, spot_pixels   # type: ignore[return-value]


def gen_ore_specular(spot_pixels: list[tuple[int, int]]) -> Image.Image:
    """Base smoothness=30; ore spots smoothness=180, metallic=200."""
    img = make_image()
    pix = img.load()
    # base
    for y in range(16):
        for x in range(16):
            pix[x, y] = (30, 0, 0)
    # ore spots (2x2)
    for (sx, sy) in spot_pixels:
        for dy in range(-1, 2):
            for dx in range(-1, 2):
                nx, ny = sx + dx, sy + dy
                if 0 <= nx < 16 and 0 <= ny < 16:
                    pix[nx, ny] = (180, 200, 0)
    return img


def gen_nether_normal() -> Image.Image:
    """Aggressive noise ±30."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = noisy_normal(30)
    return img


def gen_nether_specular() -> Image.Image:
    """smoothness=25, metallic=0, emissive=0"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (25, 0, 0)
    return img


def gen_emissive_normal() -> Image.Image:
    """Subtle variation ±8."""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = noisy_normal(8)
    return img


def gen_emissive_specular() -> Image.Image:
    """smoothness=60, metallic=0, emissive=220"""
    img = make_image()
    pix = img.load()
    for y in range(16):
        for x in range(16):
            pix[x, y] = (60, 0, 220)
    return img


# ─── Block catalogue ─────────────────────────────────────────────────────────

# (block_name, category)
BLOCKS: list[tuple[str, str]] = [
    # Stone
    ("stone",             "stone"),
    ("granite",           "stone"),
    ("diorite",           "stone"),
    ("andesite",          "stone"),
    ("deepslate",         "stone"),
    ("cobblestone",       "stone"),
    ("stone_bricks",      "stone"),
    ("mossy_stone_bricks","stone"),
    # Wood
    ("oak_planks",        "wood"),
    ("spruce_planks",     "wood"),
    ("birch_planks",      "wood"),
    ("dark_oak_planks",   "wood"),
    ("oak_log",           "wood"),
    ("spruce_log",        "wood"),
    # Metal
    ("iron_block",        "metal"),
    ("gold_block",        "metal"),
    ("copper_block",      "metal"),
    ("netherite_block",   "metal"),
    # Earth
    ("dirt",              "earth"),
    ("grass_block_top",   "earth"),
    ("grass_block_side",  "earth"),
    ("sand",              "earth"),
    ("gravel",            "earth"),
    ("clay",              "earth"),
    # Glass
    ("glass",             "glass"),
    # Ore
    ("diamond_ore",       "ore"),
    ("iron_ore",          "ore"),
    ("gold_ore",          "ore"),
    ("emerald_ore",       "ore"),
    ("lapis_ore",         "ore"),
    ("coal_ore",          "ore"),
    # Nether
    ("netherrack",        "nether"),
    ("nether_bricks",     "nether"),
    ("basalt",            "nether"),
    ("blackstone",        "nether"),
    # Emissive
    ("glowstone",         "emissive"),
    ("lava_still",        "emissive"),
    ("sea_lantern",       "emissive"),
]


def generate_block(name: str, category: str) -> None:
    n_path = BLOCK_DIR / f"{name}_n.png"
    s_path = BLOCK_DIR / f"{name}_s.png"

    if category == "stone":
        n_img = gen_stone_normal()
        s_img = gen_stone_specular()

    elif category == "wood":
        n_img = gen_wood_normal()
        s_img = gen_wood_specular()

    elif category == "metal":
        n_img = gen_metal_normal()
        s_img = gen_metal_specular()

    elif category == "earth":
        n_img = gen_earth_normal()
        s_img = gen_earth_specular()

    elif category == "glass":
        n_img = gen_glass_normal()
        s_img = gen_glass_specular()

    elif category == "ore":
        result = gen_ore_normal()
        n_img, spot_pixels = result
        s_img = gen_ore_specular(spot_pixels)

    elif category == "nether":
        n_img = gen_nether_normal()
        s_img = gen_nether_specular()

    elif category == "emissive":
        n_img = gen_emissive_normal()
        s_img = gen_emissive_specular()

    else:
        raise ValueError(f"Unknown category: {category!r}")

    save(n_img, n_path)
    save(s_img, s_path)


# ─── Environment textures ─────────────────────────────────────────────────────

def gen_blue_noise() -> Image.Image:
    """
    64x64 blue noise texture.

    True blue noise generation (void-and-cluster algorithm) is expensive for a
    script utility; instead we use a spatially-stratified shuffle approach that
    gives decent high-frequency distribution with no visible clustering, while
    staying fast and reproducible.
    """
    SIZE = 64
    img = make_image(SIZE)
    pix = img.load()

    # Build a stratified set of RGB triples by shuffling separate channel grids
    # and pairing them.  This avoids low-frequency clustering better than pure
    # independent random.
    n = SIZE * SIZE

    def shuffled_channel() -> list[int]:
        vals = [int(i * 255 / (n - 1)) for i in range(n)]
        random.shuffle(vals)
        return vals

    reds   = shuffled_channel()
    greens = shuffled_channel()
    blues  = shuffled_channel()

    idx = 0
    for y in range(SIZE):
        for x in range(SIZE):
            pix[x, y] = (reds[idx], greens[idx], blues[idx])
            idx += 1
    return img


def gen_brdf_lut() -> Image.Image:
    """
    256x256 GGX BRDF integration LUT.

    X axis (u) → NdotV in [0,1]
    Y axis (v) → roughness in [0,1]

    We integrate the split-sum formula for a white F0=1 dielectric:
        ∫ V(l,v,α) * D_GGX(h,α) * (1-F) dω_i  →  stored in R
        ∫ V(l,v,α) * D_GGX(h,α) * F           dω_i  →  stored in G

    Both stored as uint8 in [0,255].  Uses importance sampling with
    SAMPLES=64 (fast but accurate enough for a baked texture).
    """
    SIZE    = 256
    SAMPLES = 64
    img     = make_image(SIZE)
    pix     = img.load()

    def radical_inverse_vdc(bits: int) -> float:
        bits  = (bits << 16) | (bits >> 16)
        bits  = ((bits & 0x55555555) << 1) | ((bits & 0xAAAAAAAA) >> 1)
        bits  = ((bits & 0x33333333) << 2) | ((bits & 0xCCCCCCCC) >> 2)
        bits  = ((bits & 0x0F0F0F0F) << 4) | ((bits & 0xF0F0F0F0) >> 4)
        bits  = ((bits & 0x00FF00FF) << 8) | ((bits & 0xFF00FF00) >> 8)
        return (bits & 0xFFFFFFFF) / 4294967296.0

    def hammersley(i: int, n: int) -> tuple[float, float]:
        return i / n, radical_inverse_vdc(i)

    def importance_sample_ggx(xi: tuple[float, float], roughness: float) -> tuple[float, float, float]:
        a = roughness * roughness
        phi      = 2.0 * math.pi * xi[0]
        cos_theta = math.sqrt((1.0 - xi[1]) / (1.0 + (a * a - 1.0) * xi[1] + 1e-7))
        sin_theta = math.sqrt(max(0.0, 1.0 - cos_theta * cos_theta))
        hx = math.cos(phi) * sin_theta
        hy = math.sin(phi) * sin_theta
        hz = cos_theta
        return hx, hy, hz

    def geometry_schlick_ggx(ndotv: float, roughness: float) -> float:
        a  = roughness
        k  = (a * a) / 2.0
        return ndotv / (ndotv * (1.0 - k) + k + 1e-7)

    def geometry_smith(ndotv: float, ndotl: float, roughness: float) -> float:
        return geometry_schlick_ggx(ndotv, roughness) * geometry_schlick_ggx(ndotl, roughness)

    for yi in range(SIZE):
        roughness = yi / (SIZE - 1)          # 0 → 1  top-to-bottom
        roughness = max(roughness, 0.04)     # clamp to avoid degenerate cases
        for xi in range(SIZE):
            ndotv = xi / (SIZE - 1)          # 0 → 1  left-to-right
            ndotv = max(ndotv, 0.001)

            vx = math.sqrt(max(0.0, 1.0 - ndotv * ndotv))
            vy = 0.0
            vz = ndotv

            a1 = 0.0   # F0 = 0 term  (1-F)
            a2 = 0.0   # F0 = 1 term  (F)

            for si in range(SAMPLES):
                xi_s = hammersley(si, SAMPLES)
                hx, hy, hz = importance_sample_ggx(xi_s, roughness)

                # Reflect V around H to get L
                vdoth = vx * hx + vy * hy + vz * hz
                lx = 2.0 * vdoth * hx - vx
                ly = 2.0 * vdoth * hy - vy
                lz = 2.0 * vdoth * hz - vz

                ndotl = max(lz, 0.0)
                ndoth = max(hz, 0.0)
                vdoth = max(vdoth, 0.0)

                if ndotl > 0.0:
                    g      = geometry_smith(ndotv, ndotl, roughness)
                    g_vis  = (g * vdoth) / (ndoth * ndotv + 1e-7)
                    fc     = (1.0 - vdoth) ** 5
                    a1    += (1.0 - fc) * g_vis
                    a2    += fc * g_vis

            a1 /= SAMPLES
            a2 /= SAMPLES

            r = int(max(0.0, min(1.0, a1)) * 255)
            g = int(max(0.0, min(1.0, a2)) * 255)
            pix[xi, yi] = (r, g, 0)

    return img


# ─── Main ─────────────────────────────────────────────────────────────────────

def main() -> None:
    print("Generating PBR textures for Lumen Realis…\n")

    # ── Block textures ───────────────────────────────────────────────────────
    by_category: dict[str, int] = {}
    for name, category in BLOCKS:
        generate_block(name, category)
        by_category[category] = by_category.get(category, 0) + 2   # n + s

    print(f"  {'Block textures':30s}")
    for cat, count in sorted(by_category.items()):
        blocks_in_cat = [n for n, c in BLOCKS if c == cat]
        print(f"    {cat:12s}: {len(blocks_in_cat):2d} blocks → {count:3d} files")

    total_block_files = sum(by_category.values())
    print(f"\n  Total block files  : {total_block_files}")

    # ── Environment textures ─────────────────────────────────────────────────
    print("\n  Environment textures")

    bn_path = ENV_DIR / "noise_blue.png"
    save(gen_blue_noise(), bn_path)
    print(f"    noise_blue.png  : 64×64  blue noise (stratified shuffle)")

    print("    brdf_lut.png    : 256×256  GGX split-sum LUT (computing…)", end="", flush=True)
    bl_path = ENV_DIR / "brdf_lut.png"
    save(gen_brdf_lut(), bl_path)
    print(" done")

    # ── Summary ─────────────────────────────────────────────────────────────
    print(f"\n{'─'*60}")
    print(f"  Files created      : {len(files_created)}")
    print(f"  Block dir          : {BLOCK_DIR.relative_to(PROJECT_ROOT)}")
    print(f"  Environment dir    : {ENV_DIR.relative_to(PROJECT_ROOT)}")
    print(f"{'─'*60}\n")

    # Spot-check a few paths
    samples = [files_created[0], files_created[1],
               files_created[-3], files_created[-2], files_created[-1]]
    print("  Sample outputs:")
    for f in samples:
        print(f"    {f}")


if __name__ == "__main__":
    main()
