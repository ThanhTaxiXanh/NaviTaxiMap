#!/usr/bin/env python3
"""Check colored REAL OSM roads are drawn, without third-party PNG libraries.

Reuse the already-tested stdlib PNG decoder from synthetic PMTiles smoke.
The blue local-road line paint is RGB (39,94,108), unique to this pilot style;
the map viewport excludes toolbar buttons and attribution labels.
"""
import importlib.util
from pathlib import Path
import sys

checker = Path(__file__).with_name("check-offline-screenshot.py")
spec = importlib.util.spec_from_file_location("navitaxi_png_decoder", checker)
assert spec is not None and spec.loader is not None
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
count = module.map_line_pixels(sys.argv[1])
print(f"REAL OSM local-road line pixels in central map viewport: {count}")
if count < 500:
    print("FAIL: no confirmed real OSM blue road geometry", file=sys.stderr)
    sys.exit(1)
