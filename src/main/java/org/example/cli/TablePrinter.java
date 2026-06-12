package org.example.cli;

import org.example.model.MonitorItem;
import org.example.service.SummaryResult;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TablePrinter {

    private static final String FMT = "| %-4d | %-16s | %-11s | %9.2f | %-10s | %-19s |";
    private static final String HEADER = "| ID   | NAME             | CATEGORY    |     VALUE | STATUS     | CREATED_AT          |";
    private static final String SEP    = "+------+------------------+-------------+-----------+------------+---------------------+";
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void printItems(List<MonitorItem> items) {
        if (items.isEmpty()) {
            System.out.println("결과가 없습니다.");
            return;
        }
        System.out.println(SEP);
        System.out.println(HEADER);
        System.out.println(SEP);
        for (MonitorItem item : items) {
            System.out.printf((FMT) + "%n",
                    item.getId(),
                    truncate(item.getName(), 16),
                    truncate(item.getCategory(), 11),
                    item.getValue(),
                    item.getStatus(),
                    item.getCreatedAt().format(DT));
        }
        System.out.println(SEP);
    }

    public static void printItem(MonitorItem item) {
        printItems(List.of(item));
    }

    public static void printSummary(SummaryResult r) {
        System.out.println("+------------+---------+------------+");
        System.out.println("| STATUS     |   COUNT |  AVG VALUE |");
        System.out.println("+------------+---------+------------+");
        System.out.printf("| %-10s | %7d | %10.2f |%n", "ACTIVE",   r.getActiveCount(),   avgOrZero(r.getActiveCount(),   r));
        System.out.printf("| %-10s | %7d | %10.2f |%n", "INACTIVE", r.getInactiveCount(), avgOrZero(r.getInactiveCount(), r));
        System.out.printf("| %-10s | %7d | %10.2f |%n", "WARNING",  r.getWarningCount(),  avgOrZero(r.getWarningCount(),  r));
        System.out.println("+------------+---------+------------+");
        System.out.printf("| %-10s | %7d | %10.2f |%n", "TOTAL", r.getTotal(), r.getAverageValue());
        System.out.println("+------------+---------+------------+");
    }

    private static double avgOrZero(long count, SummaryResult r) {
        return count == 0 ? 0.0 : r.getAverageValue();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
