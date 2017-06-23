package com.cloudco.sfdc;

import com.cloudco.sfdc.delegate.IntegracionDelegate;
import com.cloudco.sfdc.helpers.TransformationHelper;
import com.cloudco.sfdc.objects.Integracion;
import com.cloudco.sfdc.objects.LoginInfo;
import com.cloudco.sfdc.services.CommonService;
import com.cloudco.sfdc.services.InputService;
import com.cloudco.sfdc.services.OutputService;
import com.cloudco.sfdc.services.SalesforceService;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;


/**
 *
 * @author Dell
 */
public class Excel2Salesforce {

    private static Logger logger = Logger.getLogger(Excel2Salesforce.class.getName());
    
    public static void main(String[] args) throws ConnectionException, ParseException {
        //BasicConfigurator.configure();
        //PropertyConfigurator.configure(args[0]);
        
        IntegracionDelegate myDelegate = new IntegracionDelegate();
        String archivoConfig = CommonService.FILE_NAME;
        if(args.length==1){
            archivoConfig = args[0];
        }
        Integracion myIntegracion = myDelegate.getIntegracion(archivoConfig);
        //Empezamos a escribir en el Log
        String processID = System.currentTimeMillis() + "-" + myIntegracion.getPrefijoArchivo();
        logger.info("processId - " + processID);
        //Verificamos que el usuario se pueda conectar a SFDC
        if(myDelegate.isValidUser(myIntegracion)){
            SalesforceService.iniciaLog(processID);
            Date date;
            if(args.length < 2){
                Calendar now = Calendar.getInstance();
                date = now.getTime();
            }else{
                SimpleDateFormat parser = new SimpleDateFormat("yyMMdd");
                date = parser.parse(args[1]);
            }
            
            try {
                logger.info("Usuario firmado correctamente: Session Id - " + LoginInfo.SESSIONID);
                //Obtenemos el Archivo 
                InputService myInputService = new InputService();
                String rutaArchivo = myIntegracion.getRutaPendientes()+ TransformationHelper.getFileName(myIntegracion.getPrefijoArchivo(), date);
                logger.info("Obteniendo archivo  " + rutaArchivo);
                SalesforceService.startParseLog(processID, rutaArchivo);
                
                //FileInputStream excelFile = new FileInputStream(new File(rutaArchivo));
//                ExcelHeader e = new ExcelHeader();
//                List<String> lHeaders=null; 
//                lHeaders=e.getHeaders(excelFile);
//                for(int i=0;i<lHeaders.size();i++){
//                  System.out.println(lHeaders.get(i));
//                }
                
                logger.debug("Processando Batch del datos de entrada...");
                //contador del lineas del archivo de entrada                
                int inputFileRowNumber = 0;
                if(myIntegracion.getInputHasHeaders()){
                    inputFileRowNumber = 3;
                }
                List<SObject> myObjsParsed = new ArrayList<SObject>();
                int contadorErrores = 0;
                int contadorSuccess = 0;
                int contadorObjParsed = 0;
                int contadorObjToUpsert = 0;
                
                FileInputStream excelFile;
                do{
                    excelFile = new FileInputStream(new File(rutaArchivo));
                    myObjsParsed = myInputService.getDataAsSObject(myIntegracion, excelFile,inputFileRowNumber,myIntegracion.getBatchSize());
                    excelFile.close();
                    inputFileRowNumber = inputFileRowNumber + myObjsParsed.size();
                    System.out.println("input file row number: "+ inputFileRowNumber);
                    logger.debug("Tamaño de Batch (Obj Parsed): " + myObjsParsed.size());
                    contadorObjParsed = contadorObjParsed + myObjsParsed.size();
                    contadorObjToUpsert = contadorObjToUpsert + myObjsParsed.size();
                    
                    UpsertResult[] myResult = SalesforceService.upsert(myObjsParsed,myIntegracion.getExternalId(),myIntegracion.getBatchSize());
                    for(int i=0;i<myResult.length;i++){

                        if (!myResult[i].isSuccess()) {

                            com.sforce.soap.partner.Error[] errors = myResult[i].getErrors();
                            for (com.sforce.soap.partner.Error error : errors) {                            
                                logger.error("External ID : "+ myObjsParsed.get(i).getField("ExternalID__c") + " | Nombre : " + myObjsParsed.get(i).getField("Name") + " | Error:" + error.getMessage()+" Fila: " + (i+1));

                                contadorErrores++;
                            }

                        } else {
                            logger.info("Upsert Id : " + myResult[i].getId() + " IsSuccess : " + myResult[i].isSuccess() + " Created : " + myResult[i].getCreated()+" Fila: "+ (i+1));
                            //accountsSuccess.add("Empresa: " + input[i].getField("Name") + "| Id: " + upsertResult.getId());
                            contadorSuccess++;
                        }

                    }
                    System.out.println("myObjsParsed.isEmpty(): "+myObjsParsed.isEmpty());
                }while (!myObjsParsed.isEmpty());
                SalesforceService.endLog(processID,contadorObjParsed, contadorObjToUpsert, contadorSuccess, contadorErrores);
                //renombrar archivo
                String nuevoNombre = myIntegracion.getRutaProcesados()+ TransformationHelper.getNewName(myIntegracion.getPrefijoArchivo(), date);
                OutputService.renombrarExcel(rutaArchivo,nuevoNombre);
                
                System.out.println("renombra Archivo");
                
            } catch (Exception ex) {
                
                SalesforceService.exceptionLog(processID, ex.getMessage());
                logger.error("Exception: " + ex.getMessage());
                
            }
            
        }else{
            logger.error("El usuario y password en el archivo de configuración seleccionado no son válidos. archivo - " + archivoConfig);
        }
    }
}
