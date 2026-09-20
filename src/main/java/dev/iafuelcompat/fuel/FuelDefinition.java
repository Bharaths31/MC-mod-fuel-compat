package dev.iafuelcompat.fuel;

public record FuelDefinition(
    String id,
    String displayName,
    String provider,
    String fluidId,
    int baseBurnTime,
    float multiplier,
    boolean enabled
) {
    public int getEffectiveBurnTime() {
        return Math.round(baseBurnTime * multiplier);
    }
}
