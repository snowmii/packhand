<a id="readme-top"></a>

<div align="center">

<h1>Packhand</h1>

<p>
  A minecraft resourcepack QoL mod 
  <br />
  <a href="https://modrinth.com/mod/packhand">Modrinth</a>
  &middot;
  <a href="../../issues/new?labels=bug">Report Bug</a>
  &middot;
  <a href="../../issues/new?labels=enhancement">Request Feature</a>
</p>

</div>

Tired of vanilla resourcepack screen making you click tiny arrow buttons 20 times
to move a pack to where you want it to be? <br />
Sick of re-adding all the packs you use regularly after a somehow failed resourcepack loading?  <br />
Wanna switch between tens and hundreds of packs in just a snap of the finger? <br />
Packhand is exactly here to save your hand.

It allows you to:
* Drag resourcepacks around
* Save the packs you use into presets and load them all at once.

And it supports:
* Minecraft 26.1.2 and 26.2 with Fabric Loader 0.19.3 or newer

TODOS:
* Import/export pack presets
* or request new features <a href="../../issues/new?labels=enhancement">here</a>
* 1.21.x support

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

## License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for details.

## Powered by

* [FabricMC](https://fabricmc.net/)
* [Stonecutter](https://stonecutter.kikugie.dev/) for multi-version supports

<p align="right">(<a href="#readme-top">back to top</a>)</p>
