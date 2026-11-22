# Paczka modów (Minecraft 1.20.1 Forge)

Wspólna paczka modów Marcina. Po instalacji sama się aktualizuje: przy każdym uruchomieniu gry
pobiera nowe i zmienione mody, więc nie trzeba niczego ściągać ręcznie.

## Instalacja (raz)

1. Zainstaluj **Prism Launcher**: https://prismlauncher.org/download/ i zaloguj się kontem Microsoft.
2. Pobierz plik **[Paczka-modow-Prism.zip](https://github.com/Amaystrong/testowanie-md-w/raw/main/prism/Paczka-modow-Prism.zip)**.
3. W Prism kliknij **Dodaj instancję → Importuj** i wskaż pobrany plik zip (albo po prostu przeciągnij zip na okno Prism).
4. Uruchom instancję **Paczka modow**. Za pierwszym razem pojawi się okienko, które pobiera mody (kilka minut, ok. 110 MB). Potem gra wystartuje sama.

Gotowe. Od teraz przy każdym starcie gry paczka sprawdza aktualizacje.

Paczki tekstur są włączone od razu przy pierwszej instalacji. Twoje ustawienia (klawisze, grafika) nie są potem nadpisywane.

## Mapy (opcjonalnie, raz)

Mapy nie są aktualizowane automatycznie, bo nadpisałoby to Twoje postępy w grze. Ściągasz je raz:

- **New World** (duża mapa) jest podzielona na dwie części, pobierz obie:
  [część 1](https://github.com/Amaystrong/testowanie-md-w/raw/main/mapy/New%20World%20-%20czesc%201%20z%202.zip),
  [część 2](https://github.com/Amaystrong/testowanie-md-w/raw/main/mapy/New%20World%20-%20czesc%202%20z%202.zip)
- **New World (1)** (mała mapa): [pobierz](https://github.com/Amaystrong/testowanie-md-w/raw/main/mapy/New%20World%20(1).zip)

Jak wgrać: w Prism kliknij prawym na instancję → **Folder** → wejdź do `saves` (jeśli go nie ma, utwórz).
Rozpakuj tam oba pliki części mapy New World (Windows zapyta o scalenie folderów: wybierz **Tak**).
Mała mapa to jeden zip, rozpakuj go tak samo.

## Co jest w paczce

Mody: Aim Lab, Holoport, Path Marker, Police Car, Zegar cyfrowy, Noclip (mody Marcina), oraz Axiom,
Sinytra Connector, Essential, Forgified Fabric API, CustomNPCs (Unofficial), Techguns (Unofficial Port),
MrCrayfish's Vehicle (Unofficial), Framework, Entity Model/Texture Features + EMF Compat, ModernFix, FerriteCore.

## Dla Marcina: jak dodać nową wersję moda

- Własny mod: `narzedzia/podmien-mod.sh nowy-plik.jar`, potem commit i push. Stary jar trafia do `stare-wersje/` jako `.jar---` (nic nie jest kasowane).
- Mod z Modrinth/CurseForge: `packwiz mr add <nazwa>` albo `packwiz cf add <nazwa>`.
- Znajomi dostaną zmiany przy następnym uruchomieniu gry.
