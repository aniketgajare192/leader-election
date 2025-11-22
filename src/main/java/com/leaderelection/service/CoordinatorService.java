package com.leaderelection.service;

import com.leaderelection.model.PodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class CoordinatorService {
  private final Map<String, PodInfo> registeredPods = new ConcurrentHashMap<>();
  private final ReentrantLock electionLock = new ReentrantLock();
  private static final long HEARTBEAT_TIMEOUT_SECONDS = 5;
  private static final int MAX_PODS = 10;

  public boolean registerPod(String podId, String url) {
    if (registeredPods.size() >= MAX_PODS) {
      log.warn("Maximum number of pods ({}) reached. Cannot register pod: {}", MAX_PODS, podId);
      return false;
    }

    PodInfo podInfo = new PodInfo(podId, url);
    registeredPods.put(podId, podInfo);
    log.info("Pod {} registered successfully at {}", podId, url);

    electLeader();
    return true;
  }

  public void heartbeat(String podId) {
    PodInfo podInfo = registeredPods.get(podId);
    if (podInfo != null) {
      podInfo.updateHeartbeat();
      log.debug("Received heartbeat from pod: {}", podId);
    }
  }

  public Optional<String> getCurrentLeader() {
    return registeredPods.values().stream()
        .filter(PodInfo::isLeader)
        .filter(PodInfo::isActive)
        .map(PodInfo::getPodId)
        .findFirst();
  }

  public List<PodInfo> getAllPods() {
    return new ArrayList<>(registeredPods.values());
  }

  public void unregisterPod(String podId) {
    PodInfo removed = registeredPods.remove(podId);
    if (removed != null) {
      log.info("Pod {} unregistered", podId);
      if (removed.isLeader()) {
        electLeader();
      }
    }
  }

  @Scheduled(fixedRate = 2000)
  public void checkPodHealth() {
    boolean reElectNeeded = false;
    LocalDateTime now = LocalDateTime.now();

    for (PodInfo pod : registeredPods.values()) {
      long secondsSinceHeartbeat = ChronoUnit.SECONDS.between(pod.getLastHeartbeat(), now);

      if (secondsSinceHeartbeat > HEARTBEAT_TIMEOUT_SECONDS) {
        if (pod.isActive()) {
          log.warn("Pod {} has not sent heartbeat for {} seconds. Marking as inactive.",
              pod.getPodId(), secondsSinceHeartbeat);
          pod.setActive(false);

          if (pod.isLeader()) {
            reElectNeeded = true;
            log.warn("Leader pod {} is inactive. Re-election needed.", pod.getPodId());
          }
        }
      } else {
        if (!pod.isActive()) {
          log.info("Pod {} recovered. Reactivating.", pod.getPodId());
          pod.setActive(true);
          reElectNeeded = true;
        }
      }
    }

    if (reElectNeeded) {
      electLeader();
    }
  }

  private void electLeader() {
    electionLock.lock();
    try {
      List<PodInfo> activePods = registeredPods.values().stream()
          .filter(PodInfo::isActive)
          .sorted(Comparator.comparing(PodInfo::getPodId))
          .toList();

      if (activePods.isEmpty()) {
        log.warn("No active pods available for leader election");
        return;
      }

      Optional<PodInfo> currentLeader = activePods.stream()
          .filter(PodInfo::isLeader)
          .findFirst();

      if (currentLeader.isPresent() && currentLeader.get().isActive()) {
        log.debug("Current leader {} is still active. No re-election needed.",
            currentLeader.get().getPodId());
        return;
      }

      registeredPods.values().forEach(pod -> pod.setLeader(false));

      PodInfo newLeader = activePods.get(0);
      newLeader.setLeader(true);

      log.info("New leader elected: {} from {} active pod(s)",
          newLeader.getPodId(), activePods.size());
    } finally {
      electionLock.unlock();
    }
  }

  public boolean isPodActive(String podId) {
    PodInfo pod = registeredPods.get(podId);
    return pod != null && pod.isActive();
  }
}

