# Minecraft Commands & Command Block Recipes

Advanced command patterns for experienced players. All tested on **1.21.x**.

---

## Boss Fights

### Custom Boss Health Bar
```mcfunction
# Create a boss bar
bossbar add custom:dragon_boss {"text":"Ancient Dragon","color":"dark_red"}
bossbar set custom:dragon_boss max 200
bossbar set custom:dragon_boss color red
bossbar set custom:dragon_boss style notched_10
bossbar set custom:dragon_boss players @a

# Link to a mob's health (run in repeating command block)
execute as @e[type=minecraft:wither,limit=1] store result bossbar custom:dragon_boss value run data get entity @s Health
```

### Mob with Custom Gear
```mcfunction
# Skeleton with enchanted bow + diamond armor
summon minecraft:skeleton ~ ~ ~ {HandItems:[{id:"minecraft:bow",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:power":5,"minecraft:flame":1}}}},{}],ArmorItems:[{id:"minecraft:diamond_boots",count:1},{id:"minecraft:diamond_leggings",count:1},{id:"minecraft:diamond_chestplate",count:1},{id:"minecraft:diamond_helmet",count:1}],CustomName:'{"text":"Elite Guard","color":"gold"}',PersistenceRequired:1b}
```

---

## Execute Chains

### Kill zone — damage all players in area every tick
```mcfunction
# Repeating command block, always active
execute as @a[x=100,y=64,z=100,dx=20,dy=10,dz=20] run damage @s 2 minecraft:magic
```

### Proximity detection — trigger when player approaches
```mcfunction
# Check if any player is within 5 blocks of a point
execute if entity @a[x=0,y=64,z=0,distance=..5] run say Player detected!

# Scoreboard-based cooldown (prevents spam)
scoreboard objectives add cd dummy
execute as @a[x=0,y=64,z=0,distance=..5,scores={cd=0}] run function custom:on_detect
execute as @a[scores={cd=1..}] run scoreboard players remove @s cd 1
```

### Chain execution — sequential multi-step
```mcfunction
# Step 1: Mark entities
tag @e[type=zombie,distance=..20] add marked
# Step 2: Buff marked entities
effect give @e[tag=marked] minecraft:strength 30 2
effect give @e[tag=marked] minecraft:speed 30 1
# Step 3: Announce
title @a actionbar {"text":"Zombie horde incoming!","color":"red"}
# Step 4: Cleanup
tag @e[tag=marked] remove marked
```

---

## Scoreboard Mechanics

### Kill counter with ranks
```mcfunction
scoreboard objectives add kills playerKillCount {"text":"Kills"}
scoreboard objectives setdisplay sidebar kills

# Rank system (chain of conditional command blocks)
execute as @a[scores={kills=10..}] run tag @s add veteran
execute as @a[scores={kills=50..}] run tag @s add elite
execute as @a[scores={kills=100..}] run tag @s add legend

# Display rank in nametag
execute as @a[tag=legend] run team join legends @s
team modify legends prefix {"text":"[LEGEND] ","color":"gold"}
```

### Timer system
```mcfunction
scoreboard objectives add timer dummy
# Increment (repeating command block)
scoreboard players add @a timer 1
# Check for 5 minutes (6000 ticks)
execute as @a[scores={timer=6000}] run function custom:on_timer_done
execute as @a[scores={timer=6000}] run scoreboard players set @s timer 0
```

---

## World Manipulation

### Fill patterns
```mcfunction
# Hollow cube (glass dome)
fill ~-10 ~ ~-10 ~10 ~20 ~10 glass hollow

# Replace specific blocks in area
fill ~-20 ~-5 ~-20 ~20 ~5 ~20 stone replace dirt

# Clone a structure
clone ~-5 ~ ~-5 ~5 ~10 ~5 ~20 ~ ~-5
```

### Structure saving/loading
```mcfunction
# Save a structure block area
setblock ~ ~ ~ structure_block{mode:"SAVE",name:"custom:my_build",posX:-5,posY:0,posZ:-5,sizeX:10,sizeY:10,sizeZ:10}

# Load it elsewhere
setblock ~ ~ ~ structure_block{mode:"LOAD",name:"custom:my_build"}
```

---

## Particle Effects

### Spiral particle ring
```mcfunction
# Run in repeating command block at center point
# Uses armor stand rotation trick for circular pattern
execute as @e[type=armor_stand,tag=particle_center] at @s run particle minecraft:flame ^1 ^0 ^0 0 0 0 0 1
execute as @e[type=armor_stand,tag=particle_center] at @s run tp @s ~ ~ ~ ~10 ~
```

### Area indicator (warning zone)
```mcfunction
# Dust particles marking a boundary
execute positioned 100 64 100 run particle minecraft:dust{color:[1.0,0.0,0.0],scale:1.5} ~ ~1 ~ 10 0.5 10 0 50
```

---

## Useful One-Liners

```mcfunction
# Find all structures near you
locate structure #minecraft:on_ocean_explorer_maps

# Clear all dropped items in loaded chunks
kill @e[type=item]

# Disable mob griefing but keep mob AI
gamerule mobGriefing false

# Give yourself a max-enchant sword
give @s minecraft:netherite_sword{components:{"minecraft:enchantments":{levels:{"minecraft:sharpness":5,"minecraft:looting":3,"minecraft:unbreaking":3,"minecraft:mending":1,"minecraft:sweeping_edge":3,"minecraft:fire_aspect":2,"minecraft:knockback":2}}}}

# Permanent night vision without particles
effect give @s night_vision infinite 0 true

# Find nearest player to a coordinate (useful for maps)
execute as @a store result score @s dist run execute positioned 0 64 0 run scoreboard players get @s dummy
```
