package com.wang.partner.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 22603
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultMessage {
    private boolean isSystem;
    private String fromName;
    private String userAvatar;
    private String nowTime;
    private Object message;
}
