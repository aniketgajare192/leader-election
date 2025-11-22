package com.flyra.controller;

import com.flyra.model.PodInfo;
import com.flyra.service.CoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/coordinator")
@RequiredArgsConstructor
@Slf4j
public class CoordinatorController {
  private final CoordinatorService coordinatorService;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerPod(@RequestBody RegisterRequest request) {
    boolean registered = coordinatorService.registerPod(request.getPodId(), request.getUrl());

    Map<String, Object> response = new HashMap<>();
    if (registered) {
      response.put("status", "registered");
      Optional<String> leader = coordinatorService.getCurrentLeader();
      response.put("currentLeader", leader.orElse(null));
      return ResponseEntity.ok(response);
    } else {
      response.put("status", "failed");
      response.put("message", "Registration failed - maximum pods reached");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  @PostMapping("/heartbeat/{podId}")
  public ResponseEntity<Map<String, String>> heartbeat(@PathVariable String podId) {
    coordinatorService.heartbeat(podId);
    return ResponseEntity.ok(Map.of("status", "ok"));
  }

  @GetMapping("/leader")
  public ResponseEntity<Map<String, Object>> getLeader() {
    Optional<String> leader = coordinatorService.getCurrentLeader();
    Map<String, Object> response = new HashMap<>();
    leader.ifPresent(l -> response.put("leader", l));
    response.put("hasLeader", leader.isPresent());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/pods")
  public ResponseEntity<List<PodInfo>> getAllPods() {
    return ResponseEntity.ok(coordinatorService.getAllPods());
  }

  @DeleteMapping("/unregister/{podId}")
  public ResponseEntity<Map<String, String>> unregisterPod(@PathVariable String podId) {
    coordinatorService.unregisterPod(podId);
    return ResponseEntity.ok(Map.of("status", "unregistered"));
  }

  @GetMapping("/status")
  public ResponseEntity<Map<String, Object>> getStatus() {
    List<PodInfo> pods = coordinatorService.getAllPods();
    Optional<String> leader = coordinatorService.getCurrentLeader();

    Map<String, Object> status = new HashMap<>();
    status.put("totalPods", pods.size());
    status.put("activePods", pods.stream().filter(PodInfo::isActive).count());
    status.put("leader", leader.orElse("none"));
    status.put("pods", pods);

    return ResponseEntity.ok(status);
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class RegisterRequest {
    private String podId;
    private String url;
  }
}

