# LostCities

This LostCities Fork is VERY fucky, im not responsible if this corrupts your world. You have been warned. I have 0 modding experience but I know my java so i did the absolutely best I could to get this working!

The original reason for this forks creation stems from this issue: https://github.com/McJtyMods/LostCities/issues/155

For some reason in 1.12.2 TileEntitys do NOT get any special treatment and are placed as normal blocks without any type of special treatment (Chests and Spawners do, but simply because they are hard coded)

This fix aims to combat this render glitch by adding in a config option to palettes. Here is an example palette:

(For some reason, it took me 20 minutes to parse this json lmfao)
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

Notice that even though not all entries of `blocks` are TileEntities. Thats fine the mod will just ignore them and not update the blocks that are not TileEntities. This also works for single `block` entries as well!

"Why cant you automatically update TileEntities" - This bug is rather unique & im not going into full detail, it took a lot of time to get this working so this will be an end goal soon & the config option removed

This mod also will add in directional highways for X and Z that way you can line up your blocks properly for very detailed roads!
