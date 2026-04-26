# 🚀 Tron Lightcycles — Setup & Run Guide

A complete guide to **set up, run, and contribute** to the Tron Lightcycles JavaFX project across **Windows, Linux (WSL), and macOS**.

---

# 📌 Project Overview

**Project Name:** `tron-lightcycles`
**Repository:** [https://github.com/iangithub05/tron-lightcycles.git](https://github.com/iangithub05/tron-lightcycles.git)
**Stack:**

* Java 17
* JavaFX
* Maven

---

# 🧰 1. Environment Setup

---

# 🪟 WINDOWS (WSL - Recommended)

---

## 🔹 1. Install WSL (if not yet installed)

Open PowerShell as Admin:

```powershell
wsl --install
```

Restart your computer if prompted.

---

## 🔹 2. Install Dependencies (WSL Ubuntu)

Open WSL terminal:

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven -y
```

Verify:

```bash
java -version
javac -version
mvn -version
```

---

## 🔹 3. GUI Support

### ✔️ Windows 11

WSLg is built-in (no setup needed)

Test:

```bash
sudo apt install x11-apps -y
xclock
```

---

### ✔️ Windows 10

Install VcXsrv and run it, then:

```bash
export DISPLAY=:0
```

---

---

# 🐧 LINUX (Native Ubuntu/Debian)

---

## 🔹 Install Dependencies

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven openjfx -y
```

Verify:

```bash
java -version
mvn -version
```

---

## 🔹 GUI

Works out of the box on most desktop environments.

---

---

# 🍎 MACOS

---

## 🔹 1. Install Homebrew (if not installed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

---

## 🔹 2. Install Dependencies

```bash
brew install openjdk@17 maven
```

Link Java:

```bash
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

Verify:

```bash
java -version
mvn -version
```

---

## 🔹 3. JavaFX Notes

JavaFX is managed via Maven dependencies → no manual install needed.

---

# 📥 2. Clone the Project

```bash
git clone https://github.com/iangithub05/tron-lightcycles.git
cd tron-lightcycles
```

---

# 💻 3. Open in VS Code (Optional but Recommended)

```bash
code .
```

Recommended extensions:

* Extension Pack for Java
* Maven for Java

---

# ▶️ 4. Run the Application

```bash
mvn clean javafx:run
```

A JavaFX window should appear.

---

# 🧪 5. Useful Commands

---

## 🔹 Build Project

```bash
mvn clean install
```

---

## 🔹 Run Project

```bash
mvn javafx:run
```

---

## 🔹 Debug (VS Code)

* Open main class
* Press **Run** or **Debug**

---

# 🗂️ 6. Project Structure

```text
tron-lightcycles/
 └── src/
     └── main/
         ├── java/
         │    └── com/tron/
         │         ├── Main.java
         │         ├── controllers/
         │         ├── models/
         │         ├── services/
         │         └── utils/
         │
         └── resources/
              └── com/tron/
                   ├── views/
                   ├── styles/
                   └── assets/
```

---

# 🧠 7. Code Organization Guide

---

## 🎮 controllers/

Handles JavaFX UI logic and interactions.

---

## 📊 models/

Contains data structures and game entities:

* Player
* Lightcycle
* Grid

---

## ⚙️ services/

Core game logic:

* Game loop
* Collision detection
* Movement system

---

## 🧩 utils/

Reusable helper functions and utilities.

---

## 🎨 resources/

* `views/` → UI layouts (if used)
* `styles/` → CSS
* `assets/` → images, sounds

---

# ⚠️ 8. Rules & Best Practices

* Keep logic out of UI classes
* Follow package structure strictly
* Use Maven commands to run/build
* Do not place `.java` files outside `src/main/java`

---

# ❌ 9. Troubleshooting

---

## JavaFX window not appearing

Check GUI:

```bash
echo $DISPLAY
```

---

## Maven build issues

```bash
mvn clean install
```

---

## Java not detected

```bash
javac -version
```

If missing:

```bash
sudo apt install openjdk-17-jdk
```

---

# 🤝 10. Contribution Guide

---

## 🔹 Branch Naming

Format:

```text
<surname>/<feature>
```

Example:

```text
arragona/game-loop
```

---

## 🔹 Workflow

1. Fork or clone the repository
2. Create a new branch:

```bash
git checkout -b <surname>/<feature>
```

3. Make changes and commit:

```bash
git add .
git commit -m "Add: <feature description>"
```

4. Push branch:

```bash
git push origin <surname>/<feature>
```

5. Create a Pull Request → **target: `staging` branch**

---

## 🔹 Commit Message Style

```text
Add: feature
Fix: bug
Refactor: code cleanup
```

---

# 🚀 11. Quick Start

```bash
# Install dependencies (Linux/WSL)
sudo apt update
sudo apt install openjdk-17-jdk maven -y

# Clone project
git clone https://github.com/iangithub05/tron-lightcycles.git
cd tron-lightcycles

# Run app
mvn clean javafx:run
```

---

# 📌 Notes

* JavaFX is handled via Maven (no manual setup required)
* GUI support is required (WSLg or system GUI)
* Ensure Java 17 is used

---
