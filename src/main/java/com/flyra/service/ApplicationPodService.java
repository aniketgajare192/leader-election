package com.flyra.service;

import com.flyra.config.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationPodService {
  private final ApplicationConfig config;
  private final RestTemplate restTemplate;
  private volatile boolean isLeader = false;
  private volatile boolean isRegistered = false;

  @PostConstruct
  public void registerWithCoordinator() {
    if ("application".equals(config.getRole())) {
      register();
    }
  }

  private void register() {
    String coordinatorUrl = config.getCoordinatorUrl();
    String podId = config.getPodId();
    String podUrl = "http://localhost:" + config.getApplicationPort();

    Map<String, String> request = new HashMap<>();
    request.put("podId", podId);
    request.put("url", podUrl);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
          coordinatorUrl + "/coordinator/register",
          HttpMethod.POST,
          entity,
          new ParameterizedTypeReference<Map<String, Object>>() {}
      );

      if (response.getStatusCode() == HttpStatus.OK) {
        isRegistered = true;
        Map<String, Object> body = response.getBody();
        String currentLeader = null;
        if (body != null && body.get("currentLeader") != null) {
          currentLeader = (String) body.get("currentLeader");
          isLeader = podId.equals(currentLeader);
        }
        log.info("Successfully registered with coordinator. Leader: {}", currentLeader);
      } else {
        log.error("Failed to register with coordinator. Status: {}", response.getStatusCode());
      }
    } catch (RestClientException e) {
      log.error("Error registering with coordinator: {}", e.getMessage());
    }
  }

    @Scheduled(fixedRate = 1000)
    public void sendHeartbeat() {
      if (!"application".equals(config.getRole())) {
        return;
      }

      if (!isRegistered) {
        register();
        return;
      }

      String coordinatorUrl = config.getCoordinatorUrl();
      String podId = config.getPodId();

      try {
        restTemplate.postForEntity(
            coordinatorUrl + "/coordinator/heartbeat/" + podId,
            null,
            String.class
        );

        checkLeaderStatus();
      } catch (RestClientException e) {
        log.error("Error sending heartbeat: {}", e.getMessage());
        isRegistered = false;
      }
    }

  private void checkLeaderStatus() {
    String coordinatorUrl = config.getCoordinatorUrl();
    String podId = config.getPodId();

    try {
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
          coordinatorUrl + "/coordinator/leader",
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<Map<String, Object>>() {}
      );

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Map<String, Object> body = response.getBody();
        String leader = (String) body.get("leader");
        isLeader = podId.equals(leader);
      }
    } catch (RestClientException e) {
      log.error("Error checking leader status: {}", e.getMessage());
    }
  }

    @Scheduled(fixedRate = 1000)
    public void printStatus() {
      if (!"application".equals(config.getRole()) || !isRegistered) {
        return;
      }

      if (isLeader) {
        System.out.println("I am leader pod");
      } else {
        System.out.println("I am application pod");
      }
    }

  public boolean isLeader() {
    return isLeader;
  }

  public boolean isRegistered() {
    return isRegistered;
  }
}

