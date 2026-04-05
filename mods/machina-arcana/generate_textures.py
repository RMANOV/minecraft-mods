#!/usr/bin/env python3
"""
Generates all 16x16 (and 8x8) textures for Machina Arcana using PIL.
Run: python3 generate_textures.py
"""

from PIL import Image, ImageDraw
import os

BASE = "src/main/resources/assets/machinaarcana/textures"
BLOCK = f"{BASE}/block"
ITEM  = f"{BASE}/item"
PARTICLE = f"{BASE}/particle"

os.makedirs(BLOCK,    exist_ok=True)
os.makedirs(ITEM,     exist_ok=True)
os.makedirs(PARTICLE, exist_ok=True)

# ── Helper ─────────────────────────────────────────────────────────────────

def new16():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))

def new8():
    return Image.new('RGBA', (8, 8), (0, 0, 0, 0))

def save(img, path):
    img.save(path)
    print(f"  Written: {path}")

# ── assembler_frame.png ────────────────────────────────────────────────────
# Dark stone brick with arcane rune lines (purple tinge)

img = new16()
px  = img.load()

# Base: dark grey stone
for y in range(16):
    for x in range(16):
        # brick pattern
        if y < 8:
            if x < 8:
                px[x, y] = (55, 50, 65, 255)   # upper-left brick
            else:
                px[x, y] = (50, 45, 60, 255)   # upper-right brick
        else:
            if x < 8:
                px[x, y] = (50, 45, 60, 255)   # lower-left brick
            else:
                px[x, y] = (55, 50, 65, 255)   # lower-right brick

# Mortar lines (darker)
for x in range(16):
    px[x, 0]  = (30, 28, 38, 255)
    px[x, 8]  = (30, 28, 38, 255)
    px[x, 15] = (30, 28, 38, 255)
for y in range(8):
    px[0,  y] = (30, 28, 38, 255)
    px[15, y] = (30, 28, 38, 255)
for y in range(8, 16):
    px[8,  y] = (30, 28, 38, 255)
    px[15, y] = (30, 28, 38, 255)

# Arcane rune lines (glowing purple)
rune_color = (140, 80, 200, 200)
for x in range(2, 7, 2):
    px[x, 3] = rune_color
    px[x, 4] = rune_color
for x in range(9, 14, 2):
    px[x, 11] = rune_color
    px[x, 12] = rune_color

save(img, f"{BLOCK}/assembler_frame.png")

# ── assembler_core.png ─────────────────────────────────────────────────────
# Glowing center block — purple/blue pulsing core

img = new16()
px  = img.load()

for y in range(16):
    for x in range(16):
        # Distance from center
        cx, cy = 7.5, 7.5
        dist = ((x - cx)**2 + (y - cy)**2) ** 0.5
        # Inner glow: bright purple-white
        if dist < 3:
            t = 1.0 - dist / 3.0
            r = int(180 + t * 75)
            g = int(100 + t * 80)
            b = int(220 + t * 35)
            px[x, y] = (min(255, r), min(255, g), min(255, b), 255)
        elif dist < 6:
            t = 1.0 - (dist - 3) / 3.0
            px[x, y] = (int(120 * t + 40), int(60 * t + 30), int(180 * t + 60), 255)
        else:
            # Outer ring: dark with purple tint
            px[x, y] = (35, 25, 55, 255)

# Border
for x in range(16):
    px[x, 0]  = (20, 15, 40, 255)
    px[x, 15] = (20, 15, 40, 255)
for y in range(16):
    px[0,  y] = (20, 15, 40, 255)
    px[15, y] = (20, 15, 40, 255)

save(img, f"{BLOCK}/assembler_core.png")

# ── mana_conduit.png ───────────────────────────────────────────────────────
# Pipe-like with glowing center

img = new16()
px  = img.load()

for y in range(16):
    for x in range(16):
        # Pipe body: grey metallic
        if 1 <= x <= 14 and 1 <= y <= 14:
            # glowing channel down the center
            if 6 <= x <= 9 and 3 <= y <= 12:
                blue_t = 1.0 - (abs(x - 7.5) / 2.0)
                b = int(180 + blue_t * 75)
                g = int(80 + blue_t * 60)
                px[x, y] = (40, g, min(255, b), 255)
            else:
                # metallic dark grey
                shade = 70 + (x + y) % 20
                px[x, y] = (shade, shade - 5, shade + 10, 255)
        else:
            px[x, y] = (25, 25, 35, 255)  # dark border

save(img, f"{BLOCK}/mana_conduit.png")

# ── mana_crystal.png ──────────────────────────────────────────────────────
# Blue/purple crystal shape

img = new16()
px  = img.load()

# Crystal outline (diamond-ish shape)
crystal_pixels = set()
for y in range(16):
    for x in range(16):
        # Two triangles forming a vertical crystal
        # Upper half: narrows toward top
        if y < 8:
            margin = y // 2
            if margin <= x <= 15 - margin:
                crystal_pixels.add((x, y))
        else:
            # Lower half: narrows toward bottom
            margin = (15 - y) // 2
            if margin <= x <= 15 - margin:
                crystal_pixels.add((x, y))

