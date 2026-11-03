from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
RESOURCE_ROOTS = (
    ROOT / "src/main/resources",
    ROOT / "src/generated/resources",
)
CLIENT_ROOT = ROOT / "src/main/java/dev/visfox/forbiddenreforged/client"
CLIENT_EVENTS = CLIENT_ROOT / "FmClientEvents.java"
TAINT_LOG_MODEL = CLIENT_ROOT / "TaintLogFaceModel.java"


def resource_parts(identifier: str, default_namespace: str) -> tuple[str, str] | None:
    value = identifier.strip()
    if not value or value.startswith("#"):
        return None
    if ":" in value:
        namespace, path = value.split(":", 1)
    else:
        namespace, path = default_namespace, value
    if not namespace or not path or path.startswith("/") or ".." in Path(path).parts:
        return None
    return namespace, path


def read_json(path: Path, failures: list[str]) -> Any | None:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        failures.append(f"{path.relative_to(ROOT)}: invalid JSON/resource: {error}")
        return None


def collect_model_references(value: Any, output: list[str]) -> None:
    if isinstance(value, dict):
        model = value.get("model")
        if isinstance(model, str):
            output.append(model)
        for nested in value.values():
            collect_model_references(nested, output)
    elif isinstance(value, list):
        for nested in value:
            collect_model_references(nested, output)


