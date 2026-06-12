package org.example.service;

import org.example.model.Status;

public class SeedData {
    public void seed(MonitorService service) {
        service.add("CPU 사용률", "system", 45.2, Status.ACTIVE);
        service.add("메모리 사용률", "system", 78.5, Status.WARNING);
        service.add("디스크 I/O", "storage", 12.3, Status.ACTIVE);
        service.add("네트워크 IN", "network", 102.4, Status.ACTIVE);
        service.add("네트워크 OUT", "network", 88.1, Status.ACTIVE);
        service.add("DB 커넥션 수", "database", 95.0, Status.WARNING);
        service.add("캐시 히트율", "cache", 67.8, Status.ACTIVE);
        service.add("응답 시간", "web", 320.0, Status.INACTIVE);
        service.add("에러율", "web", 2.5, Status.WARNING);
        service.add("스레드 수", "system", 55.0, Status.ACTIVE);
    }
}
