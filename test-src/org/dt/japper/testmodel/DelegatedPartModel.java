package org.dt.japper.testmodel;

import java.math.BigDecimal;
import java.util.Map;

public class DelegatedPartModel {
    private String delegatedId;

    private PartModel part = new PartModel();

    public String getDelegatedId() {
        return delegatedId;
    }

    public void setDelegatedId(String delegatedId) {
        this.delegatedId = delegatedId;
    }

    public String getPartno() {
        return part.getPartno();
    }

    public void setPartno(String partno) {
        part.setPartno(partno);
    }

    public String getDescription() {
        return part.getDescription();
    }

    public void setDescription(String description) {
        part.setDescription(description);
    }

    // NOTE: We deliberately leave off the partType property to ensure
    // that an incomplete delegation does not cause an NPE
    // See https://github.com/sykesd/japper/issues/31

    public Map<String, BigDecimal> getFlexFields() {
        return part.getFlexFields();
    }

    public void setFlexFields(Map<String, BigDecimal> flexFields) {
        part.setFlexFields(flexFields);
    }

    public PartModel getPart() {
        return part;
    }
}
