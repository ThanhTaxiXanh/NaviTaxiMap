# Vietnam Sovereignty Map validation

Imported and adapted from the user-supplied `vietnam-sovereignty-map.zip`.
Dataset approval and independent verification are **mandatory** before a map
label, boundary or sovereignty feature is shipped. No approved data is bundled.

Run: `python3 -m unittest discover -s tools/vietnam-sovereignty-map/tests -v`.

The online/offline capture audit in the original skill additionally needs actual
Android map evidence, an independently reviewed golden set and an adapter.
Passing unit tests alone never authorizes release.
