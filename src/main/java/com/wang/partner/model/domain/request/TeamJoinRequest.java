package com.wang.partner.model.domain.request;

import lombok.Data;

import java.io.Serializable;
@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -336873535103454774L;
    private  Long teamId;
    private  String password;
}
