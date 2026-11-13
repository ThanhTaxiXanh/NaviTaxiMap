#!/usr/bin/env python3
"""Fail emulator smoke when synthetic offline line layer is not visually present.

PNG reader uses Python standard library only; samples the central map viewport
to avoid false positives from cyan UI buttons at the top/bottom.
"""
import struct
import sys
import zlib
from pathlib import Path


def map_line_pixels(filename):
    raw = Path(filename).read_bytes()
    if not raw.startswith(b'\x89PNG\r\n\x1a\n'):
        raise ValueError("screenshot is not PNG")
    pos = 8
    idat = bytearray()
    width = height = depth = color = 0
    while pos < len(raw):
        length = struct.unpack_from(">I", raw, pos)[0]
        chunk = raw[pos + 4:pos + 8]
        value = raw[pos + 8:pos + 8 + length]
        pos += 12 + length
        if chunk == b'IHDR':
            width, height, depth, color = struct.unpack_from(">IIBB", value, 0)
        elif chunk == b'IDAT':
            idat.extend(value)
        elif chunk == b'IEND':
            break
    if depth != 8 or color not in (2, 6):
        raise ValueError(f"Unsupported PNG format: depth {depth}, color type {color}")
    bpp = 4 if color == 6 else 3
    stride = width * bpp
    unpacked = zlib.decompress(idat)
    previous = bytearray(stride)
    cursor = 0
    matched = 0
    for y in range(height):
        filt = unpacked[cursor]
        cursor += 1
        row = bytearray(unpacked[cursor:cursor + stride])
        cursor += stride
        for i in range(stride):
            left = row[i - bpp] if i >= bpp else 0
            top = previous[i]
            upper_left = previous[i - bpp] if i >= bpp else 0
            if filt == 1:
                predictor = left
            elif filt == 2:
                predictor = top
            elif filt == 3:
                predictor = (left + top) // 2
            elif filt == 4:
                p = left + top - upper_left
                a, b, c = abs(p-left), abs(p-top), abs(p-upper_left)
                predictor = left if a <= b and a <= c else (top if b <= c else upper_left)
            elif filt == 0:
                predictor = 0
            else:
                raise ValueError(f"Unknown PNG filter: {filt}")
            row[i] = (row[i] + predictor) & 255
        if int(height * .30) <= y < int(height * .72):
            for x in range(int(width * .12), int(width * .88)):
                i = x * bpp
                r, g, b = row[i:i+3]
                if r < 90 and 80 < g < 180 and 80 < b < 210:
                    matched += 1
        previous = row
    return matched


if __name__ == "__main__":
    count = map_line_pixels(sys.argv[1])
    print(f"synthetic PMTiles-colored pixels in map viewport: {count}")
    if count < 150:
        print("FAIL: missing rendered vector geometry", file=sys.stderr)
        sys.exit(1)
