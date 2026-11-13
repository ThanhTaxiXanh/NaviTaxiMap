#!/usr/bin/env python3
"""Deterministic SYNTHETIC PMTiles v3/MVT fixture generator, no geographic data."""
import base64
import gzip
import hashlib
import json
import struct
from pathlib import Path


def varint(value):
    assert value >= 0
    output = bytearray()
    while value >= 128:
        output.append((value & 0x7f) | 0x80)
        value >>= 7
    output.append(value)
    return bytes(output)


def field(number, wire_type, payload):
    return varint((number << 3) | wire_type) + payload


def packed(number, values):
    payload = b''.join(map(varint, values))
    return field(number, 2, varint(len(payload)) + payload)


def encoded_string(number, value):
    b = value.encode('utf-8')
    return field(number, 2, varint(len(b)) + b)


def zigzag(value):
    return (value << 1) ^ (value >> 31)


def vector_tile():
    # Only schematic grid lines, not streets and not a depiction of any place.
    lines = [
        [(700, 600), (700, 3500)],
        [(1500, 400), (1500, 3700)],
        [(2300, 300), (2300, 3800)],
        [(3200, 400), (3200, 3700)],
        [(400, 900), (3700, 900)],
        [(400, 1700), (3700, 1700)],
        [(400, 2500), (3700, 2500)],
        [(400, 3300), (3700, 3300)],
        [(500, 3800), (1800, 2100), (3500, 400)],
    ]
    features = []
    for number, points in enumerate(lines, start=1):
        geometry = [(1 << 3) | 1]
        current_x, current_y = 0, 0
        first_x, first_y = points[0]
        geometry += [zigzag(first_x - current_x), zigzag(first_y - current_y)]
        current_x, current_y = first_x, first_y
        geometry.append(((len(points) - 1) << 3) | 2)
        for x, y in points[1:]:
            geometry += [zigzag(x - current_x), zigzag(y - current_y)]
            current_x, current_y = x, y
        feat = field(1, 0, varint(number)) + field(3, 0, varint(2)) + packed(4, geometry)
        features.append(field(2, 2, varint(len(feat)) + feat))
    layer = encoded_string(1, 'synthetic') + b''.join(features)
    layer += field(5, 0, varint(4096)) + field(15, 0, varint(2))
    return field(3, 2, varint(len(layer)) + layer)


def create():
    tile = vector_tile()
    # One root directory entry: n=1, TileID=0, run=1, length, offset+1=1.
    directory = gzip.compress(
        varint(1) + varint(0) + varint(1) + varint(len(tile)) + varint(1),
        mtime=0,
    )
    metadata = json.dumps({
        'name': 'NaviTaxiMap SYNTHETIC offline renderer test',
        'format': 'pbf',
        'type': 'overlay',
        'vector_layers': [{'id': 'synthetic', 'fields': {}, 'minzoom': 0, 'maxzoom': 0}],
        'attribution': 'Synthetic test fixture, NOT real streets or navigation',
    }, separators=(',', ':'), sort_keys=True).encode('utf-8')
    header = bytearray(127)
    header[:8] = b'PMTiles\x03'
    root_off = 127
    meta_off = root_off + len(directory)
    leaf_off = meta_off + len(metadata)
    tile_off = leaf_off
    segments = [
        root_off, len(directory), meta_off, len(metadata),
        leaf_off, 0, tile_off, len(tile), 1, 1, 1
    ]
    struct.pack_into('<11Q', header, 8, *segments)
    header[96:102] = bytes([1, 2, 1, 1, 0, 0])  # gzip dir / uncompressed MVT
    struct.pack_into('<4i', header, 102, -1800000000, -850000000, 1800000000, 850000000)
    header[118] = 0
    struct.pack_into('<2i', header, 119, 0, 0)
    return bytes(header) + directory + metadata + tile


if __name__ == '__main__':
    data = create()
    destination = Path(__file__).resolve().parents[2] / 'app/src/main/assets/offline_demo.pmtiles.b64'
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(base64.b64encode(data).decode() + '\n', encoding='ascii')
    print(f'Generated {len(data)} bytes; SHA-256={hashlib.sha256(data).hexdigest()}')
