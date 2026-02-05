# 🦂 Scorpion Minesweeper

> A cooperative, two-player strategy game that merges classic Minesweeper logic with trivia challenges and RPG elements.

## 📖 Overview
Scorpion Minesweeper is a Java-based desktop application developed using the **MVC (Model-View-Controller)** architectural pattern. Unlike the traditional solitary game, this project introduces a **cooperative multiplayer mode** where two players share a life pool and score, requiring communication and strategy to win.

The project was executed under a strict **Agile/Scrum** methodology over 3 iterations, simulating a full software development lifecycle (SDLC) from requirements (SRS) to final delivery.

## ✨ Key Features
* **Co-op Multiplayer:** Two active boards with shared resources (Lives & Score).
* **Trivia Integration:** Special "Question Cells" trigger multiple-choice queries. Correct answers grant bonuses; wrong answers cost lives.
* **RPG Elements:** "Surprise Cells" provide random buffs (extra lives) or debuffs.
* **Accessibility:** Interface supported in five languages: English, Hebrew, Russian, Spanish, and Arabic.
* **Dynamic Translations (My Key Contribution):** Integrated **Microsoft Azure Translator API** to support real-time language switching for new questions added between all languages and CSVs.
* **Smart Feedback:** Audio cues for game events and "Smart Flags" that prevent flagging more mines than exist.

## 🛠️ Tech Stack
* **Language:** Java (JDK 19)
* **GUI:** Java Swing / AWT
* **Architecture:** Model-View-Controller (MVC)
* **Data Persistence:** CSV (Custom implementation for history & question banks)
* **APIs:** Microsoft Azure Translator
* **Testing:** JUnit (Unit & Integration Testing)

## 🚀 How to Run
1.  **Prerequisites:** Ensure you have Java 19 or higher installed.
2.  Clone the repository:
    ```bash
    git clone [https://github.com/tamarkohan/Mine-Sweeper-Scorpion.git](https://github.com/tamarkohan/Mine-Sweeper-Scorpion.git)
    ```
3.  Run the launcher script:
    ```bash
    ./run-app.bat
    ```
    *(Or run the JAR file directly)*: `java -jar Mine_Sweeper_Scorpion.jar`

## 🔮 Future Roadmap
If development were to continue beyond the semester time constraints, the following features are planned:
* **RPG Customization:** Unlockable skins and character avatars.
* **Advanced Accessibility:** High-contrast modes, dynamic font resizing, and voice-over support for visually impaired users.
* **Online Multiplayer:** Extending the cooperative logic to remote play via sockets.

## 📂 Project Documentation
This project follows industry-standard documentation practices. Detailed reports can be found in the `/docs` folder:
* **[SRS (Software Requirements Specification)](docs/SRS_Scorpion.pdf):** Full functional breakdown.
* **[SDP (Software Development Plan)](docs/SDP_Scorpion.pdf):** Agile planning and Gantt charts.
* **[Iteration 3 Report](docs/iteration3_report_Scorpion.pdf):** Final delivery summary and testing results.
* **[Bonus Report](docs/Scorpion_Bonus.pdf):** Detailed breakdown of creative bonuses and API integration.
* **[Original Requirements](docs/Mine-Sweeper_Project_Requirements.pdf):** Initial project scope.

## 👩‍💻 Credits
**Lead Developer:** Tamar Kohan
*Created as part of the Information Systems Engineering program at the University of Haifa.*