package com.wang.partner.model.domain.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wang.partner.model.domain.Space;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceVO extends Space implements Serializable {

    private static final long serialVersionUID = 5109067755331615323L;
    private String username;
    private String avatarUrl;
    private Boolean isBadge  = false;
}
