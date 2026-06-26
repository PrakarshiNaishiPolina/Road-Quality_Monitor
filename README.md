
# 📱 Smart Road Quality Monitoring System

## 📌 Overview

The **Smart Road Quality Monitoring System** is an Android application developed using **Kotlin** and **Android Studio** to monitor road conditions in real time. The application uses smartphone **accelerometer** and **GPS sensors** to detect road anomalies such as **potholes**, **speed bumps**, and **rough roads**.

The detected anomalies are stored along with their geographical coordinates for further analysis through an admin dashboard.

---

## 🚀 Features

- 🔐 Role-based Authentication (**Admin/User**)
- 👤 User Signup and Login System
- 📍 Real-time GPS Location Tracking
- 📳 Accelerometer-based Road Anomaly Detection
- 🚧 Detection of:
  - Potholes
  - Speed Bumps
  - Rough Roads
- 🚗 Vehicle-specific Threshold Configuration
- 📊 Admin Dashboard for Viewing Detection Logs
- 💾 CSV-based Data Storage
- 🔄 Persistent Login using SharedPreferences
- 🛡️ Noise Reduction using Window-based Filtering
- 🗑️ Delete Detection Logs and User Accounts (Admin)

---

## 🛠️ Technologies Used

- **Kotlin**
- **Android Studio**
- **XML**
- **Android Sensor API**
- **Google Location Services (GPS)**
- **SharedPreferences**
- **CSV File Handling**

---

## 🏗️ System Architecture

```text
User Login
     ↓
Select Vehicle Type
     ↓
Configure Threshold Values
     ↓
Start Monitoring
     ↓
Accelerometer + GPS Data Collection
     ↓
Road Anomaly Detection
     ↓
Store Data in CSV File
     ↓
Admin Dashboard for Analysis
```

---

## 📂 Modules

### 1. Authentication Module

- User/Admin Login and Signup
- Forgot Password Functionality
- Persistent Session Management

### 2. Configuration Module

- Vehicle Selection
- Threshold Configuration:
  - Pothole Threshold
  - Bump Threshold
  - Rough Road Threshold
  - Minimum Speed Threshold

### 3. Detection Module

- Continuously monitors accelerometer readings.
- Removes gravity effect from sensor data.
- Detects road anomalies based on threshold values.

### 4. Admin Module

- View all detected anomalies.
- View configured threshold values.
- Refresh and delete detection logs.
- Delete user accounts.

---

## ⚙️ Working Principle

1. The user logs into the application.
2. Vehicle type and threshold values are configured.
3. The application continuously collects accelerometer and GPS data.
4. Gravity is removed from the sensor readings to obtain actual road vibrations.
5. A moving window and threshold-based algorithm are applied to reduce noise.
6. If the vibration exceeds the predefined threshold values, the road anomaly is classified as:
   - Pothole
   - Speed Bump
   - Rough Road
7. The detected anomaly along with location coordinates is stored in a CSV file.

---

## 📊 Data Storage

Detected anomalies are stored in **CSV format** inside Android external storage.

The stored data includes:

- Vehicle Type
- Anomaly Type
- Severity Level
- Intensity
- Speed
- Threshold Values
- Latitude
- Longitude
- Timestamp

---

## 📱 Screens

- Login Screen
- Home Screen
- Configuration Screen
- Real-time Monitoring Screen
- Admin Dashboard

---

## 🔮 Future Enhancements

- Cloud Database Integration
- Google Maps Visualization
- Machine Learning-based Detection
- Automatic Road Condition Reporting
- Real-time Data Synchronization
- Analytics Dashboard

