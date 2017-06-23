/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.objects;

/**
 *
 * @author Dell
 */
public class AttributeMapping {
    private String sourceType;
    private String destinationType;
    private String sourceColumn;
    private String destinationColumn;
    private Boolean isLookup;

    public Boolean getIsLookup() {
        return isLookup;
    }

    public void setIsLookup(Boolean isLookup) {
        this.isLookup = isLookup;
    }        

    public String getSourceColumn() {
        return sourceColumn;
    }

    public String getDestinationColumn() {
        return destinationColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public void setDestinationColumn(String destinationColumn) {
        this.destinationColumn = destinationColumn;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }
}
