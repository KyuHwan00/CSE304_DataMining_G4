## 1️⃣ Assignment 1
Before do these, please check current working directory.

If you're not in `A2_G4` directory,
Move to the `A1_G4` directory that contains java codes and dataset for Assignment 1.

```
cd A1_G4
```

### Apriori
```bash
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
