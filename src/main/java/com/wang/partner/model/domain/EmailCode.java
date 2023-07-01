package com.wang.partner.model.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmailCode {
    private String email;
    private String Code;
}
