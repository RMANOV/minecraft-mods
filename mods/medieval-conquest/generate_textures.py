#!/usr/bin/env python3
"""Original 16x16 textures for Medieval Conquest items and blocks.

Run: python3 generate_textures.py   (requires Pillow)

Every pixel below is drawn here from hand-made grids or simple math; no game
assets are copied. Existing textures that are not listed are left untouched.
"""
import math
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    "src", "main", "resources", "assets", "medievalconquest", "textures")


def grid(rows, palette):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), "grids are 16x16"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), palette[ch])
    return img


def save(img, kind, name):
    os.makedirs(os.path.join(ROOT, kind), exist_ok=True)
    img.save(os.path.join(ROOT, kind, name + ".png"))
    print(f"{kind}/{name}.png")


# Icy obsidian: deep blue-black stone split by bright frost cracks and ice stars.
ICY_OBSIDIAN = grid([
    "abbcbbabedbbacbb",
    "bbcccbbaedbbbcca",
    "bcceccbbdebabbcb",
    "bcefecbbbdeabbbb",
    "bbceccabbbdebbab",
    "abbcbbaabbbdeebb",
    "bbabbbccbbbbdfeb",
    "bacbbccdcbbbbedb",
    "bbbbccdedcbabbbb",
    "abbbbcdfdcbbcbba",
    "deabbbcdcbbccdbb",
    "bdeabbbcbbbcedcb",
    "bbdeebbbabcdfedc",
    "abbbdeabbbbcedcb",
    "bcbbbdeabbbbcdbb",
    "bbcbbbedbabbbbab",
], {
    "a": (14, 16, 36, 255), "b": (26, 30, 62, 255), "c": (42, 54, 100, 255),
    "d": (72, 132, 184, 255), "e": (146, 214, 238, 255), "f": (232, 250, 255, 255),
})

# Temporary barrier: frosty blue panel with an hourglass (it melts away in time).
TEMPORARY_BARRIER = grid([
    "oooooooooooooooo",
    "oqqqqqqqqqqqqqqo",
    "oqpppppppppppppo",
    "oqpppppppppppqko",
    "oqpphhhhhhhhppko",
    "oqpppfsssshpppko",
    "oqppppfsshppppko",
    "oqpqppphhppqppko",
    "oqppppfsghppppko",
    "oqpppfgsgghpppko",
    "oqpppfsssshpppko",
    "oqpphhhhhhhhppko",
    "oqpppppppppppqko",
    "oqqppppqppppppko",
    "okkkkkkkkkkkkkko",
    "oooooooooooooooo",
], {
    "o": (36, 72, 124, 255), "k": (66, 116, 170, 255), "p": (112, 170, 222, 255),
    "q": (156, 208, 242, 255), "h": (244, 246, 252, 255), "f": (204, 226, 246, 255),
    "s": (242, 202, 92, 255), "g": (196, 228, 250, 255),
})

# Permanent barrier: royal indigo panel, gold frame and a golden infinity sign.
PERMANENT_BARRIER = grid([
    "dggggggggggggggd",
    "gymmmmmmmmmmmmyg",
    "gmnnnnnnnnnnnnmg",
    "gmnnnnnnnnnnnnmg",
    "gmnnnnnnnnnnnnmg",
    "gmnyyynnnnyyynmg",
    "gmynnnynnynnnymg",
    "gmgnnnnggnnnngmg",
    "gmdnnndnndnnndmg",
    "gmndddnnnndddnmg",
    "gmnnnnnnnnnnnnmg",
    "gmnnnnnnnnnnnnmg",
    "gmnnnnnnnnnnnnmg",
    "gmnnnnnnnnnnnnmg",
    "gymmmmmmmmmmmmyg",
    "dggggggggggggggd",
], {
    "n": (32, 22, 74, 255), "m": (58, 42, 116, 255), "g": (232, 182, 58, 255),
    "y": (255, 232, 136, 255), "d": (168, 118, 32, 255),
})

# Pink lilac flower: a cone of small blossoms on a green stem.
PINK_FLOWER = grid([
    "................",
    ".......LP.......",
    "......LPPD......",
    ".....PLPDPD.....",
    ".....LPPLPD.....",
    "....PLDPPLPD....",
    "....LPPLDPPD....",
    ".....DPPLPD.....",
    "......DPPD......",
    ".......GD.......",
    ".......G........",
    "....HG.G........",
    ".....GGG........",
    ".......G..GH....",
    ".......GGG......",
    ".......G........",
], {
    "P": (230, 140, 200, 255), "L": (250, 196, 232, 255), "D": (178, 88, 158, 255),
    "G": (74, 146, 62, 255), "H": (42, 98, 42, 255),
})

# Candied lilac flower: sugar-frosted blossom on a striped candy stick.
CANDIED_FLOWER = grid([
    "..W.............",
    ".WSW...LW.......",
    "..W...LWPD......",
    ".....PLWDWD.....",
    ".....WPPLPW..W..",
    "....PLDWPLPD.WSW",
    "....LWPLDWPD..W.",
    ".....DPWLPD.....",
    "......DPWD......",
    ".......RR.......",
    ".......WW.......",
    ".......RR.......",
    ".......WW.......",
    ".......RR.......",
    ".......WW.......",
    ".......RR.......",
], {
    "P": (236, 150, 206, 255), "L": (252, 206, 238, 255), "D": (186, 100, 166, 255),
    "W": (255, 252, 255, 255), "S": (255, 236, 150, 255), "R": (222, 56, 72, 255),
})

