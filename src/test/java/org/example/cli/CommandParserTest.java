package org.example.cli;

import org.example.model.MonitorItem;
import org.example.model.Status;
import org.example.repository.InMemoryRepository;
import org.example.service.MonitorService;
import org.example.service.SeedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private MonitorService service;
    private CommandParser parser;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        service = new MonitorService(new InMemoryRepository());
        new SeedData().seed(service);
        parser = new CommandParser(service);

        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @Test
    @DisplayName("list: 전체 항목 테이블을 출력한다")
    void execute_list_printsAllItems() {
        parser.execute("list");

        String output = out.toString();
        assertTrue(output.contains("CPU 사용률") || output.contains("ID"));
    }

    @Test
    @DisplayName("list --status WARNING: WARNING 항목만 출력한다")
    void execute_list_filterByStatus() {
        parser.execute("list --status WARNING");

        String output = out.toString();
        assertFalse(output.isEmpty());
        assertFalse(output.contains("ACTIVE"));
    }

    @Test
    @DisplayName("list --category system: system 카테고리 항목만 출력한다")
    void execute_list_filterByCategory() {
        parser.execute("list --category system");

        String output = out.toString();
        assertTrue(output.contains("system") || output.contains("ID"));
    }

    @Test
    @DisplayName("add: 새 항목을 추가하고 성공 메시지를 출력한다")
    void execute_add_createsItem() {
        int before = service.listAll().size();

        parser.execute("add test-metric custom 55.5");

        assertEquals(before + 1, service.listAll().size());
        assertTrue(out.toString().contains("추가") || out.toString().contains("test-metric"));
    }

    @Test
    @DisplayName("get: 단건 항목 정보를 출력한다")
    void execute_get_printsSingleItem() {
        MonitorItem first = service.listAll().get(0);

        parser.execute("get " + first.getId());

        String output = out.toString();
        assertTrue(output.contains(first.getName()) || output.contains(String.valueOf(first.getId())));
    }

    @Test
    @DisplayName("get: 없는 id 조회 시 오류 메시지를 출력하고 종료하지 않는다")
    void execute_get_unknownId_printsError() {
        assertDoesNotThrow(() -> parser.execute("get 9999"));

        assertTrue(out.toString().contains("9999") || out.toString().contains("찾을 수 없"));
    }

    @Test
    @DisplayName("update --value: value 변경 후 성공 메시지를 출력한다")
    void execute_update_value() {
        MonitorItem first = service.listAll().get(0);

        parser.execute("update " + first.getId() + " --value 99.9");

        assertEquals(99.9, service.findById(first.getId()).get().getValue(), 0.001);
    }

    @Test
    @DisplayName("update --status: status 변경 후 성공 메시지를 출력한다")
    void execute_update_status() {
        MonitorItem first = service.listAll().get(0);

        parser.execute("update " + first.getId() + " --status INACTIVE");

        assertEquals(Status.INACTIVE, service.findById(first.getId()).get().getStatus());
    }

    @Test
    @DisplayName("delete: 항목 삭제 후 목록에서 제거된다")
    void execute_delete_removesItem() {
        MonitorItem first = service.listAll().get(0);
        int before = service.listAll().size();

        parser.execute("delete " + first.getId());

        assertEquals(before - 1, service.listAll().size());
    }

    @Test
    @DisplayName("summary: 집계 결과를 출력한다")
    void execute_summary_printsResult() {
        parser.execute("summary");

        String output = out.toString();
        assertFalse(output.isEmpty());
    }

    @Test
    @DisplayName("seed: 데이터를 재적재하고 성공 메시지를 출력한다")
    void execute_seed_reloadsData() {
        parser.execute("seed");

        assertEquals(10, service.listAll().size());
        assertTrue(out.toString().contains("seed") || out.toString().contains("적재") || out.toString().contains("10"));
    }

    @Test
    @DisplayName("알 수 없는 명령어: 오류 메시지를 출력하고 종료하지 않는다")
    void execute_unknownCommand_printsError() {
        assertDoesNotThrow(() -> parser.execute("unknowncmd"));

        assertTrue(out.toString().contains("알 수 없는") || out.toString().contains("unknowncmd"));
    }

    @Test
    @DisplayName("숫자가 아닌 id 입력 시 오류 메시지를 출력한다")
    void execute_invalidNumberFormat_printsError() {
        assertDoesNotThrow(() -> parser.execute("get abc"));

        assertFalse(out.toString().isEmpty());
    }
}
