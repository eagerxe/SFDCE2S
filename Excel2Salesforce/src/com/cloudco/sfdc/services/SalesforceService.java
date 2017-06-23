/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.services;

import com.cloudco.sfdc.helpers.TransformationHelper;
import com.cloudco.sfdc.objects.AttributeMapping;
import com.cloudco.sfdc.objects.EnviromentTypes;
import com.cloudco.sfdc.objects.Integracion;
import com.cloudco.sfdc.objects.LoginInfo;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.EmailPriority;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SendEmailResult;
import com.sforce.soap.partner.SingleEmailMessage;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Dell
 */
public class SalesforceService {

    private static Logger logger = Logger.getLogger(SalesforceService.class.getName());
    public static PartnerConnection connection;

    public static boolean isValidUser(Integracion myIntegracion) {

        logger.info("Validando Usuario : " + myIntegracion.getUsername() + " En Ambiente: " + myIntegracion.getEnviromentType());
        if (!myIntegracion.getUsername().equals("") && !myIntegracion.getPassword().equals("")) {
            ConnectorConfig config = new ConnectorConfig();
            config.setUsername(myIntegracion.getUsername());
            config.setPassword(myIntegracion.getPassword() + myIntegracion.getToken());

            //Configuramos nuestro ambiente Production or Sandbox
            if (myIntegracion.getEnviromentType() == EnviromentTypes.PRODUCTION) {
                config.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/40.0");
            } else if (myIntegracion.getEnviromentType() == EnviromentTypes.SANDBOX) {
                config.setAuthEndpoint("https://test.salesforce.com/services/Soap/u/40.0");
            }
            try {
                connection = Connector.newConnection(config);

            } catch (ConnectionException e) {
                logger.info("ERROR in Loging for user : " + myIntegracion.getUsername());
                logger.error("ERROR in Loging ex : " + e);
                return false;
            }

            if (connection != null) {
                if (connection.getSessionHeader() != null) {

                    //Llenamos nuestro objeto en momoria para guardar nuestra session
                    LoginInfo.USERNAME = myIntegracion.getUsername();
                    LoginInfo.PASSWORD = myIntegracion.getPassword() + myIntegracion.getToken();
                    LoginInfo.SESSIONID = connection.getSessionHeader().getSessionId();
                    //LoginInfo.SERVERURL = mySession.getPartnerServerUrl();

                    logger.info("Usuario Validado : " + myIntegracion.getUsername() + " En Ambiente: " + myIntegracion.getEnviromentType());
                    return true;
                }
            } else {
                logger.error("Usuario Validado : " + myIntegracion.getUsername() + " En Ambiente: " + myIntegracion.getEnviromentType());
                return false;
            }
        }

        return false;
    }

    public static UpsertResult[] upsert(List<SObject> myObjs, String externalID, int batchsize) {

        int contador = 1;
        List<SObject> mySObjectToUpsert = new ArrayList();
        List<UpsertResult> myResult = new ArrayList();
        for (SObject mySObj : myObjs) {

            if (contador < batchsize) {
                mySObjectToUpsert.add(mySObj);
                contador++;
            } else {
                mySObjectToUpsert.add(mySObj);
                try {
                    UpsertResult[] myResultBatch = connection.upsert(externalID, TransformationHelper.convertToSObjectArray(mySObjectToUpsert));
                    myResult.addAll(Arrays.asList(myResultBatch));
                    mySObjectToUpsert.clear();
                    contador = 1;
                } catch (ConnectionException ex) {
                    logger.error(ex);
                }
            }
        }

        //Mandamos los registros que queden por insertar.
        try {
            UpsertResult[] myResultBatch = connection.upsert(externalID, TransformationHelper.convertToSObjectArray(mySObjectToUpsert));
            myResult.addAll(Arrays.asList(myResultBatch));
            mySObjectToUpsert.clear();
        } catch (ConnectionException ex) {
            logger.error(ex);
        }

        //return new UpsertBatchMethod(externalID, myObjs, false, batchsize).invoke(myPartnerStub);
        return TransformationHelper.convertToUpsertResultArray(myResult);
    }

    public static List<SObject> query(String query, int batchSize) {

        List<SObject> myResultObjects = new ArrayList();

        connection.setQueryOptions(batchSize);
        try {
            String soqlQuery = query;
            QueryResult qr = connection.queryAll(soqlQuery);
            boolean done = false;
            if (qr.getSize() > 0) {
                while (!done) {
                    SObject[] records = qr.getRecords();
                    myResultObjects.addAll(Arrays.asList(records));

                    if (qr.isDone()) {
                        done = true;
                    } else {
                        qr = connection.queryMore(qr.getQueryLocator());
                    }
                }
            } else {
                System.out.println("No records found.");
            }
        } catch (ConnectionException ex) {
            logger.error(" QUERY  : Error  --  " + ex);
        }

        return myResultObjects;

    }

