# The Lost Cities (1.12.2)

Hello and welcome to Strubium steals code from people to make a old mod slightly better 

(90% of modified code from here: https://github.com/Burchard36/LostCities-RenderFix)

(Original Mod: https://github.com/McJtyMods/LostCities)

## Changes
* TileEntitys now get special treatment and arent placed as normal blocks. (Not my work, this is the power of Burchard36) 
* New "nospawner" profile. Generates buildings with loot, but without mod spawners.
* New "geopol" profile. Generate cities with the power of [GeoPol!](https://github.com/markgyoni/geopol)
* Tweeks to the "onlycity" profile. Railways + Highways. 

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
Note that even if <code>tile_entity</code> is <code>true</code>, not all <code>block</code>'s need to tile_entitys, they will be placed "normally" without calculations on their direction.  

## Future Plans
* A palette that uses MWC Props 
* Remastered Chisel profile (It will look nicer)
