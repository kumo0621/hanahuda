package com.github.kumo0621.hanahuda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomCount {
    private static List<Integer> numbers = new ArrayList<>();
    private static int currentIndex = 0;
    private static boolean isCompleted = false;

    public static void initialize() {
        for (int i = 0; i < 45; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
    }

    public static int random() {
        if (currentIndex >= numbers.size()) {
            if (!isCompleted) {
                // すべての数字が一周した時の処理を実行
                System.out.println("すべての数字が一周しました！");
                isCompleted = true;
            }
            initialize();
            currentIndex = 0;
        }

        int num = numbers.get(currentIndex);
        currentIndex++;
        System.out.println(num);
        return num;
    }
}


