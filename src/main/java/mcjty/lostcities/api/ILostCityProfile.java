package mcjty.lostcities.api;

/**
 * The representation of a lost cities profile
 */
public interface ILostCityProfile {

    void setDescription(String description);

    void setWorldStyle(String worldStyle);

    void setCityChancle(double chance);

    void setRuinChance(float chance, float minPercent, float maxPercent);
}
