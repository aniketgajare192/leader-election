package com.flyra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodInfo {
  private String podId;
  private String url;
  private LocalDateTime lastHeartbeat;
  private boolean isLeader;
  private boolean isActive;

  public PodInfo(String podId, String url) {
    this.podId = podId;
    this.url = url;
    this.lastHeartbeat = LocalDateTime.now();
    this.isLeader = false;
    this.isActive = true;
  }

  public void updateHeartbeat() {
    this.lastHeartbeat = LocalDateTime.now();
    this.isActive = true;
  }
}

