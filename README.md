# The Lost Cities (1.12.2)

Hello and welcome to Strubium steals code from people to make a old mod slightly better 

(90% of code from here: https://github.com/Burchard36/LostCities-RenderFix)

Bugs Fixed:

* In 1.12.2 TileEntitys not gettting special treatment and being placed as normal blocks. (Not at all my work, this is the power of Burchard36) 

Example of what is now possible. (Code again from Burchard36)
```json
{
   "type":"palette",
   "name":"common",
   "palette":[
      {
         "char":"^",
         "tile_entity":true,
         "blocks":[
            {
               "random":11,
               "block":"cfm:ceiling_fan"
            },
            {
               "random":25,
               "block":"mw:hanging_body"
            },
            {
               "random":1000,
               "block":"minecraft:web"
            }
         ]
      }
   ]
}
```

## Changes
* Everything is Bugs Fixed, duh
* New "nospawner" profile. Generates buildings with loot but without mod spawners.  
