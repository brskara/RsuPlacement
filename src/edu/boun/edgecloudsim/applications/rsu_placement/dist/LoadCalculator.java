package edu.boun.edgecloudsim.applications.rsu_placement.dist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadCalculator {

    public static void main(String[] args) throws IOException {
        int limit = 10;
        findByFailedTasks(limit);
        findMinByTotalTasks(limit);
        
    }

    private static void findMinByTotalTasks(int limit) throws IOException {
        Map<Integer, Integer> tasksMap = collectTasksByIdAndSum(TaskLoad::getNumberOfTasks)
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("bottom X :");
        System.out.println(tasksMap);

    }

    private static void findByTotalTasks(int limit)  throws IOException {

        Map<Integer, Integer> tasksMap = collectTasksByIdAndSum(TaskLoad::getNumberOfTasks);
        System.out.println(tasksMap);
        int tasksSum = tasksMap.values().stream().mapToInt(i -> i).sum();
        System.out.println("all tasks sum: " + tasksSum);

        Map<Integer, Double> rateMap = tasksMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((double)e.getValue() / tasksSum)*100d,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("all rate results:");
        System.out.println(rateMap);

        Map<Integer, Integer> tasksSumForFirstXMap = tasksMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("tasks sum results for first x:");
        System.out.println(tasksSumForFirstXMap);

        int tasksSumForFirstX = tasksSumForFirstXMap.values().stream().mapToInt(i -> i).sum();
        System.out.println("tasks sum for first X: " + tasksSumForFirstX);

        Map<Integer, Double> tasksRateForFirst20Map = tasksSumForFirstXMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((double)e.getValue() / tasksSumForFirstX)*100d,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("task rate for first X results:");
        System.out.println(tasksRateForFirst20Map);

        Map<Integer, Double> weightedForXResult = tasksRateForFirst20Map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue() * (double)limit)/100d,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("weighted for X results:");
        System.out.println(weightedForXResult);
    }

    private static void findByFailedTasks(int limit) throws IOException {
        Map<Integer, Integer> failedTasksMap = collectTasksByIdAndSum(TaskLoad::getNumberOfFailedTasks);
        System.out.println(failedTasksMap);
        int failedTasksSum = failedTasksMap.values().stream().mapToInt(i -> i).sum();
        System.out.println("failed tasks sum: " + failedTasksSum);

        Map<Integer, Integer> allTasksMap = collectTasksByIdAndSum(TaskLoad::getNumberOfTasks);
        int allTasksSum = allTasksMap.values().stream().mapToInt(i -> i).sum();
        System.out.println("all tasks sum: " + allTasksSum);

        Map<Integer, Double> failedRateMap = failedTasksMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((double)e.getValue() / failedTasksSum)*100d,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("failed rate results:");
        System.out.println(failedRateMap);

        Map<Integer, Integer> failedTasksSumForFirst20Map = failedTasksMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("failed tasks sum results for first 20:");
        System.out.println(failedTasksSumForFirst20Map);

        int failedTasksSumForFirst20 = failedTasksSumForFirst20Map.values().stream().mapToInt(i -> i).sum();
        System.out.println("failed tasks sum for first 20: " + failedTasksSumForFirst20);

        Map<Integer, Double> failedTasksRateForFirst20Map = failedTasksSumForFirst20Map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((double)e.getValue() / failedTasksSumForFirst20)*100d,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("failed rate for first 20 results:");
        System.out.println(failedTasksRateForFirst20Map);


        Map<Integer, Double> weightedFor20Result = failedTasksRateForFirst20Map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue() * (double)limit)/100d,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println("weighted for " + limit + " results:");
        System.out.println(weightedFor20Result);
    }

    private static Map<Integer, Integer> collectTasksByIdAndSum(ToIntFunction<TaskLoad> function) throws IOException {
        return Files.list(Paths.get("scripts/rsu_placement/task_dist"))
                .filter(Files::isRegularFile)
                .flatMap(s -> {
                    try {
                        return Files.lines(s);
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                })
                .map(TaskLoad::new)
                .collect(Collectors.groupingBy(TaskLoad::getRsuId, Collectors.summingInt(function)));
    }


    static class TaskLoad {
        private int rsuId;
        private int numberOfTasks;
        private double rate;
        private int numberOfFailedTasks;
        private double failRate;

        public TaskLoad(String s) {
            String[] vals = s.split(";");
            rsuId = Integer.parseInt(vals[0]);
            numberOfTasks = Integer.parseInt(vals[1]);
            rate = Double.parseDouble(vals[2]);
            numberOfFailedTasks = Integer.parseInt(vals[3]);
            failRate = Double.parseDouble(vals[4]);
        }

        public int getRsuId() {
            return rsuId;
        }

        public int getNumberOfTasks() {
            return numberOfTasks;
        }

        public double getRate() {
            return rate;
        }

        public int getNumberOfFailedTasks() {
            return numberOfFailedTasks;
        }

        public double getFailRate() {
            return failRate;
        }
    }
}
