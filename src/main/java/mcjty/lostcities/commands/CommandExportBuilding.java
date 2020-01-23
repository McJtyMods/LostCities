package mcjty.lostcities.commands;

public class CommandExportBuilding {} /* @todo 1.14 implements ICommand {

    @Override
    public String getName() {
        return "lc_exportbuilding";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " <file> <floors>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        try {
            int floors = Integer.parseInt(args[1]);

            JsonArray array = new JsonArray();

            EntityPlayer player = (EntityPlayer) sender;
            BlockPos start = player.getPosition().down();

            String palettechars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int idx = 0;
            Map<BlockState, Character> mapping = new HashMap<>();
            Palette palette = new Palette("give_name");
            LostCityChunkGenerator provider = WorldTypeTools.getChunkGenerator(sender.getEntityWorld().provider.getDimension());
            BuildingInfo info = BuildingInfo.getBuildingInfo(start.getX() >> 4, start.getZ() >> 4, provider);
            for (Character character : info.getCompiledPalette().getCharacters()) {
                BlockState state = info.getCompiledPalette().getStraight(character);
                if (state != null) {
                    palette.addMapping(character, state);
                    mapping.put(state, character);
                }
            }

            for (int f = 0 ; f < floors ; f++) {
                Slice[] slices = new Slice[6];
                for (int y = 0 ; y < 6 ; y++) {
                    slices[y] = new Slice();
                }
                int cx = (start.getX() >> 4) * 16;
                int cy = start.getY() + f * 6;
                int cz = (start.getZ() >> 4) * 16;
                BlockPos.Mutable pos = new BlockPos.Mutable(cx, cy, cz);
                for (int x = 0 ; x < 16 ; x++) {
                    for (int z = 0 ; z < 16 ; z++) {
                        for (int y = 0 ; y < 6 ; y++) {
                            pos.setPos(cx + x, cy + y, cz + z);
                            BlockState state = server.getEntityWorld().getBlockState(pos);
                            Character character = mapping.get(state);
                            if (character == null) {
                                while (true) {
                                    character = state.getBlock() == Blocks.AIR ? ' ' : palettechars.charAt(idx);
                                    idx++;
                                    if (!palette.getPalette().containsKey(character)) {
                                        break;
                                    }
                                }
                                palette.addMapping(character, state);
                                mapping.put(state, character);
                            }
                            slices[y].sequence[z*16+x] = "" + character;
                        }
                    }
                }

                String[] sl = new String[6];
                for (int i = 0 ; i < 6 ; i++) {
                    sl[i] = StringUtils.join(slices[i].sequence);
                }

                BuildingPart part = new BuildingPart("p" + f, 16, 16, sl);
                array.add(part.writeToJSon());
            }

            array.add(palette.writeToJSon());

//            AssetRegistries.STYLES.writeToJson(array);
//            AssetRegistries.CITYSTYLES.writeToJson(array);
//            AssetRegistries.PALETTES.writeToJson(array);
//            AssetRegistries.PARTS.writeToJson(array);
//            AssetRegistries.BUILDINGS.writeToJson(array);
//            AssetRegistries.MULTI_BUILDINGS.writeToJson(array);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(PrintWriter writer = new PrintWriter(new File(args[0]))) {
                writer.print(gson.toJson(array));
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            sender.sendMessage(new TextComponentString("Error writing to file '" + args[0] + "'!"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }

    public static class Slice {
        String sequence[] = new String[256];
    }
}
*/