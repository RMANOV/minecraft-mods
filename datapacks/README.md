# Datapacks

Vanilla-compatible datapacks for Minecraft 1.21.x.
Drop into your world's `datapacks/` folder and `/reload`.

---

## How to Install

1. Open your world folder: `.minecraft/saves/<world>/datapacks/`
2. Copy the datapack folder there
3. In-game: `/reload`
4. Verify: `/datapack list`

## How to Create

Minimum datapack structure:
```
my_datapack/
├── pack.mcmeta
└── data/
    └── my_namespace/
        └── function/
            └── my_function.mcfunction
```

**pack.mcmeta** (1.21.x):
```json
{
  "pack": {
    "pack_format": 57,
    "description": "My custom datapack"
  }
}
```

Run with: `/function my_namespace:my_function`

---

*Datapacks will be added here as they're created.*
