#!/usr/bin/env bash
# Podmienia wersje wlasnego moda w paczce (np. nowy Aim Lab).
# Uzycie: narzedzia/podmien-mod.sh sciezka/do/aimlab-1.18.0.jar
# - stary jar z mods/ trafia do stare-wersje/ jako .jar--- (nigdy nie kasujemy starych wersji)
# - nowy jar trafia do mods/, potem packwiz refresh
set -euo pipefail
cd "$(dirname "$0")/.."
new="$1"; name="$(basename "$new")"
prefix="$(echo "$name" | sed -E 's/-[0-9][0-9.]*\.jar$//')"
for old in mods/"$prefix"-[0-9]*.jar; do
  [ -e "$old" ] || continue
  [ "$(basename "$old")" = "$name" ] && continue
  mv "$old" "stare-wersje/$(basename "$old")---"
  echo "stara wersja -> stare-wersje/$(basename "$old")---"
done
cp "$new" "mods/$name"
packwiz refresh
echo "gotowe: mods/$name"
