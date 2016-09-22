package com.enstage.wibmo.sdk.inapp.pojo;

import java.io.Serializable;


/**
 * Created by nithyak on 15/09/16.
 */

public class InAppCancelReason implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String label;

    public InAppCancelReason(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
