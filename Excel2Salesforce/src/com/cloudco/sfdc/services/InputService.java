package com.cloudco.sfdc.services;

import com.cloudco.sfdc.objects.Integracion;
import com.sforce.soap.partner.sobject.SObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Dell
 */
public class InputService {

    private static Logger logger = Logger.getLogger(InputService.class.getName());

    public List<String> getHeaderFromFile(String filename) {

        logger.debug("In: getHeaderFromFile() - filename : " + filename);

        File file = new File(getInputFolder() + filename);
        BufferedReader reader = null;
        List<String> result = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            if ((text = reader.readLine()) != null) {
                String[] myHeader = text.split(",");
                List<String> myRow = new ArrayList<String>();
                for (int i = 0; i < myHeader.length; i++) {
                    if (myHeader[i].substring(0, 1).equals("\"")) {
                        myRow.add(myHeader[i].substring(1, myHeader[i].length() - 1));
                    } else {
                        myRow.add(myHeader[i]);
                    }

                }
                result.addAll(myRow);
            }

        } catch (FileNotFoundException ex) {
            logger.error("In: getHeaderFromFile() - FileNotFound ", ex);
        } catch (IOException ex) {
            logger.error("In: getHeaderFromFile() - IOException ", ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                logger.error("In: getHeaderFromFile() - IOException ", ex);
            }
        }

        logger.debug("Out: getHeaderFromFile() - List<String> : " + result);
        return result;

    }

    public List<List<String>> getData(String filename) {

        File file = new File(filename);
        BufferedReader reader = null;
        List<List<String>> myData = new ArrayList<List<String>>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                String[] myHeader = text.split(",");
                List<String> myRow = new ArrayList<String>();
                for (int i = 0; i < myHeader.length; i++) {
                    if (myHeader[i].substring(0, 1).equals("\"")) {

                        myRow.add(myHeader[i].substring(1, myHeader[i].length() - 2));
                    } else {
                        myRow.add(myHeader[i]);
                    }

                }
                myData.add(myRow);
            }

        } catch (FileNotFoundException ex) {
            logger.error("In: getData() - FileNotFoundException ", ex);
        } catch (IOException ex) {
            logger.error("In: getData() - IOException ", ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                logger.error("In: getData() - IOException ", ex);
            }
        }

        logger.debug("Out: getHeaderFromFile() - List<List<String>> : " + myData);
        return myData;

    }

    public File downloadFile(String prefix, String baseUrl) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");

        String filename = prefix + formatter.format(new Date()) + ".TXT";

        URL url = new URL(baseUrl + filename);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        int bytes = conn.getContentLength();

        logger.info("Downloading File(" + url.toString() + ") with " + bytes + " Bytes");
        logger.info("HTTP STATUS: " + conn.getResponseCode());

        InputStream istream = conn.getInputStream();
        File f = new File("ArchivosCarga/" + filename);
        FileOutputStream fstream = new FileOutputStream(f);

        int c = 0;

        while ((c = istream.read()) != -1) {
            fstream.write(c);
        }

        fstream.close();
        istream.close();

        logger.info("Finished Downloading " + url);

        return f;

    }

    public List<SObject> getDataAsSObject(Integracion myIntegracion, FileInputStream excelFile, int startRowCount, int batchSize) throws Exception {

        logger.debug("In: getDataAsSObject() - sobject : " + myIntegracion.getObjectName());
        System.out.println("Entro a getDataAsObject");
        System.out.println("startRowCount: "+ startRowCount);
        System.out.println("batchSize: "+batchSize);
        int contadorLineas = 0;

        List<SObject> myData = new ArrayList<SObject>();

        //String text = null;
        List<String> header = null;
        List<String> myRow = null;
        if (myIntegracion.getInputHasHeaders()) {

            contadorLineas++;
        }
        
        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            System.out.println("Creando workbook");
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            //////////////////////
            Iterator rows = datatypeSheet.rowIterator();
            int maxNumOfCells = datatypeSheet.getRow(0).getLastCellNum();
            System.out.println("max num: " + maxNumOfCells);

            while (rows.hasNext()) {
                contadorLineas++;
                XSSFRow row = (XSSFRow) rows.next();
                Iterator cells = row.cellIterator();
                System.out.println("contador linea: " + contadorLineas);
                List dataRow = null;
                dataRow = new ArrayList();
                for (int cellCounter = 0; cellCounter < maxNumOfCells; cellCounter++) { // Loop through cells

                    if (contadorLineas > startRowCount) {
                        XSSFCell cell;
                        if (row.getCell(cellCounter) == null) {
                            cell = row.createCell(cellCounter);
                            System.out.println("Data cell 1 ");
                        } else {
                            cell = row.getCell(cellCounter);
                            
                            if (null != cell.getCellTypeEnum()) //tipo
                            {
                                switch (cell.getCellTypeEnum()) {
                                    case NUMERIC:
                                        System.out.println("Numerico: "+cell.getNumericCellValue());
                                        if (DateUtil.isCellDateFormatted(cell)) {
                                            System.out.println("Fecha: "+ cell.getDateCellValue());
                                            dataRow.add(cell.getDateCellValue());
                                        } else {
                                            dataRow.add(cell.getNumericCellValue());
                                        }
                                        break;
                                    case STRING:
                                        System.out.println("String: "+cell.getRichStringCellValue());
                                        dataRow.add(cell.getStringCellValue());
                                        break;
                                    case BOOLEAN:
                                        System.out.println("Boleano: "+cell.getBooleanCellValue());
                                        dataRow.add(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        System.out.println("FORMULA: "+cell.getNumericCellValue());
                                        dataRow.add(cell.getNumericCellValue());
                                        break;
                                    case BLANK:
                                        System.out.println("THIS IS BLANK");
                                        dataRow.add("");
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }

                }
                SObject myObj = SalesforceService.createSObjectC(myIntegracion, dataRow);

                if (myObj != null) {
                    myData.add(myObj);
                }
                if (contadorLineas - startRowCount == batchSize) {
                    break;
                }
            }
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        logger.debug("Out: getDataAsSObject() - List<SObject> size(): " + myData.size());
        return myData;
    }

    public String getInputFolder() {

        return getRootFolder() + "ArchivosCarga/";
    }

    public String getRootFolder() {
        //URL myURL = this.getClass().getClassLoader().getResource("dist");
        String path = "";
        try {
            path = InputService.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(InputService.class.getName()).log(Level.SEVERE, null, ex);
        }

        String path1 = path.substring(0, path.lastIndexOf("/"));
        if (path1.lastIndexOf("/") > 3) {
            String path2 = path1.substring(0, path1.lastIndexOf("/") + 1);

            if (path2.substring(path2.lastIndexOf("/") - 5, path2.lastIndexOf("/")).equals("build")) {
                path = path2.substring(0, path2.lastIndexOf("/") - 5);
            } else {
                path = path1 + "/";
            }
        } else {
            path = path1 + "/";
        }
        path = path.replace("%20", " ");

        return path;
    }

    public boolean isFileValid(String filepath) {
        logger.debug("In: isFileValid() - filepath : " + filepath);

        File file = new File(getInputFolder() + filepath);

        logger.debug("Out: isFileValid() - boolean: " + file.exists());
        return file.exists();
    }
}
