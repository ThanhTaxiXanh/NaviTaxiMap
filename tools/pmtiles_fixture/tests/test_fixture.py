"""Checks that the committed PMTiles v3 fixture is self-contained and deterministic."""
import base64
import gzip
import hashlib
import json
from pathlib import Path
import struct
import sys
import unittest

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from generate import create, vector_tile

FIXTURE = ROOT / 'app/src/main/assets/offline_demo.pmtiles.b64'
EXPECTED_SHA = '16fc20efb488c208d5e7c2c89daf0adf13a2790bb558234d9544a14038c86f3b'


def varints(data):
    pos = 0
    while pos < len(data):
        n = 0
        shift = 0
        while True:
            b = data[pos]
            pos += 1
            n |= (b & 127) << shift
            if not b & 128:
                break
            shift += 7
        yield n


class TestPMTilesFixture(unittest.TestCase):
    def setUp(self):
        self.data = base64.b64decode(FIXTURE.read_text().strip(), validate=True)

    def test_is_deterministic(self):
        self.assertEqual(self.data, create())
        self.assertEqual(hashlib.sha256(self.data).hexdigest(), EXPECTED_SHA)

    def test_header_and_section_bounds(self):
        header = self.data[:127]
        self.assertEqual(header[:8], b'PMTiles\x03')
        self.assertEqual(header[96:102], bytes([1, 2, 1, 1, 0, 0]))
        vals = struct.unpack_from('<11Q', header, 8)
        root_off, root_len, meta_off, meta_len, leaf_off, leaf_len, tile_off, tile_len, addressed, entries, contents = vals
        self.assertEqual(root_off, 127)
        self.assertLess(root_off + root_len, 16_384)
        self.assertEqual((addressed, entries, contents), (1, 1, 1))
        self.assertEqual(leaf_len, 0)
        self.assertEqual(leaf_off, tile_off)
        self.assertEqual(tile_off + tile_len, len(self.data))
        self.assertEqual(meta_off, root_off + root_len)
        self.assertEqual(tile_off, meta_off + meta_len)
        self.assertEqual(list(varints(gzip.decompress(self.data[root_off:meta_off]))), [1, 0, 1, tile_len, 1])
        self.assertEqual(self.data[tile_off:tile_off + tile_len], vector_tile())
        metadata = json.loads(self.data[meta_off:tile_off])
        self.assertEqual(metadata['vector_layers'][0]['id'], 'synthetic')

    def test_not_a_geographical_dataset(self):
        self.assertIn('SYNTHETIC', self.data.decode('latin-1'))
        self.assertNotIn(b'https://', self.data)
        self.assertNotIn(b'www.', self.data)


if __name__ == '__main__':
    unittest.main()
