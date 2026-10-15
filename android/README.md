# Weapon Detection AI for Android

![License](https://img.shields.io/badge/License-MIT-blue.svg) ![Platform](https://img.shields.io/badge/Platform-Android-green.svg) ![Language](https://img.shields.io/badge/Language-Java-orange.svg)

A state-of-the-art, privacy-first security application for Android that leverages on-device artificial intelligence to provide real-time weapon detection.

## Screenshots

| Main Screen | Detection History | Settings | About |
| :---: | :---: | :---: | :---: |
| *(Image of Main Screen)* | *(Image of History Screen)* | *(Image of Settings Screen)* | *(Image of About Screen)* |

## Core Features

-   **Multi-Modal Detection**:
    -   **Live Camera**: Scans for threats in real-time using the device\'s camera.
    -   **Image Analysis**: Detects weapons in static images selected from the gallery.
    -   **Video Analysis**: Scans pre-recorded videos for potential threats.
-   **100% Offline & Private**: All AI processing is done locally on the user\'s device. No data is ever sent to a server, ensuring absolute privacy and functionality without an internet connection.
-   **Detection History**: A persistent log of all past detection events, complete with the object name, confidence score, and a precise timestamp.
-   **Advanced User Settings**:
    -   **Configurable Confidence Threshold**: Allows users to fine-tune the AI\'s sensitivity to balance detection accuracy against the risk of false positives.
    -   **Customizable Alerts**: Enable or disable sound and vibration alerts to suit the environment.
-   **Modern, Intuitive UI**: A sleek, dark-themed interface with neon accents for a professional and user-friendly experience.

## How It Works: The AI Detection Pipeline

The intelligence of the application is powered by a **YOLOv11** (You Only Look Once) model, which is renowned for its exceptional balance of speed and accuracy on mobile devices. The entire pipeline is managed by the `YoloDetector.java` class.

1.  **Image Pre-processing**: Before analysis, every image is resized to a fixed **640x640 pixel** resolution and its pixel values are normalized. This is a critical step to match the model\'s expected input format.

2.  **AI Inference**: The prepared image is fed into the **TensorFlow Lite** interpreter. Using the device\'s CPU or GPU, the YOLOv11 model performs a forward pass to identify potential weapons.

3.  **High-Accuracy Post-processing**: Raw AI output is refined to produce clean, actionable results:
    -   **Confidence Filtering**: The system discards any detection with a confidence score below the user-defined threshold.
    -   **Non-Max Suppression (NMS)**: This crucial algorithm eliminates redundant and overlapping bounding boxes for the same object, ensuring each detected weapon is represented by a single, precise bounding box.

## Tech Stack & Key Libraries

-   **Language**: Java
-   **AI/ML**:
    -   `org.tensorflow:tensorflow-lite:2.14.0`
    -   `org.tensorflow:tensorflow-lite-support:0.4.4`
    -   `org.tensorflow:tensorflow-lite-gpu:2.14.0`
-   **Camera**:
    -   `androidx.camera:camera-camera2`
    -   `androidx.camera:camera-lifecycle`
    -   `androidx.camera:camera-view`
-   **UI & Components**:
    -   `com.google.android.material:material` for Material Design components.
    -   `androidx.constraintlayout:constraintlayout` for responsive layouts.
    -   `androidx.cardview:cardview` for structured data display.

## Setup & Installation

To build and run this project locally, follow these steps:

1.  Clone the repository:
    ```bash
    git clone https://your-repository-url/weapon-detection-ai.git
    ```
2.  Open the project in Android Studio.
3.  Let Gradle sync and download the required dependencies.
4.  Ensure you have an Android device or emulator running API level 24 or higher.
5.  Build and run the application.

## Future Work

-   **Model Enhancements**: Experiment with newer or quantized models (e.g., INT8) for improved performance on lower-end devices.
-   **Expanded Object Classes**: Train the model to recognize a wider array of threat objects.
-   **Cloud Backup**: An optional feature to allow users to back up their detection history.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
