## 2️⃣ Assignment 2

Before do these, please check current working directory.

If you're not in `A2_G4` directory,
Move to the `A2_G4` directory that contains java codes and dataset for Assignment 2.

```
cd A2_G4
```

### k-means++
```bash
javac A2_G4_t1.java
java A2_G4_t1 ./artd-31.csv 15
```
### k-means(Random initialization)
TBD

### DBSCAN
```bash
javac A2_G4_t2.java
java A2_G4_t2 ./artd-31.csv 4
```
You can use splitKDist by entering mu along with s=splitNum.
```bash
java A2_G4_t2 ./artd-31.csv 4 s=2
```
