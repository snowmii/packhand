<a id="readme-top"></a>

<div align="center">
<h1>Packhand</h1>

<p>
  A minecraft resourcepack QoL mod

[![Release](https://img.shields.io/github/v/release/snowmii/packhand?style=for-the-badge&logo=github&color=blue)](https://github.com/snowmii/packhand/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/snowmii/packhand/ci.yml?branch=master&style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/snowmii/packhand/actions/workflows/ci.yml)
[![Modrinth](https://img.shields.io/modrinth/dt/packhand?style=for-the-badge&logo=modrinth&color=00AF5C&label=downloads)](https://modrinth.com/mod/packhand)
[![License](https://img.shields.io/github/license/snowmii/packhand?style=for-the-badge&color=green)](LICENSE)

[![Minecraft](https://img.shields.io/badge/minecraft-26.1.2%20%7C%2026.2-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/packhand/versions)
[![Fabric](https://img.shields.io/badge/loader-fabric%200.19.3%2B-DBD0B4?style=for-the-badge&logo=fabricmc&logoColor=white)](https://fabricmc.net/)
</p>

</div>

Tired of the vanilla resource pack screen making you click tiny arrow buttons 20 times just to move a pack where you want it? <br />
Sick of re-adding all your favorite packs and re-ordering them after a resource pack loading failure? <br />
Want to switch between dozens—or even hundreds—of packs in the blink of an eye? <br />

Packhand is here to save your hand!


It lets you:
* Drag resourcepacks around with beautiful animations.
* Save your favorite pack combinations as presets and load them with just a single click.

And it supports:
* Minecraft 26.1.2 and 26.2 with Fabric Loader 0.19.3 or newer

## Getting Started

Download the latest release from [Modrinth](https://modrinth.com/mod/packhand) or [releases](https://github.com/snowmii/packhand/releases).
1. Install Fabric Loader for your Minecraft version.
2. Drop Fabric API into your `mods` folder.
3. Drop `packhand-<version>.jar` into your `mods` folder.
4. Launch the game.
5. And enjoy.

## Building From Source

```sh
git clone https://github.com/snowmii/packhand.git
cd packhand
./gradlew build
```

Jars land in `versions/<mc-version>/build/libs/`.

The project uses Stonecutter, so `./gradlew build` builds every target version.
To work against a single version:

```sh
./gradlew "Set active project to 26.2"
./gradlew :26.2:runClient
```

## Powered by

* [FabricMC](https://fabricmc.net/)
* [Stonecutter](https://stonecutter.kikugie.dev/) for multi-version supports

## TODOS

* Import/export pack presets
* or request new features <a href="../../issues/new?labels=enhancement">here</a>
* 1.21.x support

<p align="right">(<a href="#readme-top">back to top</a>)</p>
