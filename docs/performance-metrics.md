# Habit Tracker Performance Metrics

This document outlines the speed and memory performance of the Habit Tracker application with varying numbers of habits in the database.

---

## Performance Results

### 20 Habits in Database

#### Startup Performance
- **Initialization Time:** 1468 ms
- **Application Start Time:** 1565 ms
- **Total Startup Time:** 3062 ms

#### View Switching Times
- Add Habit View: 36 ms
- Habit List View: 24 ms
- Progress View: 79 ms
- Report View: 69 ms
- Settings View: 12 ms
- Help View: 15 ms

#### Memory and CPU Usage
- **Heap Memory Usage:** ~50 MB
- **Threads Used:** 23 (including daemon threads)
- **CPU Usage:** 0.4% during runtime

---

### 50 Habits in Database

#### Startup Performance
- **Initialization Time:** 1397 ms
- **Application Start Time:** 2070 ms
- **Total Startup Time:** 3500 ms

#### View Switching Times
- Add Habit View: 44 ms
- Habit List View: 27 ms
- Progress View: 75 ms
- Report View: 75 ms
- Settings View: 12 ms
- Help View: 15 ms

#### Memory and CPU Usage
- **Heap Memory Usage:** ~49 MB
- **Threads Used:** 23 (including daemon threads)
- **CPU Usage:** 1.4% during runtime

---

### 100 Habits in Database

#### Startup Performance
- **Initialization Time:** 1393 ms
- **Application Start Time:** 2609 ms
- **Total Startup Time:** 4031 ms

#### View Switching Times
- Add Habit View: 42 ms
- Habit List View: 39 ms
- Progress View: 73 ms
- Report View: 62 ms
- Settings View: 14 ms
- Help View: 14 ms

#### Memory and CPU Usage
- **Heap Memory Usage:** ~51 MB
- **Threads Used:** 23 (including daemon threads)
- **CPU Usage:** 1.2% during runtime

---

## Scalability Concerns

As the number of habits increases, the application's startup and view-switching performance shows significant degradation:

### 250 Habits in Database
- **Initialization Time:** 1981 ms
- **Application Start Time:** 6796 ms
- **Total Startup Time:** 8822 ms

### 1000 Habits in Database
- **Initialization Time:** 1942 ms
- **Application Start Time:** 51758 ms
- **Total Startup Time:** 53738 ms

### Observations
- **Startup Time Impact:** While the `init()` method maintains consistent performance, the `start()` method scales poorly with larger datasets. At 1000 habits, the application takes over 50 seconds to load.
- **Memory Usage:** Memory usage remains stable across datasets, suggesting that the performance bottleneck lies in computational operations during application startup.
- **View Switching:** Larger datasets may potentially affect view-switching performance, though this was not observed in smaller datasets (20, 50, 100 habits).

---

This documentation provides a baseline for performance tuning and highlights areas requiring scalability improvements for larger datasets.
