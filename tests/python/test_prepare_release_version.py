import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from prepare_release_version import (  # noqa: E402
    parse_version,
    prepare_release_version,
    select_release_version,
)


class ReleaseVersionTests(unittest.TestCase):
    def test_first_release_uses_base_version(self):
        self.assertEqual(select_release_version("2.0.0"), "2.0.0")

    def test_higher_base_version_is_preserved(self):
        self.assertEqual(select_release_version("2.1.0", "2.0.9"), "2.1.0")

    def test_equal_base_version_increments_previous_patch(self):
        self.assertEqual(select_release_version("2.0.4", "2.0.4"), "2.0.5")

    def test_lower_base_version_increments_previous_patch(self):
        self.assertEqual(select_release_version("2.0.0", "2.0.4"), "2.0.5")

    def test_base_version_must_be_three_numeric_components(self):
        for value in ("", "2", "2.0", "2.0.0.1", "v2.0.0", "2.00.0", "beta"):
            with self.subTest(value=value):
                with self.assertRaises(ValueError):
                    parse_version(value, "base version")

    def test_previous_version_is_validated_when_present(self):
        with self.assertRaisesRegex(ValueError, "previous release version"):
            select_release_version("2.0.0", "2.0")

    def test_1_21_1_channel_is_accepted(self):
        with tempfile.TemporaryDirectory() as directory:
            properties = Path(directory) / "gradle.properties"
            properties.write_text("mod_version=0.0.0-dev\n", encoding="utf-8")

            result = prepare_release_version("1.0.0", "", properties, "1.21.1")

            self.assertEqual(result, "1.0.0")
            self.assertEqual(
                properties.read_text(encoding="utf-8"),
                "mod_version=1.0.0-1.21.1\n",
            )

    def test_prepare_updates_exact_mod_version_entry(self):
        with tempfile.TemporaryDirectory() as directory:
            properties = Path(directory) / "gradle.properties"
            properties.write_text(
                "group=dev.example\nmod_version=0.0.0-dev\nminecraft_version=26.3\n",
                encoding="utf-8",
            )

            result = prepare_release_version("2.0.4", "2.0.4", properties, "26.3")

            self.assertEqual(result, "2.0.5")
            self.assertEqual(
                properties.read_text(encoding="utf-8"),
                "group=dev.example\nmod_version=2.0.5-26.3\nminecraft_version=26.3\n",
            )

    def test_prepare_does_not_write_when_version_is_invalid(self):
        with tempfile.TemporaryDirectory() as directory:
            properties = Path(directory) / "gradle.properties"
            original = "mod_version=0.0.0-dev\n"
            properties.write_text(original, encoding="utf-8")

            with self.assertRaises(ValueError):
                prepare_release_version("not-a-version", "", properties, "26.3")

            self.assertEqual(properties.read_text(encoding="utf-8"), original)

    def test_prepare_requires_exactly_one_mod_version_entry(self):
        with tempfile.TemporaryDirectory() as directory:
            properties = Path(directory) / "gradle.properties"
            original = "mod_version=0.0.0-dev\nmod_version=0.0.1-dev\n"
            properties.write_text(original, encoding="utf-8")

            with self.assertRaisesRegex(ValueError, "found 2"):
                prepare_release_version("2.0.0", "", properties, "26.3")

            self.assertEqual(properties.read_text(encoding="utf-8"), original)


if __name__ == "__main__":
    unittest.main()
