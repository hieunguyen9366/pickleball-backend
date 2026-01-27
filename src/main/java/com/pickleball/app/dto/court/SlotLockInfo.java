package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotLockInfo {
    private Long slotId;
    private boolean isLocked;
    private Long lockedByUserId; // null nếu không bị lock
}

