import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
from types import SimpleNamespace

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from generate_release_notes import (  # noqa: E402
    render_release_notes,
    write_release_notes,
)


class ReleaseNotesTests(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.repo_dir = Path(self.temp_dir.name)
        self.repo = "MxxFoxx2/forbidden-magic-reforged"
        self.source_sha = "a" * 40
        self.previous_sha = "b" * 40

    def tearDown(self):
        self.temp_dir.cleanup()

    def git_subject(self, subject="Fix release notes"):
        return SimpleNamespace(stdout=f"{subject}\n")

    @patch("generate_release_notes.subprocess.run")
    def test_renders_version_commit_compare_link_and_source_markers(self, run):
        run.return_value = self.git_subject()
        previous_body = f"<!-- release-source-sha: {self.previous_sha} -->"

        notes = render_release_notes(
            repo=self.repo,
            source_sha=self.source_sha,
            release_version="2.0.5",
            mc_version="26.3",
            repo_dir=self.repo_dir,
            previous_body=previous_body,
        )

        expected = "\n".join(
            [
                "Version 2.0.5 for Minecraft 26.3",
                "",
                f"{chr(96)}{self.source_sha[:7]}{chr(96)}: Fix release notes",
                "",
                f"[Changes since previous build](https://github.com/{self.repo}/compare/{self.previous_sha}...{self.source_sha})",
                "",
                f"<!-- release-source-sha: {self.source_sha} -->",
                "<!-- release-version: 2.0.5 -->",
            ]
        ) + "\n"
        self.assertEqual(notes, expected)
        run.assert_called_once_with(
            ["git", "-C", str(self.repo_dir), "show", "-s", "--format=%s", self.source_sha],
            check=True,
            capture_output=True,
            text=True,
        )

    @patch("generate_release_notes.subprocess.run")
    def test_omits_compare_link_without_previous_source_marker(self, run):
        run.return_value = self.git_subject()

        notes = render_release_notes(
            repo=self.repo,
            source_sha=self.source_sha,
            release_version="2.0.5",
            mc_version="26.3",
            repo_dir=self.repo_dir,
        )

        self.assertNotIn("Changes since previous build", notes)

    @patch("generate_release_notes.subprocess.run")
    def test_omits_compare_link_when_previous_source_is_current_source(self, run):
        run.return_value = self.git_subject()
        previous_body = f"<!-- release-source-sha: {self.source_sha} -->"

        notes = render_release_notes(
            repo=self.repo,
            source_sha=self.source_sha,
            release_version="2.0.5",
            mc_version="26.3",
            repo_dir=self.repo_dir,
            previous_body=previous_body,
        )

        self.assertNotIn("Changes since previous build", notes)

    @patch("generate_release_notes.subprocess.run")
    def test_includes_trimmed_version_specific_release_notes(self, run):
        run.return_value = self.git_subject()
        release_dir = self.repo_dir / "docs" / "releases"
        release_dir.mkdir(parents=True)
        (release_dir / "2.0.5.md").write_text(
            "\n\n## Major changes\n\nDetailed notes.\n\n",
            encoding="utf-8",
        )

        notes = render_release_notes(
            repo=self.repo,
            source_sha=self.source_sha,
            release_version="2.0.5",
            mc_version="26.3",
            repo_dir=self.repo_dir,
        )

        self.assertIn("\n## Major changes\n\nDetailed notes.\n", notes)

    @patch("generate_release_notes.subprocess.run")
    def test_ignores_empty_version_specific_notes(self, run):
        run.return_value = self.git_subject()
        release_dir = self.repo_dir / "docs" / "releases"
        release_dir.mkdir(parents=True)
        (release_dir / "2.0.5.md").write_text(" \n\t\n", encoding="utf-8")

        notes = render_release_notes(
            repo=self.repo,
            source_sha=self.source_sha,
            release_version="2.0.5",
            mc_version="26.3",
            repo_dir=self.repo_dir,
        )

        self.assertNotIn("Major changes", notes)
        self.assertNotIn("Detailed notes", notes)

    @patch("generate_release_notes.subprocess.run")
    def test_writes_notes_with_utf8_and_lf_newlines(self, run):
        run.return_value = self.git_subject("Исправление заметок")
        output_file = self.repo_dir / "release-notes.md"

        write_release_notes(
            output_file=output_file,
            repo=self.repo,
            source_sha=self.source_sha,
            release_version="2.0.5",
            mc_version="26.3",
            repo_dir=self.repo_dir,
        )

        data = output_file.read_bytes()
        self.assertIn("Исправление заметок".encode("utf-8"), data)
        self.assertNotIn(b"\r\n", data)
        self.assertTrue(data.endswith(b"\n"))


if __name__ == "__main__":
    unittest.main()
