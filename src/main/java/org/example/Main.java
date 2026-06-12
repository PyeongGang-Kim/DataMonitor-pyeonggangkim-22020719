package org.example;

import org.example.cli.CommandParser;
import org.example.repository.InMemoryRepository;
import org.example.service.MonitorService;
import org.example.service.SeedData;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        InMemoryRepository repository = new InMemoryRepository();
        MonitorService service = new MonitorService(repository);
        new SeedData().seed(service);

        CommandParser parser = new CommandParser(service);
        Scanner scanner = new Scanner(System.in);

        System.out.println("DataMonitor started. 명령어를 입력하세요. (help / exit)");
        while (true) {
            System.out.print("> ");
            System.out.flush();
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit")) {
                System.out.println("종료합니다.");
                break;
            }
            parser.execute(line);
        }
        scanner.close();
    }
}
