# CSE304_DataMining_Group4
> Group 4: Kyuhwan Lee, Donggyu Lee, Chaeeun Lee, Seongsik Hwang

## ⚙️ Develop ENV
  - `Java 11`

## ❗️Required

1. GitHub name & password(SSH Key)
2. Recommend MacBook over M1 
3. Each assignment is in the **A?_G4** directory. So, if you want to know our assignment1, please go to the **A1_G4** directory.

>[!NOTE]
>Our Team have only MacBook over M1. If you don't have any MacBook over M1, please send emaill to **hanbitchan@unist.ac.kr** and we will lend our macbook

## 🛍️ Assignment 1
Move to the `A1_G4` directory that contains java codes and dataset for Assignment 1.

```
cd A1_G4
```

### 1️⃣ Apriori
```bash
javac A1_G4_t1.java
java A1_G4_t1 ./groceries.csv 0.05
```
#### Apriori-TID
Before Run, change the `exec("TID", inputFile);` in main function and then re-compile.
```bash
javac A1_G4_t1.java
java A1_G4_t1 ./groceries.csv 0.05
```

### 2️⃣ FP-growth
```bash
javac A1_G4_t2.java
java A1_G4_t2 ./groceries.csv 0.05
```

## 👫 Assignment 2
Move to the `A2_G4` directory that contains java codes and dataset for Assignment 2.

```
cd A2_G4
```

### 1️⃣ k-means++
```bash
javac A2_G4_t1.java
java A2_G4_t1 ./artd-31.csv 15
```
#### k-means(Random initialization)
TBD

### 2️⃣ DBSCAN
```bash
javac A2_G4_t2.java
java A2_G4_t2 ./artd-31.csv 15
```
