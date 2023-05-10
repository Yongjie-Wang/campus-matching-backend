package com.wang.partner.model.domain.request;

import lombok.Data;

import java.io.Serializable;
@Data
public class DeleteRequest  implements Serializable {
    private static final long serialVersionUID = 9169028981599362308L;
    private long id;
}
