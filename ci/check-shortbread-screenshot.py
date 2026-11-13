#!/usr/bin/env python3
"""Require actual Shortbread blue road geometry in central viewport."""
import importlib.util, sys
from pathlib import Path
file = Path(__file__).with_name("check-offline-screenshot.py")
spec = importlib.util.spec_from_file_location("png_decoder", file)
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)
pixels = mod.map_line_pixels(sys.argv[1])
print("SHORTBREAD_ROAD_PIXELS", pixels)
if pixels < 500:
    sys.exit("FAIL: missing real offline Shortbread road geometry")
