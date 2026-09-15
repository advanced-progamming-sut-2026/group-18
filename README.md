# Plants vs. Zombies 2 — LibGDX Edition 🌻🧟‍♂️

**An Advanced Programming Final Project by Team Compile or Die**

A Java-based 2D tower-defense game inspired by *Plants vs. Zombies 2*, developed as a three-person team project for the Advanced Programming course at Sharif University of Technology.

The game was developed from the ground up using the **LibGDX framework**, with a focus on object-oriented design, modular gameplay systems, separation of responsibilities, and maintainable game architecture.

---

## 👥 Team Compile or Die

This project was developed collaboratively by three team members.

The implementation was divided into three major development areas, with each team member taking primary responsibility for approximately one-third of the overall project.

Each member independently developed their assigned subsystem and participated in integration, debugging, refactoring, and final delivery.

| Name                         | Student ID |
| :--------------------------- | :--------: |
| **Seyed Mahdi Abedi**        |  404106066 |
| **Arsam Kooshky**            |  404171199 |
| **Seyed Amir Abbas Naghavi** |  404106474 |

---

## 🎮 Project Overview

The project recreates the core gameplay experience of *Plants vs. Zombies 2* as a desktop Java game.

The game features a lane-based battlefield where plants are placed strategically to defend against waves of zombies with different attributes, behaviors, and abilities.

The project includes dedicated systems for entities, gameplay logic, zombie behavior, plant interactions, resource management, game screens, user interfaces, and game-state coordination.

---

## ✨ Main Features

### 🌱 Plant & Zombie Gameplay

* Multiple plant and zombie types
* Different health, damage, movement, and attack characteristics
* Plant–zombie interactions
* Specialized entity behaviors
* Extensible entity-oriented architecture

### 🧟 Zombie System

The zombie subsystem contains a variety of zombie types with different gameplay characteristics and specialized abilities.

The system supports:

* Different zombie attributes and statistics
* Specialized zombie behaviors
* Zombie movement and attack logic
* Plant interaction and damage handling
* Zombie-specific abilities
* Different zombie categories and gameplay roles
* Integration with spawning and wave-management systems

### 🌊 Wave & Spawn Management

A dedicated wave-management system controls the progression and spawning of zombies throughout gameplay.

Zombie composition and spawning can be coordinated according to the current stage and game state.

### ☀️ Economy & Resource Management

The game contains a dedicated economy system responsible for managing resources and resource-related gameplay events, including:

* Sun generation
* Sun collection
* Resource spending
* Special resource events
* Integration with plant placement and gameplay systems

### 🎯 Gameplay Services

Game systems are coordinated through dedicated managers and services where appropriate, reducing unnecessary coupling between individual entities and allowing gameplay responsibilities to remain modular.

### 🖥️ User Interface

The project uses **LibGDX Scene2D** and related UI components to implement:

* Main menus
* Game screens
* HUD elements
* Interactive UI components
* Buttons and menus
* Game-state-related screens

### 🖼️ Rendering

The game uses LibGDX rendering systems including:

* `SpriteBatch`
* Texture and texture-region based rendering
* Scene2D
* UI skins and atlases
* Custom game rendering components

---

# 🛠️ Technology Stack

* **Language:** Java
* **Game Framework:** LibGDX
* **Desktop Backend:** LWJGL3
* **Build System:** Gradle
* **Version Control:** Git / GitHub
* **IDE:** IntelliJ IDEA
* **Architecture:** MVC-oriented architecture
* **Programming Paradigm:** Object-Oriented Programming

---

# 🧱 Architecture

The project follows an **MVC-oriented architecture** to separate game data, presentation, and gameplay coordination.

## Model

The model layer contains the core game entities and gameplay data, including:

* Plants
* Zombies
* Game board
* Player/game data
* Economy systems
* Gameplay state
* Entity attributes and statistics

The model layer is designed to keep core game data and behavior separated from rendering concerns.

## View

The view layer is responsible for presenting the game through LibGDX, including:

* Game screens
* Menus
* HUD
* Scene2D components
* Sprite rendering
* Texture and asset presentation

## Controller & Services

Controllers and dedicated services coordinate gameplay operations such as:

* User input
* Game-loop updates
* Entity interactions
* Game-state transitions
* Zombie management
* Wave management
* Economy management
* Gameplay coordination

This separation helps prevent individual classes from becoming responsible for unrelated parts of the game and makes the system easier to extend and maintain.

---

# 📁 Repository Structure

```text
.
├── assets/
│   ├── textures/
│   ├── atlases/
│   ├── fonts/
│   └── sounds/
│
├── core/
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── compileordie/
│                       └── pvz2/
│
├── lwjgl3/
│   └── src/
│       └── main/
│           └── java/
│
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

### `assets/`

Contains the game's visual and audio resources, including textures, texture atlases, fonts, UI resources, and sound effects.

### `core/`

Contains the main game implementation, including models, views, controllers, gameplay services, entities, screens, and game systems.

### `lwjgl3/`

Contains the desktop launcher and LWJGL3-specific configuration required to run the game on desktop systems.

---

# 🤝 Team Contributions

The project was divided into three major development areas.

Each team member had **primary responsibility for approximately one-third of the complete implementation**, while all members also participated in integration, debugging, refactoring, and final delivery.

## 🧟 Seyed Amir Abbas Naghavi

**Primary responsibility: Complete Zombie Subsystem**

Responsible for the complete design, implementation, integration, and refinement of the zombie-related part of the project.

Key responsibilities included:

* Designing and implementing the base zombie architecture
* Implementing different zombie types
* Developing specialized zombie behaviors
* Implementing zombie movement and combat logic
* Implementing zombie health and damage systems
* Developing zombie–plant interactions
* Implementing zombie-specific abilities and mechanics
* Integrating zombies with the game board and gameplay systems
* Integrating zombie systems with spawning and wave management
* Debugging and refining zombie-related gameplay
* Refactoring zombie code to improve maintainability and extensibility
* Integrating the completed zombie subsystem with the other major parts of the game

The zombie subsystem was developed as a complete, independently structured part of the game and then integrated with the systems developed by the other team members.

## 🌱 Seyed Mahdi Abedi

**Primary responsibility: Assigned core gameplay subsystem**

Responsible for the complete development of the assigned major subsystem, including its design, implementation, integration, debugging, refactoring, and final refinement.

## 🎮 Arsam Kooshky

**Primary responsibility: Assigned core gameplay subsystem**

Responsible for the complete development of the assigned major subsystem, including its design, implementation, integration, debugging, refactoring, and final refinement.

---

# 🔄 Development History

The project was initially developed on another collaborative development platform and was later transferred to GitHub.

As a result, the GitHub repository does not necessarily contain the complete historical development record of the project.

The GitHub repository represents the final integrated project and its subsequent development history.

---

# 💻 Getting Started

## Prerequisites

* JDK compatible with the project configuration
* IntelliJ IDEA or another IDE with Gradle support
* Git
* A desktop environment capable of running the LibGDX LWJGL3 backend

## Clone the Repository

```bash
git clone https://github.com/advanced-progamming-sut-2026/group-18.git
```

Then open the project in IntelliJ IDEA and synchronize the Gradle configuration.

## Run the Game

Run the desktop launcher:

```text
lwjgl3:com.compileordie.pvz2.lwjgl3.Lwjgl3Launcher
```

---

# 📸 Screenshots & Gameplay

Screenshots and gameplay demonstrations can be added here to showcase the final game and its major systems.

Recommended demonstrations include:

* Main menu
* Gameplay screen
* Plant placement
* Zombie combat
* Special zombie abilities
* Wave progression
* HUD and resource management

---

# 🎓 Academic Context

This project was developed as a **final project for the Advanced Programming course at Sharif University of Technology**.

The project was designed and implemented as a collaborative software-engineering and game-development exercise, with emphasis on:

* Object-Oriented Programming
* Software architecture
* Modular design
* Team-based development
* Version control
* Game-system implementation
* Debugging and refactoring

---

# 📄 License & Intellectual Property

The original source code developed by the team is provided for educational and portfolio purposes.

The *Plants vs. Zombies* name, characters, visual identity, original game concepts, and related intellectual property belong to their respective owners, including PopCap Games and Electronic Arts.

This project is an educational, non-commercial implementation inspired by the original game and is **not affiliated with, sponsored by, or endorsed by the original rights holders**.