def main() -> int:
    failures: list[str] = []
    json_count = 0
    models: dict[tuple[str, str], Path] = {}
    textures: dict[tuple[str, str], Path] = {}
    parsed_json: dict[Path, Any] = {}

    for resource_root in RESOURCE_ROOTS:
        if not resource_root.is_dir():
            continue
        for path in sorted(resource_root.rglob("*.json")):
            json_count += 1
            document = read_json(path, failures)
            if document is None:
                continue
            parsed_json[path] = document
            relative = path.relative_to(resource_root)
            if len(relative.parts) >= 3 and relative.parts[1] == "models":
                namespace = relative.parts[0]
                model_path = Path(*relative.parts[2:]).with_suffix("").as_posix()
                models[(namespace, model_path)] = path

        assets_root = resource_root / "assets"
        if not assets_root.is_dir():
            continue
        for path in sorted(assets_root.glob("*/textures/**/*.png")):
            relative = path.relative_to(assets_root)
            namespace = relative.parts[0]
            texture_path = Path(*relative.parts[2:]).with_suffix("").as_posix()
            textures[(namespace, texture_path)] = path

    # Check all local blockstate model references and all local model parents,
    # override models, and texture bindings. External Minecraft/Thaumcraft
    # resources are intentionally resolved by their owning runtime/mod.
    checked_model_refs = 0
    checked_texture_refs = 0
    for path, document in parsed_json.items():
        relative = None
        for resource_root in RESOURCE_ROOTS:
            try:
                relative = path.relative_to(resource_root / "assets")
                break
            except ValueError:
                continue
        if relative is None or len(relative.parts) < 3:
            continue

        namespace = relative.parts[0]
        kind = relative.parts[1]
        if kind not in {"models", "blockstates"}:
            continue

        references: list[str] = []
        if kind == "blockstates":
            collect_model_references(document, references)
        else:
            parent = document.get("parent") if isinstance(document, dict) else None
            if isinstance(parent, str):
                references.append(parent)
            if isinstance(document, dict):
                for override in document.get("overrides", []):
                    if isinstance(override, dict) and isinstance(override.get("model"), str):
                        references.append(override["model"])

            texture_map = document.get("textures", {}) if isinstance(document, dict) else {}
            if isinstance(texture_map, dict):
                for texture in texture_map.values():
                    if not isinstance(texture, str) or texture.startswith("#"):
                        continue
                    parts = resource_parts(texture, namespace)
                    if parts is None:
                        failures.append(f"{path.relative_to(ROOT)}: malformed texture id {texture!r}")
                        continue
                    texture_namespace, texture_path = parts
                    if texture_namespace != "forbiddenmagic":
                        continue
                    checked_texture_refs += 1
                    key = (texture_namespace, texture_path.removesuffix(".png"))
                    if key not in textures:
                        failures.append(
                            f"{path.relative_to(ROOT)}: missing texture {texture_namespace}:{texture_path}"
                        )

        for reference in references:
            parts = resource_parts(reference, namespace)
            if parts is None:
                failures.append(f"{path.relative_to(ROOT)}: malformed model id {reference!r}")
                continue
            model_namespace, model_path = parts
            if model_namespace != "forbiddenmagic":
                continue
            checked_model_refs += 1
            if (model_namespace, model_path) not in models:
                failures.append(
                    f"{path.relative_to(ROOT)}: missing model {model_namespace}:{model_path}"
                )

    # Every custom item predicate must have a client-side property provider,
    # and every registered provider must have a matching item-model override.
    events_text = CLIENT_EVENTS.read_text(encoding="utf-8") if CLIENT_EVENTS.is_file() else ""
    registrations: set[tuple[str, str]] = set()
    registration_pattern = re.compile(
        r'ItemProperties\.register\(\s*(.+?),\s*ForbiddenMagic\.id\("([^"]+)"\)',
        re.DOTALL,
    )
    for match in registration_pattern.finditer(events_text):
        item_expression, predicate_path = match.groups()
        if "DEADLY_SHARDS" in item_expression:
            shard = re.search(r'DEADLY_SHARDS\.get\("([^"]+)"\)', item_expression)
            if shard is None:
                failures.append(f"cannot resolve item expression for predicate {predicate_path}")
                continue
            item_name = shard.group(1) + "_shard"
        else:
            item_match = re.search(r"FmItems\.([A-Z0-9_]+)\.get\(\)", item_expression)
            if item_match is None:
                failures.append(f"cannot resolve item expression for predicate {predicate_path}")
                continue
            item_name = item_match.group(1).lower()
        registrations.add((item_name, f"forbiddenmagic:{predicate_path}"))

    model_predicates: set[tuple[str, str]] = set()
    for (namespace, model_path), model_file in models.items():
        if namespace != "forbiddenmagic" or not model_path.startswith("item/"):
            continue
        document = parsed_json.get(model_file)
        if not isinstance(document, dict):
            continue
        for override in document.get("overrides", []):
            if not isinstance(override, dict) or not isinstance(override.get("predicate"), dict):
                continue
            for predicate in override["predicate"]:
                if predicate.startswith("forbiddenmagic:"):
                    model_predicates.add((model_path.removeprefix("item/"), predicate))

    for item_name, predicate in sorted(registrations):
        if (item_name, predicate) not in model_predicates:
            failures.append(
                f"FmClientEvents registers {predicate} for {item_name}, "
                "but its item model has no matching override"
            )
    for item_name, predicate in sorted(model_predicates):
        if (item_name, predicate) not in registrations:
            failures.append(
                f"item/{item_name} declares {predicate}, but FmClientEvents has no matching provider"
            )

    # The dynamic Taint Log loader and all baked submodels must be registered
    # and present; otherwise these errors only appear when the client bakes models.
    custom_loader_models = [
        (path, document.get("loader"))
        for path, document in parsed_json.items()
        if isinstance(document, dict) and isinstance(document.get("loader"), str)
    ]
    if custom_loader_models:
        if "TaintLogFaceModel.Loader.ID" not in events_text or "TaintLogFaceModel.Loader.INSTANCE" not in events_text:
            failures.append("custom geometry loader exists but is not registered in FmClientEvents")
        if not TAINT_LOG_MODEL.is_file():
            failures.append("custom geometry loader references a missing TaintLogFaceModel implementation")
        elif "taint_log_faces" not in TAINT_LOG_MODEL.read_text(encoding="utf-8"):
            failures.append("TaintLogFaceModel does not declare the expected taint_log_faces loader id")

    if TAINT_LOG_MODEL.is_file():
        model_text = TAINT_LOG_MODEL.read_text(encoding="utf-8")
        for model_path in re.findall(r'ForbiddenMagic\.id\("(block/[^"]+)"\)', model_text):
            if ("forbiddenmagic", model_path) not in models:
                failures.append(f"TaintLogFaceModel bakes missing model forbiddenmagic:{model_path}")

    # Confirm the block entity's visual renderer is wired at the client event layer.
    renderer_path = CLIENT_ROOT / "WrathCageRenderer.java"
    if renderer_path.is_file():
        if "registerBlockEntityRenderer(FmBlockEntities.WRATH_CAGE.get(), WrathCageRenderer::new)" not in events_text:
            failures.append("WrathCageRenderer exists but is not registered for the Wrath Cage block entity")
    elif "registerBlockEntityRenderer(FmBlockEntities.WRATH_CAGE.get(), WrathCageRenderer::new)" in events_text:
        failures.append("FmClientEvents registers a missing WrathCageRenderer class")

    if failures:
        print("Client asset integrity check failed:")
        print("\n".join(failures))
        return 1

    print(
        "Client asset integrity check passed "
        f"({json_count} JSON resources, {checked_model_refs} local model references, "
        f"{checked_texture_refs} local texture references, "
        f"{len(registrations)} item predicates, {len(custom_loader_models)} custom loaders)."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
