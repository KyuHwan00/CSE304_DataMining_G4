import java.io.*;
import java.util.*;

public class A1_G4_t1 {

    static Map<Integer, LinkedHashSet<String>> dataSet = new HashMap<>();
    static Map<Integer, Set<LinkedHashSet<String>>> result = new HashMap<>();
    static Float minSupport;
    public static void main(String[] args) {
        // init the input file, minimum support from command line
        String inputFile = args[0];
        minSupport = Float.parseFloat(args[1]);

        // for itemset of size 1 (later using with L1)
        Set<LinkedHashSet<String>> itemSets_1 = new HashSet<>();
        int tid = 0;

        // read the input file and sorted the items in each transaction using lexographical order
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            while ((line = br.readLine()) != null) {
                String[] lineStrings = line.split(",");
                Arrays.sort(lineStrings);
                LinkedHashSet<String> itemSet = new LinkedHashSet<>(Arrays.asList(lineStrings));
                for (String item : itemSet) {
                    LinkedHashSet<String> comb = new LinkedHashSet<>();
                    comb.add(item);
                    itemSets_1.add(comb);
                }
                dataSet.put(tid, itemSet);
                tid++;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // call the apriori algorithm
        apriori(itemSets_1);
    }

    // apriori algorithm
    public static void apriori(Set<LinkedHashSet<String>> itemSet_1) {

        Set<LinkedHashSet<String>> l1 = getSupportedItemset(itemSet_1);
        Set<LinkedHashSet<String>> supportedItemset = l1;
        for(int k = 2; !supportedItemset.isEmpty(); k++) {
            result.put(k-1, supportedItemset);
            // candidate generation
            Set<LinkedHashSet<String>> candidateSet = getUnion(supportedItemset, k);
            Set<LinkedHashSet<String>> candidateSetPruned = pruning(candidateSet, supportedItemset, k-1);
            // subset
            supportedItemset = getAboveMinSup(candidateSetPruned, dataSet.values());
        }
        
        printResultsInAscendingOrder(result);
    }

    public static Set<LinkedHashSet<String>> getSupportedItemset(Set<LinkedHashSet<String>> itemset) {
        Set<LinkedHashSet<String>> map = new HashSet<>();
        for (LinkedHashSet<String> comb : itemset) {
            Float support = calcSupport(comb);
            if (support >= minSupport) {
                map.add(comb);
            }
        }
        
        return map;
    }
    // calculate the support of an itemset
    public static Float calcSupport(LinkedHashSet<String> itemset) {
        Integer freq = 0;
        for (LinkedHashSet<String> item : dataSet.values()) {
            if (item.containsAll(itemset)) {
                freq++;
            }
        }
        return freq / ((float)dataSet.size());
    }
    
    public static Set<LinkedHashSet<String>> getAboveMinSup(Set<LinkedHashSet<String>> C_k, Collection<LinkedHashSet<String>> transactions) {
        Set<LinkedHashSet<String>> supportedSet = new HashSet<>();
        Map<LinkedHashSet<String>, Integer> C_t = new HashMap<>();
        for (LinkedHashSet<String> item : C_k) {
            for (LinkedHashSet<String> transaction : transactions) {
                if (transaction.containsAll(item)) {
                    C_t.put(item, C_t.getOrDefault(item, 0) + 1);
                }
            }
        }
        for (Map.Entry<LinkedHashSet<String>, Integer> entry : C_t.entrySet()) {
            float support = entry.getValue() / (float) transactions.size();
            if (support >= minSupport) {
                supportedSet.add(entry.getKey());
            }
        }

        return supportedSet;
    }

    public static Set<LinkedHashSet<String>> getUnion(Set<LinkedHashSet<String>> itemSet, int length) {
        Set<LinkedHashSet<String>> resultSet = new HashSet<>();
        for (LinkedHashSet<String> i : itemSet) {
            for (LinkedHashSet<String> j : itemSet) {
                if (i.equals(j)) {
                    continue;
                }
                LinkedHashSet<String> union = new LinkedHashSet<>(i);
                Iterator<String> iIterator = i.iterator();
                Iterator<String> jIterator = j.iterator();
                for (int k = 0; k < length - 2; k++) {
                    String iNext = iIterator.next();
                    String jNext = jIterator.next();
                    if (!iNext.equals(jNext)) {
                        break;
                    }
                    union.add(iNext);
                }
                String iNext = iIterator.next();
                String jNext = jIterator.next();
                if (iNext.compareTo(jNext) < 0) {
                    union.add(iNext);
                    union.add(jNext);
                } else {
                    union.add(jNext);
                    union.add(iNext);
                }

                if (union.size() == length) {
                    resultSet.add(union);
                }
            }
        }
        return resultSet;
    }

    public static Set<LinkedHashSet<String>> pruning(Set<LinkedHashSet<String>> candidateSet, Set<LinkedHashSet<String>> prevFreqSet, int length) {
        Set<LinkedHashSet<String>> prunedSet = new HashSet<>(candidateSet);
        for (LinkedHashSet<String> item : candidateSet) {
            Set<LinkedHashSet<String>> subsets = getSubsets(item, length);
            for (Set<String> subset : subsets) {
                if (!prevFreqSet.contains(subset)) {
                    prunedSet.remove(item);
                    break;
                }
            }
        }
        return prunedSet;
    }

    private static Set<LinkedHashSet<String>> getSubsets(LinkedHashSet<String> set, int length) {
        Set<LinkedHashSet<String>> result = new HashSet<>();
        getSubsetsHelper(set, length, new LinkedHashSet<>(), result);
        return result;
    }

    private static void getSubsetsHelper(LinkedHashSet<String> set, int length, LinkedHashSet<String> current, Set<LinkedHashSet<String>> result) {
        if (current.size() == length) {
            result.add(new LinkedHashSet<>(current));
            return;
        }
        LinkedHashSet<String> remaining = new LinkedHashSet<>(set);
        for (String s : set) {
            current.add(s);
            remaining.remove(s);
            getSubsetsHelper(remaining, length, current, result);
            current.remove(s);
        }
    }

    public static void printResultsInAscendingOrder(Map<Integer, Set<LinkedHashSet<String>>> result) {
        List<Map.Entry<String, Float>> itemList = new ArrayList<>();

        for (Map.Entry<Integer, Set<LinkedHashSet<String>>> entry : result.entrySet()) {
            for (LinkedHashSet<String> item : entry.getValue()) {
                String itemString = String.join(", ", item);
                itemList.add(new AbstractMap.SimpleEntry<>(itemString, calcSupport(item)));
            }
        }

        // Sorting based on support value in ascending order
        itemList.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, Float> item : itemList) {
            System.out.println(item.getKey() + " : " + String.format("%.7f", item.getValue()).replaceFirst("0*$", ""));
        }
    }
}


