# ERROR_LOG
## Resolved with evidence
1. CI 43351850610: obsolete Android SDK tools package; setup-android v4 fixed.
2. CI 43351946604: Kotlin 2.0 compiler vs MapLibre 2.2 metadata; Kotlin 2.2.10 fixed.
3. CI 43352713250: first map display style lacked roads; OpenFreeMap Liberty/Dark fixed, verified 43353698195.
4. CI 43358323365: synthetic PMTiles z0 archive app-launched but vector map was blank, not a true PASS. Root cause not isolated conclusively; revised fixture to five tiles (z0+z1), uncompressed directory and added screenshot pixel assertion. CI 43359838414 confirms visible geometry and passes.

## Open
- Need verify PMTiles behavior with realistic, licensed Vietnam vector packages, bounds and multiple zooms.
- Need ensure offline package manager safely handles downloads, pause/resume, corrupt files, atomics, storage pressure and rollback.
- No connected Xiaomi/Termux device for physical QA.
- No approved live traffic/sovereignty polygons/verified speed limits.
- No production style/attribution audit.
