# ERROR_LOG
Resolved:
1. CI SDK tool package deprecated; upgraded setup-android v4.
2. Kotlin 2.0 compiler incompatible with MapLibre 13.5.4 (metadata 2.2); upgraded Kotlin to 2.2.10.
3. World-level demo style lacked street details; online OpenFreeMap style succeeded in emulator.

Open:
- PMTiles offline runtime screenshot requires evidence from new emulator smoke.
- Xiaomi/Termux device is not connected.
- No offline city/province packs, routing graphs or approved sovereignty layers.
- Online OpenFreeMap dependency does not satisfy independent offline map operations.
