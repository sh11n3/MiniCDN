#!/usr/bin/env python3
"""Compatibility entrypoint for the cross-platform throughput benchmark."""

from pathlib import Path
import runpy


if __name__ == "__main__":
    target = Path(__file__).resolve().parent / "scripts" / "performance" / "test_throughput_1v2.py"
    runpy.run_path(str(target), run_name="__main__")
