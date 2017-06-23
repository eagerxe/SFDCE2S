/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.services;

/**
 *
 * @author Dell
 */
public class CommonService {
    public static final String FILE_NAME ="ConfigFile.xml";
    public static final String ACTIVE_USERS_QUERY = "select id, Sales_Person__c, Region__c, Localidad__c From User where isActive = true";
}
