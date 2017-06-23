/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.services;

import com.cloudco.sfdc.objects.AttributeMapping;
import com.cloudco.sfdc.objects.EnviromentTypes;
import com.cloudco.sfdc.objects.Integracion;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Dell
 */
public class OutputService {
    public Integracion getIntegracion(String path) throws FileNotFoundException, IOException{
        File f;
       
        f = new File(path);

        Integracion myIntegracion;

        if (f.exists()) {

            //Ya existe solo lo obtenemos
            XStream xstream = new XStream(new DomDriver());

            myIntegracion = (Integracion)xstream.fromXML(readFile(path));
            
            //Desencriptamos nuestro password antes de guardar nuestra integración
            //PasswordService myPasswordService = new PasswordService();
            //myIntegracion.setPassword(myPasswordService.decrypt(myIntegracion.getPassword()));
            myIntegracion.setPassword(myIntegracion.getPassword());            
            return myIntegracion;
           

        }else{
            //Serealizamos nuestro objeto
            return createXmlStructure(path);
        }
        
    }


    /***
     * Metodo que crea el XML con la estructura inicial
     * @param filename
     * @return Integracion Vacio
     * @throws FileNotFoundException
     */
    private Integracion createXmlStructure(String filename) throws FileNotFoundException {
        
        Integracion myIntegracion = new Integracion();
        
        myIntegracion.setUsername("");
        myIntegracion.setPassword("");
        myIntegracion.setToken("");
        myIntegracion.setObjectName("");
        
        myIntegracion.setEmail("");
        myIntegracion.setEnviromentType(EnviromentTypes.SANDBOX);      

        
        AttributeMapping myAttr = new AttributeMapping();
        myAttr.setSourceType("");
        myAttr.setSourceColumn("");
        myAttr.setDestinationColumn("");
        myAttr.setDestinationType("");
        
        myIntegracion.setMapping(myAttr);

        XStream xstream = new XStream();
        xstream.toXML(myIntegracion, new FileOutputStream(filename));

        return myIntegracion;

    }


    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader (file));
        String line  = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }
        return stringBuilder.toString();
    }

    public Integracion saveIntegracion(Integracion myIntegracion) throws FileNotFoundException, IOException {

        XStream xstream = new XStream(new DomDriver());

        //Encriptamos nuestro password antes de guardar nuestra integración
        //PasswordService myPasswordService = new PasswordService();
        //myIntegracion.setPassword(myPasswordService.encrypt(myIntegracion.getPassword()));
        myIntegracion.setPassword(myIntegracion.getPassword());

        xstream.toXML(myIntegracion, new FileOutputStream(CommonService.FILE_NAME));
        
        myIntegracion = (Integracion)xstream.fromXML(readFile(CommonService.FILE_NAME));

        //Desencriptamos nuestro password para tener el valor correcto en Memoria
        //myIntegracion.setPassword(myPasswordService.decrypt(myIntegracion.getPassword()));
        myIntegracion.setPassword(myIntegracion.getPassword());
        return myIntegracion;

    }
    
    public static void renombrarExcel (String nombreOrigen,String nuevoNombre){
        try{
            String ruta = System.getProperty("user.dir") + "\\"+nombreOrigen;
            String ruta2 = System.getProperty("user.dir") + "\\"+nuevoNombre;
            System.out.println("Ruta1: "+ruta);
            System.out.println("Ruta2: "+ ruta2);
            File archivo=new File(ruta);
            archivo.renameTo(new File(ruta2));
        }catch (Exception e) {
            
        }
    }
    
}
