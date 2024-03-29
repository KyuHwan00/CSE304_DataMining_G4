import java.io.*;
import java.util.*;

public class A1_G4_t1 {

    static Map<Integer, String[]> dataSet = new HashMap<>();
    static Map<Integer, Set<Set<String>>> result = new HashMap<>();
    static Float minSupport;
    public static void main(String[] args) {
        // init the input file, minimum support from command line
        String inputFile = args[0];
        minSupport = Float.parseFloat(args[1]);

        // for itemset of size 1 (later using with L1)
        Set<Set<String>> itemSets_1 = new HashSet<>();
        int tid = 0;

        // read the input file and sorted the items in each transaction using lexographical order
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            while ((line = br.readLine()) != null) {
                String[] lineStrings = line.split(",");
                Set<String> itemSet = new HashSet<>(Arrays.asList(lineStrings));
                for (String item : itemSet) {
                    Set<String> comb = new HashSet<>();
                    comb.add(item);
                    itemSets_1.add(comb);
                }
                Arrays.sort(lineStrings);
                dataSet.put(tid, lineStrings);
                tid++;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // call the apriori algorithm
        apriori(itemSets_1);
    }

    // apriori algorithm
    public static void apriori(Set<Set<String>> itemSet_1) {

        Set<Set<String>> l1 = getSupportedItemset(itemSet_1);
        Set<Set<String>> supportedItemset = l1;

        for(int k = 2; !supportedItemset.isEmpty(); k++) {
            result.put(k-1, supportedItemset);
            // candidate generation
            Set<Set<String>> candidateSet = getUnion(supportedItemset, k);
            Set<Set<String>> candidateSetPruned = pruning(candidateSet, supportedItemset, k-1);
            // subset
            supportedItemset = getAboveMinSup(candidateSetPruned, dataSet.values());
        }
        
        printResultsInAscendingOrder(result);
    }

    public static Set<Set<String>> getSupportedItemset(Set<Set<String>> itemset) {
        Set<Set<String>> map = new HashSet<>();
        for (Set<String> comb : itemset) {
            Float support = calcSupport(comb);
            if (support >= minSupport) {
                map.add(comb);
            }
        }

        return map;
    }
    // calculate the support of an itemset
    public static Float calcSupport(Set<String> itemset) {
        Integer freq = 0;
        for (String[] item : dataSet.values()) {
            if (Arrays.asList(item).containsAll(itemset)) {
                freq++;
            }
        }
        return freq / ((float)dataSet.size());
    }
    
    public static Set<Set<String>> getAboveMinSup(Set<Set<String>> C_k, Collection<String[]> transactions) {
        Set<Set<String>> supportedSet = new HashSet<>();
        Map<Set<String>, Integer> C_t = new HashMap<>();
        for (Set<String> item : C_k) {
            for (String[] transaction : transactions) {
                Set<String> transactionSet = new HashSet<>(Arrays.asList(transaction));
                if (transactionSet.containsAll(item)) {
                    C_t.put(item, C_t.getOrDefault(item, 0) + 1);
                }
            }
        }
        for (Map.Entry<Set<String>, Integer> entry : C_t.entrySet()) {
            float support = entry.getValue() / (float) transactions.size();
            if (support >= minSupport) {
                supportedSet.add(entry.getKey());
            }
        }

        return supportedSet;
    }

    public static Set<Set<String>> getUnion(Set<Set<String>> itemSet, int length) {
        Set<Set<String>> resultSet = new HashSet<>();
        for (Set<String> i : itemSet) {
            for (Set<String> j : itemSet) {
                Set<String> union = new HashSet<>(i);
                union.addAll(j);
                if (union.size() == length) {
                    resultSet.add(union);
                }
            }
        }
        return resultSet;
    }

    public static Set<Set<String>> pruning(Set<Set<String>> candidateSet, Set<Set<String>> prevFreqSet, int length) {
        Set<Set<String>> prunedSet = new HashSet<>(candidateSet);
        for (Set<String> item : candidateSet) {
            Set<Set<String>> subsets = getSubsets(item, length);
            for (Set<String> subset : subsets) {
                if (!prevFreqSet.contains(subset)) {
                    prunedSet.remove(item);
                    break;
                }
            }
        }
        return prunedSet;
    }

    private static Set<Set<String>> getSubsets(Set<String> set, int length) {
        Set<Set<String>> result = new HashSet<>();
        getSubsetsHelper(set, length, new HashSet<>(), result);
        return result;
    }

    private static void getSubsetsHelper(Set<String> set, int length, Set<String> current, Set<Set<String>> result) {
        if (current.size() == length) {
            result.add(new HashSet<>(current));
            return;
        }
        Set<String> remaining = new HashSet<>(set);
        for (String s : set) {
            current.add(s);
            remaining.remove(s);
            getSubsetsHelper(remaining, length, current, result);
            current.remove(s);
        }
    }

    public static void printResultsInAscendingOrder(Map<Integer, Set<Set<String>>> result) {
        List<Map.Entry<String, Float>> itemList = new ArrayList<>();

        for (Map.Entry<Integer, Set<Set<String>>> entry : result.entrySet()) {
            for (Set<String> item : entry.getValue()) {
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


