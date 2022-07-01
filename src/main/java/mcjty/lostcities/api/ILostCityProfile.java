package mcjty.lostcities.api;

/**
 * The representation of a lost cities profile
 */
public interface ILostCityProfile {

    void setDescription(String description);

    void setWorldStyle(String worldStyle);

    void setCityChancle(double chance);

    void setRuinChance(float chance, float minPercent, float maxPercent);

    void setGroundLevel(int level);

    void setCityLevelHeights(int l0, int l1, int l2, int l3);

    void setOceanCorrectionBorder(int border);
}
