/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.objects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dell
 */
public class Integracion {

    private String username;
    private String password;
    private String token;
    private String objectName;
    private String rutaPendientes;
    private String prefijoArchivo;
    private String rutaProcesados;
    private Boolean inputHasHeaders;
    private String externalId;
    private Integer batchSize;

    private String email;
    private Enum enviromentType;

    public String getPrefijoArchivo() {
        return prefijoArchivo;
    }

    public void setPrefijoArchivo(String prefijoArchivo) {
        this.prefijoArchivo = prefijoArchivo;
    }

    public String getExternalId() {
        return externalId;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Boolean getInputHasHeaders() {
        return inputHasHeaders;
    }

    public void setInputHasHeaders(Boolean inputHasHeaders) {
        this.inputHasHeaders = inputHasHeaders;
    }

    public String getRutaPendientes() {
        return rutaPendientes;
    }

    public String getRutaProcesados() {
        return rutaProcesados;
    }

    public void setRutaPendientes(String rutaPendientes) {
        this.rutaPendientes = rutaPendientes;
    }

    public void setRutaProcesados(String rutaProcesados) {
        this.rutaProcesados = rutaProcesados;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getEmail() {
        return email;
    }

    public Enum getEnviromentType() {
        return enviromentType;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEnviromentType(Enum enviromentType) {
        this.enviromentType = enviromentType;
    }

    private List<AttributeMapping> mappings = new ArrayList<AttributeMapping>();

    public List<AttributeMapping> getMappings() {
        return mappings;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public void setMappings(List<AttributeMapping> mappings) {
        this.mappings = mappings;
    }

    public void setMapping(AttributeMapping myAttributeMapping) {

        for (AttributeMapping map : mappings) {
            if (map.equals(myAttributeMapping)) {
                this.mappings.remove(map);
                break;
            }
        }
        this.mappings.add(myAttributeMapping);
    }

    public AttributeMapping getAttributeMappingByName(String columnName) {

        for (AttributeMapping map : mappings) {

            if (map.getSourceColumn().equals(columnName)) {

                return map;
            }
        }

        return null;
    }

    public AttributeMapping getAttributeMappingByPosition(Integer columnIndex) {

        if (mappings.size() > columnIndex) {

            return mappings.get(columnIndex);
        }

        return null;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Integracion{" + "username=" + username + ", objectName=" + objectName + ", email=" + email + ", enviromentType=" + enviromentType + '}';
    }
}
