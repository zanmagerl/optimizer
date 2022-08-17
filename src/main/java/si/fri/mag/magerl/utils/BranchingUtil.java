package si.fri.mag.magerl.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class BranchingUtil {

    /**
     * Working but highly inefficient method, since we do not need every possible variation, since
     * for n possibilities we have 2^n options.
     */
    public static <T> List<List<T>> getBranchingOptions(List<T> patternUsages) {
        log.info("{}", patternUsages.size());
        if (patternUsages.isEmpty()) {
            List<T> onlyElement = List.of();
            List<List<T>> returningList = new ArrayList<>();
            returningList.add(onlyElement);
            return returningList;
        }
        List<List<T>> resultList = new ArrayList<>();
        List<List<T>> recursedLists = getBranchingOptions(patternUsages.subList(1, patternUsages.size()));
        for (List<T> list : recursedLists) {;
            List<T> listWithElement = new ArrayList<>(list);
            listWithElement.add(patternUsages.get(0));

            resultList.add(listWithElement);
        }
        resultList.addAll(recursedLists);
        log.info("{} {}", patternUsages, patternUsages.size());
        return resultList;
    }

    public static <T> List<List<T>> sampleBranchingOptions(List<T> patternUsages, int numberOfOptions) {
        List<List<T>> results = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfOptions-1; i++) {
            List<T> iter = new ArrayList<>();
            for (int j = 0; j < patternUsages.size(); j++) {
                if (random.nextBoolean()) {
                    iter.add(patternUsages.get(j));
                }
            }
            results.add(iter);
        }
        results.add(new ArrayList<>(patternUsages));
        return results;
    }

}
