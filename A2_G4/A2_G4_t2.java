

import java.io.*;
import java.util.*;

public class A2_G4_t2 {
    static int MU;
    static double EPSILON;
    static int noise_count = 0;
    static List<Double> epsilons = new ArrayList<>();
    static int splitNum = 0;
    static List<List<Map.Entry<Double, Point>>> splitList = new ArrayList<>();
    static List<List<Point>> splitPoints = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        String inputFilePath = args[0];
        // set mu and epsilon according to args
        if (args.length < 3) {
            if (args.length == 1) {
                System.out.println("Invalid input");
                return;
            } else {
                String flag_arg_1 = checkNumberFormat(args[1]);
                if (flag_arg_1.equals("int")) {
                    MU = Integer.parseInt(args[1]);
                    epsilons = estimateEpsilon(inputFilePath, MU, splitNum);
                    EPSILON = epsilons.get(0);
                    System.out.println("Estimated eps : " + EPSILON);
                    //EPSILON = 0.15; // need to estimate
                } else if (flag_arg_1.equals("float")) {
                    MU = 4; // need to estimate
                    EPSILON = Double.parseDouble(args[1]);
                    System.out.println("Estimated MinPts : " + MU);
                } else {
                    System.out.println("Invalid input");
                    return;
                }
            }

        } else {
            String flag_arg_1 = checkNumberFormat(args[1]);
            String flag_arg_2 = checkNumberFormat(args[2]);
            if (flag_arg_1.equals("int") && flag_arg_2.equals("float")) {
                MU = Integer.parseInt(args[1]);
                EPSILON = Double.parseDouble(args[2]);
            } else if (flag_arg_1.equals("float") && flag_arg_2.equals("int")) {
                MU = Integer.parseInt(args[2]);
                EPSILON = Double.parseDouble(args[1]);
            } else if (flag_arg_1.equals("int") && args[2].startsWith("s=")) {
                splitNum = Integer.parseInt(args[2].substring(2));
                MU = Integer.parseInt(args[1]);
                epsilons = estimateEpsilon(inputFilePath, MU, splitNum);
                EPSILON = epsilons.get(0);
                System.out.print("Estimated eps : ");
                for (Double d : epsilons) {
                    System.out.print(d + " ");
                }
                System.out.println();
            } else {
                System.out.println("Invalid input");
                return;
            }
        }
        // execute the program
        exec(inputFilePath);
    }

    private static List<Double> estimateEpsilon(String inputFilePath, int mu, int splitNum) throws IOException {
        List<Point> points = loadData(inputFilePath);

        int n = points.size();
        List<Map.Entry<Double, Point>> k_dist = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Point p1 = points.get(i);
            List<Double> distances = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    Point p2 = points.get(j);
                    distances.add(p1.distanceTo(p2));
                }
            }
            Collections.sort(distances);
            k_dist.add(new AbstractMap.SimpleEntry<>(distances.get(mu - 1), p1));
        }
        Collections.sort(k_dist, new Comparator<Map.Entry<Double, Point>>() {
            @Override
            public int compare(Map.Entry<Double, Point> e1, Map.Entry<Double, Point> e2) {
                return Double.compare(e2.getKey(), e1.getKey());
            }
        });
