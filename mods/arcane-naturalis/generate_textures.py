#!/usr/bin/env python3
"""
Texture generator for Arcane Naturalis mod.
Run: python3 generate_textures.py
Requires: Pillow (pip install Pillow)
"""
from PIL import Image, ImageDraw
import os

BASE = "/home/rmanov/minecraft-mods/mods/arcane-naturalis/src/main/resources/assets/arcanenaturalis/textures"
os.makedirs(f"{BASE}/block", exist_ok=True)
os.makedirs(f"{BASE}/entity", exist_ok=True)
os.makedirs(f"{BASE}/particle", exist_ok=True)
os.makedirs(f"{BASE}/item", exist_ok=True)
ICON_PATH = "/home/rmanov/minecraft-mods/mods/arcane-naturalis/src/main/resources/assets/arcanenaturalis"

# ── crystal_seed.png — small teal crystal shape on transparent ────────────
def make_crystal_seed():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Central stem — dark teal
    d.rectangle([7, 10, 8, 15], fill=(60, 160, 120, 255))
    # Crystal points — faceted look
    # Main center shard
    d.polygon([(8, 2), (6, 8), (8, 12), (10, 8)], fill=(100, 220, 180, 230))
    # Left shard
    d.polygon([(7, 5), (5, 9), (7, 11), (8, 7)],  fill=(80, 200, 160, 200))
    # Right shard
    d.polygon([(9, 5), (11, 9), (9, 11), (8, 7)], fill=(120, 240, 200, 200))
    # Highlight on center shard
    d.line([(8, 3), (9, 6)], fill=(200, 255, 240, 180), width=1)
    img.save(f"{BASE}/block/crystal_seed.png")
    print("crystal_seed.png done")

# ── crystal_block.png — amethyst-like purple crystal face ─────────────────
def make_crystal_block():
    img = Image.new('RGBA', (16, 16), (80, 40, 120, 255))
    d = ImageDraw.Draw(img)
    # Draw several crystal faces with varying purple tones
    # Large central crystal face
    d.polygon([(8, 1), (4, 6), (4, 14), (8, 15), (12, 14), (12, 6)],
              fill=(130, 70, 180, 255))
    # Left facet (darker)
    d.polygon([(4, 6), (1, 9), (1, 14), (4, 14)],
              fill=(90, 50, 140, 255))
    # Right facet (lighter)
    d.polygon([(12, 6), (15, 9), (15, 14), (12, 14)],
              fill=(160, 100, 210, 255))
    # Top highlight
    d.polygon([(8, 1), (4, 6), (12, 6)], fill=(200, 150, 240, 255))
    # Inner glow sparkle
    d.ellipse([6, 6, 10, 10], fill=(220, 180, 255, 200))
    # White sparkle dot
    d.point((8, 8), fill=(255, 255, 255, 255))
    img.save(f"{BASE}/block/crystal_block.png")
    print("crystal_block.png done")

# ── butterfly.png — 32x32 butterfly sprite ────────────────────────────────
def make_butterfly():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Body region: 0-15 x, 0-15 y (front texture)
    # Body — dark body center
    d.rectangle([6, 5, 9, 14], fill=(40, 30, 20, 255))
    d.rectangle([7, 3, 8, 5], fill=(60, 50, 30, 255))  # head

    # Left wing (upper part of texture) — monarch-style orange+black
    d.ellipse([0, 3, 11, 12], fill=(220, 100, 20, 230))
    # Wing venation / black border
    d.ellipse([1, 4, 10, 11], outline=(20, 10, 0, 180), width=1)
    # White wing spots
    d.ellipse([1, 4, 3, 6], fill=(255, 255, 220, 200))
    d.ellipse([3, 9, 5, 11], fill=(255, 255, 220, 180))

    # Right wing (mirrored on same row, x=10-15 region shown at x+16 in texture)
    # Use 16-31 columns for right side
    d.ellipse([16, 3, 27, 12], fill=(220, 100, 20, 230))
    d.ellipse([17, 4, 26, 11], outline=(20, 10, 0, 180), width=1)
    d.ellipse([24, 4, 26, 6], fill=(255, 255, 220, 200))
    d.ellipse([22, 9, 24, 11], fill=(255, 255, 220, 180))

    # Lower wings (bottom half of texture, y=16-31)
    # Lower left wing
    d.ellipse([1, 16, 10, 28], fill=(200, 80, 15, 210))
    d.ellipse([2, 17, 9, 27], outline=(20, 10, 0, 150), width=1)
    d.ellipse([2, 25, 4, 27], fill=(255, 255, 220, 180))

    # Lower right wing
    d.ellipse([17, 16, 26, 28], fill=(200, 80, 15, 210))
    d.ellipse([18, 17, 25, 27], outline=(20, 10, 0, 150), width=1)
    d.ellipse([23, 25, 25, 27], fill=(255, 255, 220, 180))

    img.save(f"{BASE}/entity/butterfly.png")
    print("butterfly.png done")