    public static Map<String, SObject> getActiveUsers() {

        List<SObject> myUsers = query(CommonService.ACTIVE_USERS_QUERY, 200);

        Map<String, SObject> myUsersMap = new HashMap<String, SObject>();
        for (SObject obj : myUsers) {

            String salesPerson = obj.getField("Sales_Person__c").toString();
            if (salesPerson != null) {
                myUsersMap.put(salesPerson, obj);
            }
        }

        return myUsersMap;

    }
    public static String getAccount (String clienteSAP){
        String id = null;
        String soqlQuery = "SELECT Id,LastName FROM Account where Cliente_SAP__c='"+clienteSAP+"'";
        List<SObject> cuenta = query(soqlQuery,1); 
        for (SObject obj : cuenta) {

            String idCuenta = obj.getId();
            String apellido = obj.getField("LastName").toString();
            if (idCuenta != null) {
                id=idCuenta;
            }
        }
        return id;
    }

    public static SObject createSObjectC(Integracion myIntegracion, List<Object> values) {
        System.out.println("creando Sobject");
        
        System.out.println("values.size: " + values.size());
        System.out.println("");
        if (values.size() >= 75) {
            System.out.println("creando Sobject");
            SObject sobj = new SObject();
            sobj.setType(myIntegracion.getObjectName());
            sobj.setField("ExternalID__c", System.currentTimeMillis() + "-" + "I");
            System.out.println("Object name: " + myIntegracion.getObjectName());
            for (int j = 0; j < values.size(); j++) {
                //obtenemos Mapeo
                AttributeMapping attMap = null;
                attMap = myIntegracion.getAttributeMappingByPosition(j);
                //System.out.println("attMAp"+myIntegracion.getAttributeMappingByPosition(j)+" "+j);
                //System.out.println(attMap.getDestinationColumn());
                if (attMap != null) {
                    System.out.print("Campo: " + attMap.getDestinationColumn() + " ");
                    
                    System.out.println("DestinationType: " + attMap.getDestinationType() + " ");
                    if (attMap.getSourceType().equals("Double")) {
                        System.out.println("Entro a Double:");
                        if (attMap.getDestinationType().equals("Double")) {
                            System.out.println("Double sin cast: "+values.get(j));
                            sobj.setField(attMap.getDestinationColumn(), values.get(j));
                        } else if (attMap.getDestinationType().equals("Integer")) {
                            System.out.println("Double to integer");
                            System.out.println("Valor: " + values.get(j));
                            if(!values.get(j).equals("")){
                                Double d = Double.valueOf(values.get(j).toString());
                                System.out.println("Integer: " + d.intValue());
                                sobj.setField(attMap.getDestinationColumn(), d.intValue());
                            }else{
                                sobj.setField(attMap.getDestinationColumn(), null);
                            }
                            
                        } else if (attMap.getDestinationType().equals("String")) {
                            sobj.setField(attMap.getDestinationColumn(), values.get(j).toString());
                            System.out.println("Double con cast: " + values.get(j).toString());
                        }
                    } else if (attMap.getSourceType().equals("Date")) {

                        if (!values.get(j).equals("")) {

                            System.out.println("Entro a fecha: " + values.get(j));
                            
                            DateFormat outputDateFormat = new SimpleDateFormat("YYYY-MM-dd");
                            //sobj.setField(attMap.getDestinationColumn(),TransformationHelper.parseToDateTime(outputDateFormat.toString(), "dd/MM/yyyy"));
                            sobj.setField(attMap.getDestinationColumn(), Date.valueOf(outputDateFormat.format(values.get(j))));
                            System.out.println("Fecha: " + outputDateFormat.format(values.get(j)));
                        } else {
                            sobj.setField(attMap.getDestinationColumn(), values.get(j));
                            System.out.println("Fecha en blanco");
                        }

                    } else if (attMap.getSourceType().equals("String")) {
                        //query
                        if(attMap.getDestinationColumn().equals("Cliente_SAP__c")){
                            if (!values.get(j).equals("")){
                               try{
                                String idCuenta = SalesforceService.getAccount(values.get(j).toString());
                                System.out.println("id cuenta: "+idCuenta);
                                sobj.setField("Cuenta__c", idCuenta);
                                }catch(Error e){
                                
                                } 
                            }
                            
                        }
//                        sobj.setField(attMap.getDestinationColumn(),TransformationHelper.getString(values.get(j)));
                        if (attMap.getSourceType().equals("String")) {
                            sobj.setField(attMap.getDestinationColumn(), values.get(j).toString());
                            System.out.println("String: " + values.get(j).toString());
                        }
                        //sobj.setField(attMap.getDestinationColumn(), String.valueOf(values.get(j)));
                    }
                } else {
                    continue;
                }

            }
            //System.out.println("sobj"+sobj);
            return sobj;

        } else {
            return null;
        }

    }

