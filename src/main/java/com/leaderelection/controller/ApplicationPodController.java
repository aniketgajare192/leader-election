package com.leaderelection.controller;

import com.leaderelection.service.ApplicationPodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
@Slf4j
public class ApplicationPodController {
  private final ApplicationPodService applicationPodService;

  @GetMapping("/status")
  public ResponseEntity<Map<String, Object>> getStatus() {
    Map<String, Object> status = new HashMap<>();
    status.put("isLeader", applicationPodService.isLeader());
    status.put("isRegistered", applicationPodService.isRegistered());
    return ResponseEntity.ok(status);
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of("status", "UP"));
  }
}