# ── firefly.png — 8x8 yellow glow dot ────────────────────────────────────
def make_firefly():
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Glow gradient (concentric circles, brightest in center)
    d.ellipse([1, 1, 6, 6], fill=(255, 230, 50, 180))   # outer glow
    d.ellipse([2, 2, 5, 5], fill=(255, 240, 100, 220))  # mid
    d.ellipse([3, 3, 4, 4], fill=(255, 255, 200, 255))  # bright core
    img.save(f"{BASE}/particle/firefly.png")
    print("firefly.png done")

# ── aurora.png — 8x8 green/blue shimmer ──────────────────────────────────
def make_aurora():
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Soft vertical streak with green-cyan glow
    d.rectangle([2, 0, 5, 7], fill=(50, 200, 150, 160))  # base
    d.rectangle([3, 1, 4, 6], fill=(100, 255, 200, 220))  # bright center
    d.point((3, 0), fill=(180, 255, 230, 255))
    d.point((4, 7), fill=(130, 220, 190, 200))
    img.save(f"{BASE}/particle/aurora.png")
    print("aurora.png done")

# ── magical_dust.png — 8x8 purple sparkle ────────────────────────────────
def make_magical_dust():
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Star/sparkle shape in purple
    # Cross sparkle
    d.rectangle([3, 0, 4, 7], fill=(180, 80, 220, 200))   # vertical
    d.rectangle([0, 3, 7, 4], fill=(180, 80, 220, 200))   # horizontal
    d.ellipse([2, 2, 5, 5],   fill=(220, 130, 255, 240))   # center glow
    d.point((3, 3), fill=(255, 200, 255, 255))              # bright center
    img.save(f"{BASE}/particle/magical_dust.png")
    print("magical_dust.png done")

# ── icon.png — 32x32 mod icon ─────────────────────────────────────────────
def make_icon():
    img = Image.new('RGBA', (32, 32), (15, 10, 30, 255))  # dark background
    d = ImageDraw.Draw(img)
    # Crystal cluster in center
    d.polygon([(16, 4), (12, 12), (14, 22), (16, 24), (18, 22), (20, 12)],
              fill=(130, 70, 200, 255))
    d.polygon([(16, 4), (12, 12), (20, 12)], fill=(200, 150, 255, 255))
    # Butterfly silhouette
    d.ellipse([4, 8, 13, 16], fill=(200, 90, 20, 200))
    d.ellipse([19, 8, 28, 16], fill=(200, 90, 20, 200))
    d.rectangle([15, 10, 17, 20], fill=(40, 30, 20, 255))  # body
    # Stars
    for (sx, sy) in [(5, 5), (27, 6), (3, 25), (29, 26), (16, 28)]:
        d.point((sx, sy), fill=(255, 255, 200, 255))
    img.save(f"{ICON_PATH}/icon.png")
    print("icon.png done")

if __name__ == "__main__":
    make_crystal_seed()
    make_crystal_block()
    make_butterfly()
    make_firefly()
    make_aurora()
    make_magical_dust()
    make_icon()
    print("\nAll textures generated successfully!")
