package org.example.cli;

import org.example.model.MonitorItem;
import org.example.model.Status;
import org.example.service.MonitorService;
import org.example.service.SummaryResult;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CommandParser {

    private final MonitorService service;

    public CommandParser(MonitorService service) {
        this.service = service;
    }

    public void execute(String line) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isBlank()) return;

        String cmd = tokens[0].toLowerCase();
        try {
            switch (cmd) {
                case "list"    -> executeList(tokens);
                case "get"     -> executeGet(tokens);
                case "add"     -> executeAdd(tokens);
                case "update"  -> executeUpdate(tokens);
                case "delete"  -> executeDelete(tokens);
                case "summary" -> executeSummary();
                case "seed"    -> executeSeed();
                case "help"    -> executeHelp();
                default        -> System.out.println("알 수 없는 명령어: " + tokens[0]);
            }
        } catch (NumberFormatException e) {
            System.out.println("숫자 형식 오류: " + e.getMessage());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            System.out.println(e.getMessage());
        }
    }

    private void executeList(String[] tokens) {
        List<MonitorItem> items;
        if (tokens.length >= 3 && tokens[1].equals("--status")) {
            Status status = Status.valueOf(tokens[2].toUpperCase());
            items = service.listByStatus(status);
        } else if (tokens.length >= 3 && tokens[1].equals("--category")) {
            items = service.listByCategory(tokens[2]);
        } else {
            items = service.listAll();
        }
        TablePrinter.printItems(items);
    }

    private void executeGet(String[] tokens) {
        if (tokens.length < 2) { System.out.println("사용법: get <id>"); return; }
        long id = Long.parseLong(tokens[1]);
        Optional<MonitorItem> item = service.findById(id);
        if (item.isEmpty()) {
            System.out.println("찾을 수 없습니다: id=" + id);
            return;
        }
        TablePrinter.printItem(item.get());
    }

    private void executeAdd(String[] tokens) {
        if (tokens.length < 4) { System.out.println("사용법: add <name> <category> <value>"); return; }
        String name = tokens[1];
        String category = tokens[2];
        double value = Double.parseDouble(tokens[3]);
        MonitorItem item = service.add(name, category, value, Status.ACTIVE);
        System.out.println("추가됨: id=" + item.getId() + ", name=" + item.getName());
    }

    private void executeUpdate(String[] tokens) {
        if (tokens.length < 4) { System.out.println("사용법: update <id> --value <v> | --status <s>"); return; }
        long id = Long.parseLong(tokens[1]);
        String option = tokens[2];
        String val = tokens[3];
        if (option.equals("--value")) {
            MonitorItem item = service.updateValue(id, Double.parseDouble(val));
            System.out.println("수정됨: id=" + item.getId() + ", value=" + item.getValue());
        } else if (option.equals("--status")) {
            Status status = Status.valueOf(val.toUpperCase());
            MonitorItem item = service.updateStatus(id, status);
            System.out.println("수정됨: id=" + item.getId() + ", status=" + item.getStatus());
        } else {
            System.out.println("알 수 없는 옵션: " + option);
        }
    }

    private void executeDelete(String[] tokens) {
        if (tokens.length < 2) { System.out.println("사용법: delete <id>"); return; }
        long id = Long.parseLong(tokens[1]);
        boolean deleted = service.delete(id);
        if (deleted) {
            System.out.println("삭제됨: id=" + id);
        } else {
            System.out.println("찾을 수 없습니다: id=" + id);
        }
    }

    private void executeSummary() {
        SummaryResult result = service.summarize();
        TablePrinter.printSummary(result);
    }

    private void executeSeed() {
        service.seed();
        System.out.println("seed 완료: " + service.listAll().size() + "건 적재됨");
    }

    private void executeHelp() {
        System.out.println("사용 가능한 명령어:");
        System.out.println("  list [--status <STATUS>] [--category <CATEGORY>]");
        System.out.println("  get <id>");
        System.out.println("  add <name> <category> <value>");
        System.out.println("  update <id> --value <value>");
        System.out.println("  update <id> --status <STATUS>");
        System.out.println("  delete <id>");
        System.out.println("  summary");
        System.out.println("  seed");
        System.out.println("  help");
        System.out.println("  exit");
    }
}
