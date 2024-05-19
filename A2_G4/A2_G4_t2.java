

import java.io.*;
import java.util.*;

public class A2_G4_t2 {
    static int MU;
    static double EPSILON;
    static int noise_count = 0;
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
                    EPSILON = estimateEpsilon(inputFilePath, MU);
                    //EPSILON = 0.15; // need to estimate
                } else if (flag_arg_1.equals("float")) {
                    MU = 10; // need to estimate
                    EPSILON = Double.parseDouble(args[1]);
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
            } else {
                System.out.println("Invalid input");
                return;
            }
        }
        // execute the program
        exec(inputFilePath);
    }

    private static double estimateEpsilon(String inputFilePath, int mu) {
        List<Point> points = loadData(inputFilePath);

        int n = points.size();
        List<Double> k_dist = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Point p1 = points.get(i);
            List<Double> distances = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                Point p2 = points.get(j);
                distances.add(p1.distanceTo(p2));
            }
            Collections.sort(distances);
            double sum = 0;
            for (int j = 0; j < mu; j++) {
                sum += distances.get(j);
            }
            k_dist.add(sum / mu);
        }
        Collections.sort(k_dist, Comparator.reverseOrder());

        return k_dist.get(findKneePoint(k_dist));
    }

    private static int findKneePoint(List<Double> sortedKDists) {
        int n = sortedKDists.size();

        double x1 = 0, y1 = sortedKDists.get(0);
        double x2 = n - 1, y2 = sortedKDists.get(n - 1);

        double maxDistance = -1;
        int kneePointIndex = 0;

        for (int i = 1; i < n - 1; i++) {
            double x0 = i, y0 = sortedKDists.get(i);

            double distance = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) /
                    Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

            if (distance > maxDistance) {
                maxDistance = distance;
                kneePointIndex = i;
            }
        }

        return kneePointIndex;
    }

    private static void exec(String inputFilePath) throws IOException {
        List<Point> points = loadData(inputFilePath);
        Date start = new Date();
        DBSCAN dbscan = new DBSCAN();
        dbscan.dbscan(points, EPSILON, MU);
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

        private int nextId(int currentId) {
            return currentId + 1;
        }

        public void dbscan(List<Point> setOfPoints, double eps, int minPts) {
            int clusterId = nextId(NOISE);
            for (Point point : setOfPoints) {
                if (point.getClusterId() == UNCLASSIFIED) {
                    if (expandCluster(setOfPoints, point, clusterId, eps, minPts)) {
                        clusterId = nextId(clusterId);
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
