package si.fri.mag.magerl.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BranchingUtil {

    public static <T> List<List<T>> getBranchingOptions(List<T> patternUsages) {

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

        return resultList;
    }

}
