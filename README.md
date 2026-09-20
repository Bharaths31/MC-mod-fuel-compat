# Immersive Aircraft Fuel Compatibility

A simple Fabric mod for Minecraft 1.21.1 that bridges the gap between **Immersive Aircraft** and tech mods like **Oritech** and **Tech Reborn**. This mod enables Immersive Aircraft engines to burn liquid fuels provided by these mods directly, either via buckets or cells.

## Features
- **Oritech Support**: Burn Oritech Fuel, Biofuel, and Diesel buckets directly in IA aircraft.
- **Tech Reborn Support**: Burn Tech Reborn Diesel, Biofuel, Nitro Diesel, and Nitrofuel buckets and cells.
- **Dynamic Configuration**: Extensive `ia-fuel-compat.json` configuration file allows you to toggle specific fuels, change burn rates, or disable entire integrations.
- **Smart Container Handling**: Returns empty buckets or cells when a fuel item is fully consumed, exactly as players expect.
- **Universal Addon Support**: By intercepting `Utils.getFuelTime`, this mod works universally on both base IA vehicles (Biplane, Airship, etc.) and all third-party IA addon vehicles!

## Commands
- `/ia-fuels` (Requires permission level 2): Dumps the active fuel registry and loaded container adapters to chat, showing exactly what fuels are registered, what mod provides them, and their effective burn time.

## Configuration
The mod generates a `config/ia-fuel-compat.json` file on first launch.
- Adjust `multiplier` values to change burn times.
- Set `enabled` to `false` for specific fuels to disable them.
- Toggle `buckets` or `techrebornCells` to control what items the mod intercepts.

## Technical Details
This mod dynamically queries the Fabric registries at runtime rather than relying on hardcoded item references. This means it will safely skip disabled or missing items without crashing or erroring.
