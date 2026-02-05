# Scorpion Minesweeper

**A cooperative, two-player strategy game merging classic Minesweeper logic with trivia challenges and RPG mechanics.**

## Project Overview
Scorpion Minesweeper is a Java-based desktop application developed using the **Model-View-Controller (MVC)** architectural pattern. Unlike the traditional solitary game, this project introduces a **cooperative multiplayer mode** where two players share a life pool and score, requiring communication and strategy to win.

The project was executed under a strict **Agile/Scrum** methodology over three iterations, simulating a full software development lifecycle (SDLC) from requirements gathering (SRS) to final delivery.

## System Features

### Core Gameplay
* **Co-op Multiplayer:** Two active boards with synchronized resources, including shared lives and scores.
* **Trivia Integration:** Special "Question Cells" trigger multiple-choice queries. Correct answers grant point bonuses, while incorrect answers result in life penalties.
* **RPG Mechanics:** "Surprise Cells" introduce random elements, providing buffs (such as extra lives) or debuffs based on chance.

### My Contributions
* **Accessibility & Localization:** Engineered a multi-language interface supporting English, Hebrew, Russian, Spanish, and Arabic to broaden user inclusion.
* **Dynamic Translation API:** Integrated the **Microsoft Azure Translator API** to enable real-time, automated translation of new question content across all supported languages and CSV data files.
* **Smart Feedback System:** Implemented audio cues for game events and a "Smart Flag" validation system that prevents users from flagging more mines than statistically possible.

## Technical Architecture
* **Language:** Java (JDK 19)
* **GUI Framework:** Java Swing / AWT
* **Design Pattern:** Model-View-Controller (MVC)
* **Data Persistence:** Custom CSV implementation for game history and question banks
* **External APIs:** Microsoft Azure Translator
* **Testing:** JUnit 5 (Unit & Integration Testing)

## Installation and Execution
1.  **Prerequisites:** Ensure Java 19 or higher is installed on the local machine.
2.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/tamarkohan/Mine-Sweeper-Scorpion.git](https://github.com/tamarkohan/Mine-Sweeper-Scorpion.git)
    ```
3.  **Run the Application:**
    Navigate to the root directory and execute the launcher script:
    ```bash
    ./run-app.bat
    ```
    Alternatively, launch the JAR file directly:
    ```bash
    java -jar Mine_Sweeper_Scorpion.jar
    ```

## Future Roadmap
If development continues beyond the current scope, the following features are proposed:
* **RPG Customization:** Implementation of unlockable skins and character avatars.
* **Advanced Accessibility:** High-contrast modes, dynamic font resizing, and screen reader support.
* **Online Multiplayer:** Extension of cooperative logic to support remote play via sockets.

## Documentation
This project adheres to industry-standard documentation practices. Detailed reports are available in the `docs/` directory:
* **[SRS (Software Requirements Specification)](docs/SRS_Scorpion.pdf):** Functional breakdown and system requirements.
* **[SDP (Software Development Plan)](docs/SDP_Scorpion.pdf):** Agile planning, Gantt charts, and timeline.
* **[Iteration 3 Report](docs/iteration3_report_Scorpion.pdf):** Final delivery summary and testing metrics.
* **[Bonus Report](docs/Scorpion_Bonus.pdf):** Technical breakdown of API integration and advanced features.
* **[Original Requirements](docs/Mine-Sweeper_Project_Requirements.pdf):** Initial academic project scope.

## Credits
**Lead Developer:** Tamar Kohan
*Developed as part of the Information Systems Engineering program at the University of Haifa.*
