# Configuration

The mod generates a configuration file located at `config/ia-fuel-compat.json`. This file gives you fine-grained control over how the mod behaves, allowing you to seamlessly balance it for your modpack.

## Global Settings

```json
  "enabled": true,
  "debugLogging": false,
  "baseBurnTime": 1200
```
*   `enabled`: Master toggle for the entire compatibility mod. Set to `false` to disable the mod entirely without uninstalling it.
*   `debugLogging`: Enable this if you are a modpack developer troubleshooting fuel detection or fluid extraction.
*   `baseBurnTime`: The baseline fuel value (in ticks) that fuel multipliers are applied against.

## Fuel Settings

Under the `fuels` section, you will find an entry for every supported fuel type across all integrated mods.

```json
  "fuels": {
    "tr_nitro_diesel": {
      "enabled": true,
      "multiplier": 2.0
    },
    ...
  }
```
*   `enabled`: Set to `false` to prevent this specific fluid from being used as fuel.
*   `multiplier`: Determines how powerful the fuel is relative to `baseBurnTime`. A value of `2.0` will provide twice as much burn time per bucket as a value of `1.0`.

## Container Settings

```json
  "containers": {
    "buckets": true,
    "techrebornCells": true
  }
```
*   `buckets`: Allows fluid buckets to be used in the aircraft's fuel slot (currently a placeholder for future generic bucket support).
*   `techrebornCells`: Toggle specific container integrations.
