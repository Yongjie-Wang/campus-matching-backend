package com.wang.partner.model.domain.request;

import lombok.Data;

import java.io.Serializable;
@Data
public class TeamQuitRequest  implements Serializable {
    private static final long serialVersionUID = 6972559759689828668L;
    private Long teamId;
}
