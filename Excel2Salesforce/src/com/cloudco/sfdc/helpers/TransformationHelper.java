/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudco.sfdc.helpers;

import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Dell
 */
public class TransformationHelper {
    public static SObject[] convertToSObjectArray(List<SObject> myList){ 

        SObject[] myResult = new SObject[myList.size()];

        for (int i = 0; i< myList.size(); i++) {
            myResult[i]=myList.get(i);
        }

        return myResult;


    }
    public static UpsertResult[] convertToUpsertResultArray(List<UpsertResult> myList){

        UpsertResult[] myResult = new UpsertResult[myList.size()];

        for (int i = 0; i< myList.size(); i++) {
            myResult[i]=myList.get(i);
        }

        return myResult;


    }
    

    public static String parseToDateTime(String myStringDate){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {
            Date today = df.parse(myStringDate);
            return outputDateFormat.format(today);
        } catch (ParseException ex) {
            
            return null;
        }
    }
    
    public static String parseToDate(String myStringDate, String format){
        if(myStringDate!=null){
            DateFormat df = new SimpleDateFormat(format);
            DateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");

            try {
                Date today = df.parse(myStringDate);
                return outputDateFormat.format(today);
            } catch (ParseException ex) {

                return null;
            }
        }else{
            return null;
        }
    }
    
    public static Date parseToDateTime(String myStringDate,String format){
        
        if(!myStringDate.equals("00/00/0000")){
            DateFormat df = new SimpleDateFormat(format);
            DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            try {
                Date today = df.parse(myStringDate);
                return outputDateFormat.parse(outputDateFormat.format(today));
            } catch (ParseException ex) {

                return null;
            }
        }else{
            return null;
        }
        
    }
    public static String getDate (String format){
        DateFormat df = new SimpleDateFormat(format);

        return df.format(new Date());
    }
    
    public static void waitForInput(){
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));                       
        try {
            in.read();
        } catch (IOException ex) {            
        }
    }
    
    public static String parseStreet(String value){
        if(value == null)
                return null;

        if(value.length() < 50)
        {
                return value.trim();
        }

        String street = value.substring(0,50);
        String colonia = value.substring(50);

        if(colonia.trim().equals(""))
                return street.trim();

        return street.trim() + ", " + colonia.trim();
    }

    public static String parseRFC(String value){
        if(value == null)
                return null;

        if(value.length()<12)
                return value;

        value = value.substring(0,3) +  value.substring(3,9) + "-" + value.substring(9);

        return value.toUpperCase();

    }
    
    public static Boolean getBoolean(String value){
        if(value == null)
                return null;

        value = value.trim().toUpperCase();

        if(value.equals("Y"))
                return new Boolean(true);
        if(value.equals("TRUE"))
                return new Boolean(true);
        if(value.equals("YES"))
                return new Boolean(true);

        if(value.equals("N"))
                return new Boolean(false);
        if(value.equals("FALSE"))
                return new Boolean(false);
        if(value.equals("NO"))
                return new Boolean(false);

        return new Boolean(false);
    }

    public static Integer getInteger(String value){
    
        String val = getString(value);
        
        if(val == null)
            return null;

        if(val.equals(""))
            return null;



        val = val.replaceAll("\"", "");

        try
        {
            return Integer.valueOf(val);
        }
        catch(NumberFormatException e)
        {    
            return null;
        }
    }
    
    public static Double getDouble(String value){
        String val = getString(value);

        if(val == null)
                return null;

        if(val.equals(""))
                return null;

        try
        {
                return Double.valueOf(val);
        }
        catch(NumberFormatException e)
        {             
                return null;
        }
    }
    
    public static String getString(String value){
        if(value == null)
            return null;

        value = value.trim();

        StringBuffer buffer = new StringBuffer();

        value = value.replaceAll("•", "—");
        value = value.replaceAll("\"", "");

        for(int i=0;i<value.length();i++)
        {
                char c = value.charAt(i);

                if(!Character.isISOControl(c))
                        buffer.append(c);
                else
                        System.out.println("Dicarded: " + c);

        }

        value = buffer.toString();



        return value;
    }
    
    
    public static String getFileName(String prefix, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");

        String filename = prefix + formatter.format(date) + ".xlsx";

        return filename;
    }
    
    public static String getNewName(String prefix, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String filename = prefix + formatter.format(date) +"procesado" + ".xlsx";
        return filename;
    }
}