for (x, y) in crystal_pixels:
    cx, cy = 7.5, 7.5
    dist = ((x - cx)**2 + (y - cy)**2) ** 0.5
    # Gradient: bright blue-white center, deep purple edges
    t = max(0.0, 1.0 - dist / 7.0)
    r = int(60  + t * 120)
    g = int(40  + t * 100)
    b = int(180 + t * 75)
    px[x, y] = (min(255, r), min(255, g), min(255, b), 255)

# Crystal edge highlight
for (x, y) in crystal_pixels:
    # Check if any neighbor is NOT in crystal_pixels
    is_edge = any((x+dx, y+dy) not in crystal_pixels
                  for dx, dy in [(-1,0),(1,0),(0,-1),(0,1)])
    if is_edge:
        # Brighten edge
        r, g, b, a = px[x, y]
        px[x, y] = (min(255, r + 60), min(255, g + 50), min(255, b + 40), 255)

save(img, f"{ITEM}/mana_crystal.png")

# ── arcane_ingot.png ──────────────────────────────────────────────────────
# Dark metallic ingot with purple tinge (shaped like a standard ingot)

img = new16()
px  = img.load()

# Ingot shape: roughly like vanilla gold ingot
ingot_rows = {
    2: (3, 12),
    3: (2, 13),
    4: (2, 13),
    5: (2, 13),
    6: (3, 12),
    7: (3, 12),
    8: (3, 12),
    9: (3, 12),
    10: (3, 12),
    11: (3, 12),
    12: (4, 11),
    13: (4, 11),
}

for y, (x_start, x_end) in ingot_rows.items():
    for x in range(x_start, x_end + 1):
        # Dark metallic purple-black gradient
        shade_base = 55 + (x - x_start) * 3
        r = max(0, min(255, shade_base + 10))
        g = max(0, min(255, shade_base - 10))
        b = max(0, min(255, shade_base + 35))
        px[x, y] = (r, g, b, 255)

# Top highlight (lighter row)
if 2 in ingot_rows:
    x0, x1 = ingot_rows[2]
    for x in range(x0, x1 + 1):
        px[x, 2] = (110, 80, 150, 255)

# Purple vein lines for arcane feel
for x in range(5, 11, 2):
    if 5 <= x <= 10:
        for y in [5, 6, 8, 9]:
            if y in ingot_rows:
                x0, x1 = ingot_rows[y]
                if x0 <= x <= x1:
                    r, g, b, a = px[x, y]
                    px[x, y] = (min(255, r + 40), g, min(255, b + 60), 255)

save(img, f"{ITEM}/arcane_ingot.png")

# ── particle/mana_stream.png ──────────────────────────────────────────────
# 8x8 blue glow sprite

img = new8()
px  = img.load()

for y in range(8):
    for x in range(8):
        cx, cy = 3.5, 3.5
        dist = ((x - cx)**2 + (y - cy)**2) ** 0.5
        if dist < 3.5:
            t = 1.0 - dist / 3.5
            alpha = int(220 * t)
            r = int(80  * t)
            g = int(60  * t)
            b = int(220 * t)
            px[x, y] = (r, g, b, alpha)
        else:
            px[x, y] = (0, 0, 0, 0)

save(img, f"{PARTICLE}/mana_stream.png")

# ── icon.png ──────────────────────────────────────────────────────────────
# 128x128 mod icon (upscaled arcane core)

ICON_DIR = "src/main/resources/assets/machinaarcana"
os.makedirs(ICON_DIR, exist_ok=True)

icon = Image.new('RGBA', (128, 128), (0, 0, 0, 255))
draw = ImageDraw.Draw(icon)

# Background gradient (dark)
for y in range(128):
    for x in range(128):
        shade = int(20 + (y / 128) * 15)
        icon.putpixel((x, y), (shade, shade - 5, shade + 20, 255))

# Central glowing orb
cx, cy = 64, 64
for y in range(128):
    for x in range(128):
        dist = ((x - cx)**2 + (y - cy)**2) ** 0.5
        if dist < 45:
            t = 1.0 - dist / 45.0
            r = int(100 + t * 155)
            g = int(50  + t * 80)
            b = int(200 + t * 55)
            # Blend over background
            existing = icon.getpixel((x, y))
            blend = int(t * 255)
            nr = (r * blend + existing[0] * (255 - blend)) // 255
            ng = (g * blend + existing[1] * (255 - blend)) // 255
            nb = (b * blend + existing[2] * (255 - blend)) // 255
            icon.putpixel((x, y), (min(255,nr), min(255,ng), min(255,nb), 255))

# Rune ring
for angle_deg in range(0, 360, 15):
    import math
    angle = math.radians(angle_deg)
    rx = int(cx + 50 * math.cos(angle))
    ry = int(cy + 50 * math.sin(angle))
    if 0 <= rx < 128 and 0 <= ry < 128:
        icon.putpixel((rx, ry), (180, 120, 255, 255))
        if 0 <= rx+1 < 128 and 0 <= ry+1 < 128:
            icon.putpixel((rx+1, ry), (180, 120, 255, 200))
            icon.putpixel((rx, ry+1), (180, 120, 255, 200))

save(icon, f"{ICON_DIR}/icon.png")

print("\nAll textures generated successfully!")
