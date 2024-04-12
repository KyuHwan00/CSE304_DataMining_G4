# DataMining-Group4
Group 4 : 이규환, 이동규, 이채은, 황성식
___
## ⚙️ Develop ENV
  - `Java 11`

## ❗️Required

1. GitHub name & password(SSH Key)
2. Recommend MacBook over M1 
   (Our Team have only MacBook over M1. If you don't have any MacBook over M1, please send emaill to **hanbitchan@unist.ac.kr** and we will lend our macbook)
3. Each assignment is in the **A?_G4** directory. So, if you want to know our assignment1, please go to the **A1_G4** directory.
   
## 1️⃣ Assignment1
Move to the `A1_G4` directory that contains java codes and dataset for Assignment 1.
### Apriori
```bash
cd A1_G4
javac A1_G4_t1.java
java A1_G4_t1 ./groceries.csv 0.05
```
### Apriori-TID
Before Run, change the `exec("TID", inputFile);` in main function and then re-compile.
```bash
javac A1_G4_t1.java
java A1_G4_t1 ./groceries.csv 0.05
```

### FP-growth
```bash
javac A1_G4_t2.java
java A1_G4_t2 ./groceries.csv 0.05
```