# Alien flower: teal petals, glowing magenta heart, antennae with lime tips.
ALIEN_FLOWER = grid([
    "...Y........Y...",
    "....T......T....",
    ".....T....T.....",
    "....cCC..CCc....",
    "...cCCCcCCCCc...",
    "...cCCmMMmCCc...",
    "....cCMmmMCc....",
    "...cCCmMMmCCc...",
    "...cCCCcCCCCc...",
    "....cCC..CCc....",
    ".......tT.......",
    "..CC...tT..CC...",
    "...cCc.tT.cCc...",
    ".....cctTcc.....",
    ".......tT.......",
    ".......tT.......",
], {
    "T": (40, 150, 136, 255), "t": (22, 94, 92, 255), "C": (94, 232, 212, 255),
    "c": (42, 170, 172, 255), "M": (232, 64, 204, 255), "m": (255, 156, 242, 255),
    "Y": (214, 255, 96, 255),
})


def bad_orb():
    """Dark violet orb with a slow crimson swirl and one bright glint."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    cx = cy = 7.5
    radius = 6.6
    for y in range(16):
        for x in range(16):
            dx, dy = x - cx, y - cy
            dist = math.hypot(dx, dy)
            if dist > radius:
                continue
            if dist > radius - 1.0:
                img.putpixel((x, y), (28, 8, 40, 255))
                continue
            shade = 1.0 - 0.45 * (dist / radius) - 0.04 * (dx + dy)
            swirl = math.sin(3.0 * math.atan2(dy, dx) + 0.95 * dist)
            if swirl > 0.45:
                base = (178, 40, 76)
            else:
                base = (70, 22, 96)
            img.putpixel((x, y), tuple(max(0, min(255, int(c * shade))) for c in base) + (255,))
    for px, py, col in ((4, 4, (250, 214, 246)), (5, 4, (214, 160, 226)), (4, 5, (214, 160, 226))):
        img.putpixel((px, py), col + (255,))
    return img


def dragon_spawn_egg():
    """Speckled egg in the overworld dragon's forest greens."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    inside = set()
    for y in range(16):
        for x in range(16):
            ny = (y - 8.5) / 7.0
            half = 5.6 * (0.9 + 0.1 * ny)  # a little narrower at the top
            nx = (x - 7.5) / half
            if nx * nx + ny * ny <= 1.0:
                inside.add((x, y))
    for x, y in inside:
        edge = any((x + ox, y + oy) not in inside for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            col = (16, 44, 18)
        elif x + y < 13:
            col = (56, 128, 58)
        elif x + y > 19:
            col = (28, 78, 34)
        else:
            col = (40, 104, 46)
        img.putpixel((x, y), col + (255,))
    light, gold = (122, 196, 92), (232, 200, 64)
    spots = {(6, 4): light, (7, 4): light, (6, 5): light, (10, 7): light, (10, 8): light,
             (4, 9): light, (5, 9): light, (8, 12): light, (9, 12): light, (8, 11): light,
             (8, 8): gold, (4, 6): gold, (11, 11): gold, (6, 13): gold}
    for pos, col in spots.items():
        assert pos in inside
        img.putpixel(pos, col + (255,))
    return img


def flowering_lilac_leaves():
    """Lilac leaves with clustered purple blossoms (opaque, tiles seamlessly)."""
    greens = [(66, 112, 66), (78, 128, 74), (58, 100, 60), (88, 140, 82)]
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), greens[(x * 7 + y * 13 + (x * y) % 5) % 4] + (255,))
    light, mid, dark = (226, 196, 240), (186, 138, 214), (140, 92, 178)
    for cx, cy in ((3, 3), (11, 2), (7, 8), (14, 10), (2, 12), (10, 14)):
        for ox, oy, col in ((0, 0, light), (1, 0, mid), (-1, 0, mid), (0, 1, mid),
                            (0, -1, light), (1, 1, dark), (-1, 1, dark)):
            img.putpixel(((cx + ox) % 16, (cy + oy) % 16), col + (255,))
    return img


def main():
    save(ICY_OBSIDIAN, "block", "icy_obsidian")
    save(TEMPORARY_BARRIER, "block", "temporary_barrier")
    save(PERMANENT_BARRIER, "block", "permanent_barrier")
    save(flowering_lilac_leaves(), "block", "lilac_flowering_leaves")
    save(PINK_FLOWER, "item", "pink_flower")
    save(CANDIED_FLOWER, "item", "candied_flower")
    save(ALIEN_FLOWER, "block", "alien_flower")
    save(bad_orb(), "item", "bad_orb")
    save(dragon_spawn_egg(), "item", "overworld_dragon_spawn_egg")


if __name__ == "__main__":
    main()
