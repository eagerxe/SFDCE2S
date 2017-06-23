/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.delegate;

import com.cloudco.sfdc.objects.Integracion;
import com.cloudco.sfdc.services.OutputService;
import com.cloudco.sfdc.services.SalesforceService;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Dell
 */
public class IntegracionDelegate {
    
    private static Logger logger = Logger.getLogger(IntegracionDelegate.class.getName());
    
    public boolean isValidUser(Integracion myIntegracion){
       
        return SalesforceService.isValidUser(myIntegracion);

    }
    
    public Integracion getIntegracion(String path){
        logger.debug("In: getIntegracion()");
        try {
            //obtenemos nuestro Objeto Integracion
            OutputService myOutputService = new OutputService();
            Integracion myIntegracion = myOutputService.getIntegracion(path);

            logger.info("Out : getIntegracion() - " + myIntegracion.toString());
            return myIntegracion;


        } catch (IOException ex) {
           
            logger.error("Out: getIntegracion()", ex);
            return null;
        }
    }
}
