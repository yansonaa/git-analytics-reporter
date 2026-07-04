package com.gitanalytics.controller;

import com.gitanalytics.service.GitCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CollectController {

    private final GitCollectorService gitCollectorService;

    @PostMapping("/local")
    public ResponseEntity<Map<String, Object>> collectFromLocal(
            @RequestParam String repoPath,
            @RequestParam String projectId,
            @RequestParam(required = false) LocalDateTime since) {

        if (since == null) {
            since = LocalDateTime.now().minusMonths(1);
        }
        // 采集本地数据
        int count = gitCollectorService.collectFromLocalPath(repoPath, projectId, since);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "collected", count,
                "projectId", projectId,        "since", since.toString()
        ));
    }
}
