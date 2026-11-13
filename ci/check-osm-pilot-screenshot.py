#!/usr/bin/env python3
"""Assert independently that actual OSM pilot roads were drawn on emulator.

The unique layer colors are not used by the synthetic grid demo or by the
blank offline background. This is a coarse image assertion, not geospatial QA.
"""
from PIL import Image
import sys

path = sys.argv[1]
im = Image.open(path).convert("RGB")
w, h = im.size
# Exclude top status/navigation/action bars and a margin around the map.
rect = (int(w * .10), int(h * .22), int(w * .90), int(h * .72))
crop = im.crop(rect)
palette = ((39, 94, 108), (211, 100, 52), (128, 151, 143))
counts = []
for color in palette:
    count = sum(1 for r, g, b in crop.getdata()
                if all(abs(a - c) <= 19 for a, c in zip((r, g, b), color)))
    counts.append(count)
print("Real OSM pilot colors in central map viewport:", counts)
assert sum(counts[:2]) > 200, "Insufficient REAL OSM road pixels; map may be blank"
