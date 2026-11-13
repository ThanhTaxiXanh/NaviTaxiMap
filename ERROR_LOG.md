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

- 2026-11-13 PMTiles: screenshot of offline-demo in run 43358323365 has blank vector layer even though app process/UI is alive. Investigating source tile addressing and capture native logs; user-visible status must remain IN PROGRESS until geometry renders.
