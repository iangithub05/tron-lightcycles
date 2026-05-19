# 🚀 Tron Lightcycles

A JavaFX-based multiplayer game built with Maven.

📚 **CMSC 137** — Project Submission

---

## 👥 Contributors

| Name |
|------|
| Ian Tristan Arragona |
| Joy Guevarra |
| Vince Tabelisma |

---

## 📌 Documentation

- 📦 Setup Guide → [`docs/setup-windows.md`](docs/setup-windows.md), [`docs/setup-macos.md`](docs/setup-macos.md), [`docs/setup-linux.md`](docs/setup-linux.md)
- ▶️ Running the App → [`docs/running-the-app.md`](docs/running-the-app.md)
- 🗂️ Project Structure → [`docs/project-structure.md`](docs/project-structure.md)
- 🔀 Branching Rules → [`docs/branching-policy.md`](docs/branching-policy.md)

---

## 📥 Repository

```
https://github.com/iangithub05/tron-lightcycles.git
```

---

## ▶️ How to Run

### Option 1: Run the Executable JAR *(Recommended for Users)*

Requires **Java 21 or newer**.

```bash
java -jar target/tron-lightcycles-game.jar
```

If the JAR is in a different folder:

```bash
java -jar path/to/tron-lightcycles-game.jar
```

---

### Option 2: Run Using Maven *(For Developers)*

**Prerequisites:**
- Java 21+
- Maven

Clone the repository and run:

```bash
git clone https://github.com/iangithub05/tron-lightcycles.git
cd tron-lightcycles
mvn javafx:run
```

This builds and launches the JavaFX application directly from source.

---

## 🛠 Troubleshooting

### JavaFX runtime components are missing

If you see:

```
Error: JavaFX runtime components are missing, and are required to run this application
```

The executable JAR was not packaged with JavaFX dependencies. Use Maven to run instead:

```bash
mvn javafx:run
```

---

### `java` is not recognized

Check your Java installation:

```bash
java --version
```

If Java is not installed, download and install **Java 21 or newer**.

---

### `mvn` is not recognized

Check your Maven installation:

```bash
mvn --version
```

If Maven is not installed, download [Apache Maven](https://maven.apache.org/) and ensure it is added to your system `PATH`.
