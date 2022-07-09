package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.ConditionRE;
import mcjty.lostcities.worldgen.lost.regassets.data.ConditionPart;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Condition implements ILostCityAsset {

    private final ResourceLocation name;

    private final List<Pair<Predicate<ConditionContext>, Pair<Float, String>>> valueSelector = new ArrayList<>();

    public Condition(ConditionRE object) {
        name = object.getRegistryName();
        for (ConditionPart cp : object.getValues()) {
            float factor = cp.getFactor();
            String value = cp.getValue();
            Predicate<ConditionContext> test = ConditionContext.parseTest(cp);
            valueSelector.add(Pair.of(test, Pair.of(factor, value)));
        }
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    public String getRandomValue(Random random, ConditionContext info) {
        List<Pair<Float, String>> values = new ArrayList<>();
        for (Pair<Predicate<ConditionContext>, Pair<Float, String>> pair : valueSelector) {
            if (pair.getLeft().test(info)) {
                values.add(pair.getRight());
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        Pair<Float, String> randomFromList = Tools.getRandomFromList(random, values, Pair::getLeft);
        if (randomFromList == null) {
            return null;
        } else {
            return randomFromList.getRight();
        }
    }
}