    public static SObject createSObject(Integracion myIntegracion, List<String> header, List<String> values, Map<String, SObject> myUsersMap) {

        if (values.size() >= 29) {
            SObject sobj = new SObject();
            sobj.setType(myIntegracion.getObjectName());
            for (int j = 0; j < values.size(); j++) {

                //Obtenemos nuestro mapeo
                AttributeMapping attMap = null;
                if (myIntegracion.getInputHasHeaders()) {
                    attMap = myIntegracion.getAttributeMappingByName(header.get(j));
                } else {
                    attMap = myIntegracion.getAttributeMappingByPosition(j);
                }

                if (attMap != null) {
                    //Revisamos el tipo de Dato del nuestro campo
                    if (attMap.getDestinationType().equals("Date") || attMap.getDestinationType().equals("Datetime")) {

                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.parseToDateTime(values.get(j), "dd/MM/yyyy"));

                    } else if (attMap.getIsLookup()) {

                        if (attMap.getSourceColumn().equals("SalesPerson")) {

                            SObject userObj = myUsersMap.get(values.get(j).trim());
                            if (userObj != null) {
                                //Obtenemos el id del salesperson 
                                sobj.setField("OwnerId", userObj.getField("Id"));
                                String regionTmp = userObj.getField("Region__c").toString();
                                if (regionTmp.length() >= 2) {
                                    regionTmp = regionTmp.substring(0, 1);
                                }
                                sobj.setField("Region__c", TransformationHelper.getString(regionTmp));
                                sobj.setField(attMap.getDestinationColumn(), values.get(j).trim());
                            } else {
                                logger.error("No se encontro al Sales Person :" + values.get(j));
                            }

                        } else if (attMap.getSourceColumn().equals("ParentAccount")) {

                            Integer parentID = TransformationHelper.getInteger(values.get(j).trim());

                            if (parentID != null && parentID != 0) {
                                String[] objectAtt = attMap.getDestinationColumn().split("\\.");
                                if (objectAtt.length == 2) {
                                    SObject myOwnerObj = new SObject();
                                    myOwnerObj.setType(attMap.getDestinationType());
                                    myOwnerObj.setField(objectAtt[1], parentID + "-" + values.get(28).trim());
                                    sobj.setField(objectAtt[0], myOwnerObj);
                                } else {

                                    logger.error(" Se encontró un error en el Mapeo - " + attMap.getDestinationType() + " Column : " + attMap.getDestinationColumn());
                                }
                            }
                        } else {
                            String[] objectAtt = attMap.getDestinationColumn().split("\\.");
                            if (objectAtt.length == 2) {
                                SObject myOwnerObj = new SObject();
                                myOwnerObj.setType(attMap.getDestinationType());
                                myOwnerObj.setField(objectAtt[1], values.get(j).trim());
                                sobj.setField(objectAtt[0], myOwnerObj);
                            } else {

                                logger.error(" Se encontró un error en el Mapeo - " + attMap.getDestinationType() + " Column : " + attMap.getDestinationColumn());
                            }

                        }

                    } else if (attMap.getSourceColumn().equals("RFC")) {

                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.parseRFC(values.get(j)));

                    } else if (attMap.getSourceColumn().equals("STREET")) {
                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.parseStreet(values.get(j)));

                    } else if (attMap.getSourceType().equals("Integer")) {

                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.getInteger(values.get(j)));

                    } else if (attMap.getSourceType().equals("Double")) {

                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.getDouble(values.get(j)));

                    } else if (attMap.getSourceType().equals("Boolean")) {

                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.getBoolean(values.get(j)));

                    } else {

                        //if(attMap.getDestinationColumn().equals("Industry") && values.get(j).length()>=39){
                        //    sobj.setField(attMap.getDestinationColumn(),TransformationHelper.getString(values.get(j).substring(0, 39)));
                        //}else{
                        sobj.setField(attMap.getDestinationColumn(), TransformationHelper.getString(values.get(j)));
                        //}
                    }
                } else {
                    continue;
                }
            }

            //Copiamos la Billing address a la Shipping Address
            sobj.setField("ShippingStreet", sobj.getField("BillingStreet"));
            sobj.setField("ShippingCity", sobj.getField("BillingCity"));
            sobj.setField("ShippingState", sobj.getField("BillingState"));
            sobj.setField("ShippingPostalCode", sobj.getField("BillingPostalCode"));
            sobj.setField("ShippingCountry", sobj.getField("BillingCountry"));

            //Tipo de Moneda
            sobj.setField("CurrencyIsoCode", TransformationHelper.getString(values.get(29).trim()));

            //Ponemos el External id  - ODMS_ID__c	+ "-" + Localidad__c
            sobj.setField("ExternalID__c", sobj.getField("ODMS_ID__c") + "-" + TransformationHelper.getInteger(sobj.getField("Localidad__c").toString()));

            return sobj;

        } else {
            return null;
        }

    }

    public static void sendNotificationMail(String email, String subject, String ouput, String date) {

        logger.info("Enviando notificacion de correo electrónico a " + email);
        if (!email.equals("")) {
            try {

                SingleEmailMessage message = new SingleEmailMessage();

                message.setEmailPriority(EmailPriority.High);
                message.setSubject(subject + date);

                // We can also just use an id for an implicit to address
                message.setPlainTextBody(ouput);

                message.setToAddresses(new String[]{email});
                SingleEmailMessage[] messages = {message};
                SendEmailResult[] results = connection.sendEmail(messages);
                if (results[0].isSuccess()) {
                    logger.info("The email was sent successfully.");
                } else {
                    logger.error("The email failed to send: " + results[0].getErrors()[0].getMessage());
                }
            } catch (ConnectionException ce) {
                logger.error(" MAIL  : Error  --  " + ce);
            }
        }

    }

    public static void iniciaLog(String processID) {

        SObject sobj = new SObject();
        sobj.setType("ParserResult__c");

        sobj.setField("ProcessID__c", processID);
        sobj.setField("Name", processID);
        sobj.setField("Status__c", "Not Started");

        List<SObject> myObjectsToUpsert = new ArrayList<SObject>();

        myObjectsToUpsert.add(sobj);

        try {
            UpsertResult[] myResultBatch = connection.upsert("ProcessID__c", TransformationHelper.convertToSObjectArray(myObjectsToUpsert));

        } catch (ConnectionException ex) {

            logger.error(" inicialLog  : Error  --  " + ex);

        }

    }

    public static void startParseLog(String processID, String fileName) {

        SObject sobj = new SObject();
        sobj.setType("ParserResult__c");

        sobj.setField("ProcessID__c", processID);
        sobj.setField("Name", processID);
        sobj.setField("Status__c", "Parser Started");
        sobj.setField("FileName__c", fileName);
        sobj.setField("ParserStarted__c", Calendar.getInstance());
        sobj.setField("LinesInFile__c", null);

        List<SObject> myObjectsToUpsert = new ArrayList<SObject>();

        myObjectsToUpsert.add(sobj);

        try {
            UpsertResult[] myResultBatch = connection.upsert("ProcessID__c", TransformationHelper.convertToSObjectArray(myObjectsToUpsert));

        } catch (ConnectionException ex) {

            logger.error(" inicialLog  : Error  --  " + ex);

        }

    }

    public static void endLog(String processID, Integer linesParsed, Integer linesProcessed, Integer linesSuccess, Integer linesError) {

        SObject sobj = new SObject();
        sobj.setType("ParserResult__c");

        sobj.setField("ProcessID__c", processID);
        sobj.setField("ParserFinished__c", Calendar.getInstance());
        sobj.setField("Status__c", "Finished");
        sobj.setField("LinesAcceptedByParser__c", linesParsed);
        sobj.setField("LinesRejectedByParser__c", linesParsed - linesProcessed);
        sobj.setField("Upserts__c", linesProcessed);
        sobj.setField("LinesAcceptedBySalesForce__c", linesSuccess);
        sobj.setField("LinesRejectedBySalesForce__c", linesError);

        List<SObject> myObjectsToUpsert = new ArrayList<SObject>();

        myObjectsToUpsert.add(sobj);

        try {
            UpsertResult[] myResultBatch = connection.upsert("ProcessID__c", TransformationHelper.convertToSObjectArray(myObjectsToUpsert));

        } catch (ConnectionException ex) {

            logger.error(" inicialLog  : Error  --  " + ex);

        }

    }

    public static void exceptionLog(String processID, String exception) {

        SObject sobj = new SObject();
        sobj.setType("ParserResult__c");

        sobj.setField("ProcessID__c", processID);
        sobj.setField("ParserFinished__c", Calendar.getInstance());
        sobj.setField("Status__c", "EXCEPTION");
        sobj.setField("Comment__c", exception);

        List<SObject> myObjectsToUpsert = new ArrayList<SObject>();

        myObjectsToUpsert.add(sobj);

        try {
            UpsertResult[] myResultBatch = connection.upsert("ProcessID__c", TransformationHelper.convertToSObjectArray(myObjectsToUpsert));

        } catch (ConnectionException ex) {

            logger.error(" inicialLog  : Error  --  " + ex);

        }

    }

}
