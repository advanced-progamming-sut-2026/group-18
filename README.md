# Plants vs. Zombies 2: LibGDX Edition 🌻🧟‍♂️
**An Advanced Programming Final Project by Team Compile or Die!**

A Java-based 2D tower defense game built from the ground up using the LibGDX framework. This project successfully recreates the core mechanics of *Plants vs. Zombies 2*, featuring grid-based plant placement, dynamic wave generation, and intelligent enemy pathing, all strictly adhering to a clean Model-View-Controller (MVC) architectural pattern.

## 👥 Team Compile or Die!
| Name | Student ID |
| :--- | :--- |
| Seyed Mahdi Abedi | 404106066 |
| Arsam Kooshky | 404171199 |
| Seyed Amir Abbas Naghavi | 404106474 |

## 🚀 Features
* **Classic Grid Combat:** Plant sunflowers, peashooters, and defensive units on a multi-lane lawn to fend off incoming hordes.
* **Dynamic Wave Management:** A custom `WaveManager` system that dynamically spawns varying zombie types based on stage progression and difficulty.
* **Strategy Pattern AI:** Zombie behaviors and plant attack types are decoupled using standard OOP design patterns, allowing for easy expansion of new unit types without bloated code.
* **Scalable UI:** A crisp, responsive user interface utilizing LibGDX's Scene2D and NinePatch textures to maintain quality across different monitor resolutions.
* **Cross-Platform Ready:** Built with Gradle and LWJGL3, ensuring the project can easily compile for Desktop and be extended for other platforms in the future.

## 🛠️ Tech Stack & Architecture
* **Language:** Java 25
* **Game Engine:** LibGDX (v1.14.2)
* **Build System:** Gradle
* **Architecture:** Model-View-Controller (MVC)
    * **Models:** Pure data classes (Plants, Zombies, Level Data). Zero I/O or rendering logic.
    * **Views:** Scene2D Stages and SpriteBatches rendering `assets/` to the screen.
    * **Controllers:** Input handlers, collision detection, and game loop state management.

## 📁 Repository Structure
The project utilizes a standard LibGDX multi-module layout:

* `assets/`: Contains all raw game data, including textures (`.png`), UI skins (`uiskin.json`, `.atlas`), TrueType fonts (`.ttf`), and audio.
* `core/`: The heart of the game. Contains all MVC Java packages, Screen managers, and game logic.
* `lwjgl3/`: The desktop launcher module. Handles window configuration (resolution, framerate, vsync) and booting the game on PC.

## 💻 Getting Started (For Developers)

### Prerequisites
* **JDK 25** installed and configured.
* An IDE with excellent Gradle support (IntelliJ IDEA is highly recommended).
* Git for version control.
* Clone the repository and get started

## 📝 License
* This project is created for educational purposes as a university final project. All Plants vs. Zombies IP, characters, and concepts are the property of PopCap Games and Electronic Arts.
