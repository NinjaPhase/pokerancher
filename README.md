# Pokemon Rancher (PokeWilds)

A Pokémon-inspired game built with LibGDX featuring procedural world generation
and multiplayer capabilities.

## Project Overview

Pokemon Rancher is an open-source game inspired by the Pokémon franchise. The game features a procedurally generated
world where players can explore, catch and battle with Pokémon. It supports both single-player and multiplayer modes,
allowing players to host or join game sessions.

## Features

- **Procedural World Generation**: Dynamic world creation with different biomes
- **Multiplayer Support**: Host your own server or join existing ones
- **Mod Support**: Extend the game with custom mods
- **Pokemon System**: Catch, train, and battle with Pokémon
- **Cross-platform**: Built with LibGDX for compatibility across different platforms

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Gradle build system

### Building the Project

1. Clone the repository
2. Navigate to the project directory
3. Build using Gradle:

```bash
./gradlew build
```

### Running the Game

#### Desktop

```bash
./gradlew desktop:run
```

#### Server Mode

To run as a headless server:

```bash
./gradlew desktop:run --args="--headless"
```

## Project Structure

- **api/**: Core API interfaces and classes
- **core/**: Main game logic and mechanics
    - **com.pokewilds**: Main package containing game code
    - **com.pokewilds.pokemon**: Pokemon-related classes
    - **com.pokewilds.world**: World generation and management
    - **com.pokewilds.scene**: Game scenes and UI management
- **desktop/**: Desktop-specific implementation
- **assets/**: Game resources (graphics, audio, data files)
- **assetpack/**: Raw asset files for processing
- **mods/**: Directory for mod storage
- **sample/**: Sample mod implementation
- **tools/**: Utility tools for development
- **worlds/**: Generated world data
- **saves/**: Player save files

## Game Modes

- **Single Player**: Explore and play in your own world
- **Host Game**: Host a server for other players to join
- **Client Game**: Connect to existing game servers

## Modding

Pokemon Rancher supports modding through its mod system. Mods can be placed in the `mods/` directory or loaded
programmatically. The sample mod provides an example of how to create mods for the game.

### Creating a Mod

1. Create a new project that depends on the API module
2. Implement the necessary interfaces
3. Build your mod and place it in the `mods/` directory

## Technical Details

### Technologies Used

- **LibGDX**: Cross-platform game development framework
- **Gradle**: Build automation tool
- **Java**: Primary programming language

### AI Notice

This project utilises AI to generate game code, all content is checked and
verified by a Software Engineer.

## Inspiration

Inspired by [pokewilds](https://github.com/SheerSt/pokewilds)