//        for (int i=0; i<k_dist.size(); i++) {
//            System.out.println(k_dist.get(i));
//        }
        List<Double> result = new ArrayList<>();
        if (splitNum == 0) {
            result.add(k_dist.get(findKneePoint(k_dist)).getKey());
            return result;
        }
        splitList = splitKDist(k_dist, splitNum);

        for (List<Map.Entry<Double, Point>> k_dist_split : splitList) {
            List<Point> pointList = new ArrayList<>();
            for (Map.Entry<Double, Point> m : k_dist_split) {
                pointList.add(m.getValue());
            }
            splitPoints.add(pointList);
            result.add(k_dist_split.get(findKneePoint(k_dist_split)).getKey());
        }

        return result;
    }

    public static List<List<Map.Entry<Double, Point>>> splitKDist(List<Map.Entry<Double, Point>> k_dist, int splitNum) {
        int n = k_dist.size();

        List<Map.Entry<Double, Integer>> distChange = new ArrayList<>();

        for (int i = 1; i < n - 1; i++) {
            double distGap = (k_dist.get(i-1).getKey() - k_dist.get(i).getKey())/(k_dist.get(i-1).getKey() + k_dist.get(i).getKey());
            //System.out.println("slope : " + (slopeLeft) + ", index : " + i);
            distChange.add(new AbstractMap.SimpleEntry<>(distGap, i));
        }

        Collections.sort(distChange, new Comparator<Map.Entry<Double, Integer>>() {
            @Override
            public int compare(Map.Entry<Double, Integer> e1, Map.Entry<Double, Integer> e2) {
                return Double.compare(e2.getKey(), e1.getKey());
            }
        });

        List<Integer> splitIndex = new ArrayList<>();
        for (int i=0; i<splitNum-1; i++) {
            splitIndex.add(distChange.get(i).getValue());
        }
        Collections.sort(splitIndex);

        return splitListByIndices(k_dist, splitIndex);
    }

    public static List<List<Map.Entry<Double, Point>>> splitListByIndices(List<Map.Entry<Double, Point>> k_dist, List<Integer> indices) {
        List<List<Map.Entry<Double, Point>>> result = new ArrayList<>();

        int previousIndex = 0;
        for (int index : indices) {
            if (index > previousIndex && index <= k_dist.size()) {
                result.add(new ArrayList<>(k_dist.subList(previousIndex, index)));
                previousIndex = index;
            }
        }

        if (previousIndex < k_dist.size()) {
            result.add(new ArrayList<>(k_dist.subList(previousIndex, k_dist.size())));
        }

        return result;
    }

    private static int findKneePoint(List<Map.Entry<Double, Point>> sortedKDists) {
        int n = sortedKDists.size();

        double x1 = 0, y1 = sortedKDists.get(0).getKey();
        double x2 = n - 1, y2 = sortedKDists.get(n - 1).getKey();

        double maxDistance = -1;
        int kneePointIndex = 0;

        for (int i = 1; i < n - 1; i++) {
            double x0 = i, y0 = sortedKDists.get(i).getKey();

            double distance = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) /
                    Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

            if (distance > maxDistance) {
                maxDistance = distance;
                kneePointIndex = i;
            }
        }

        return kneePointIndex;
    }

    public static void writeCSV(String filePath, List<List<String>> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (List<String> row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
            System.out.println("CSV 파일이 성공적으로 생성되었습니다: " + filePath);
        } catch (IOException e) {
            System.err.println("CSV 파일을 생성하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private static void exec(String inputFilePath) throws IOException {
        Date start = new Date();
        DBSCAN dbscan = new DBSCAN();
        List<Point> points = new ArrayList<>();
        if (splitNum == 0) {
            points = loadData(inputFilePath);
            dbscan.dbscan(points, EPSILON, MU);
        } else {
            for (int i=0; i<splitNum; i++) {
                List<Point> split_points = splitPoints.get(i);
                dbscan.dbscan(split_points, epsilons.get(i), MU);
                points.addAll(split_points);
            }
        }
        // to do: implement DBSCAN
        
        Date end = new Date();
        long time = end.getTime() - start.getTime();

        Map<Integer, List<Point>> clusters = new HashMap<>();
        int noiseCount = 0;

        for (Point point : points) {
            int clusterId = point.getClusterId();
            if (clusterId == DBSCAN.NOISE) {
                noiseCount++;
            } else {
                clusters.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(point);
            }
        }

        System.out.println("Number of clusters : " + clusters.size());
        System.out.println("Number of noise : " + noiseCount);

        for (Map.Entry<Integer, List<Point>> entry : clusters.entrySet()) {
            System.out.print("Cluster #" + entry.getKey() + " => ");
            for (Point point : entry.getValue()) {
                System.out.print(point.getId() + " ");
            }
            System.out.println();
        }
        // System.out.println("Execution time is " + time + " milliseconds");

    }

    public static List<Point> loadData(String filePath) {
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String id = values[0];
                double x = Double.parseDouble(values[1]);
                double y = Double.parseDouble(values[2]);
                int value = Integer.parseInt(values[3]);
                points.add(new Point(id, x, y));
                // points.add(new Point(id, x, y, value));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    private static String checkNumberFormat(String input) {
        if (isInteger(input)) {
            return "int";
        } else if (isFloat(input)) {
            return "float";
        } else {
            return "neither";
        }
    }

    private static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFloat(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private static class Point {
        private String id;
        private double x, y;
        private int clusterId;

        public Point(String name, double x, double y) {
            this.x = x;
            this.y = y;
            this.clusterId = DBSCAN.UNCLASSIFIED;
            this.id = name;
        }

        public double distanceTo(Point other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }

        public int getClusterId() {
            return clusterId;
        }

        public void setClusterId(int clusterId) {
            this.clusterId = clusterId;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public String getId() {
            return id;
        }
    }

    private static class DBSCAN {
        public static final int UNCLASSIFIED = -1;
        public static final int NOISE = 0;

        public static int CLUSTER_ID = 1;

        private static void increase_Cluster_ID() {
            CLUSTER_ID++;
        }

        public void dbscan(List<Point> setOfPoints, double eps, int minPts) {
            for (Point point : setOfPoints) {
                if (point.getClusterId() == UNCLASSIFIED) {
                    if (expandCluster(setOfPoints, point, CLUSTER_ID, eps, minPts)) {
                        increase_Cluster_ID();
                    }
                }
            }
        }

        private boolean expandCluster(List<Point> setOfPoints, Point point, int clusterId, double eps, int minPts) {
            List<Point> seeds = regionQuery(setOfPoints, point, eps);
            if (seeds.size() < minPts) {
                point.setClusterId(NOISE);
                return false;
            } else {
                for (Point seed : seeds) {
                    seed.setClusterId(clusterId);
                }
                seeds.remove(point);

                while (!seeds.isEmpty()) {
                    Point currentP = seeds.get(0);
                    List<Point> result = regionQuery(setOfPoints, currentP, eps);
                    if (result.size() >= minPts) {
                        for (Point resultP : result) {
                            if (resultP.getClusterId() == UNCLASSIFIED || resultP.getClusterId() == NOISE) {
                                if (resultP.getClusterId() == UNCLASSIFIED) {
                                    seeds.add(resultP);
                                }
                                resultP.setClusterId(clusterId);
                            }
                        }
                    }
                    seeds.remove(currentP);
                }
                return true;
            }
        }

        private List<Point> regionQuery(List<Point> setOfPoints, Point point, double eps) {
            List<Point> neighbors = new ArrayList<>();
            for (Point p : setOfPoints) {
                if (point.distanceTo(p) <= eps) {
                    neighbors.add(p);
                }
            }
            return neighbors;
        }
    }
}
