

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serialKirimData;

/**
 *
 * @author RifqiTh0kz
 */
import gnu.io.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.JOptionPane;

public class Serial implements SerialPortEventListener {

    /**
     * Deklasrasi objek dari class Monitor
     */
    Monitor window = null;
    private Enumeration port = null;
    private HashMap portMap = new HashMap();
    private CommPortIdentifier portIdentifier = null;
    private SerialPort serialPort = null;
    private InputStream inPut = null;
    private OutputStream outPut = null;
    private boolean serialConnected = false;
    StringBuilder olah = new StringBuilder("");
    final static int TIMEOUT = 2000;
    String dataIn = "";
    String statusPort = "";

    public Serial(Monitor window) {
        this.window = window;
    }

    /**
     * Cek PORT yang tersedia
     */
    public void cekSerialPort() {
        port = CommPortIdentifier.getPortIdentifiers();
        while (port.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) port.nextElement();
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                window.jCommPort.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    final public boolean getConnected() {
        return serialConnected;
    }

    public void setConnected(boolean serialConnected) {
        this.serialConnected = serialConnected;
    }

    public void connect() {
        String selectedPort = (String) window.jCommPort.getSelectedItem();
        System.out.println(selectedPort);
        portIdentifier = (CommPortIdentifier) portMap.get(selectedPort);
        CommPort commPort = null;
        try {
            commPort = portIdentifier.open(null, TIMEOUT);
            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(
                            1200,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
            setConnected(true);
            window.bConn.setText("Disconnect");
        } catch (PortInUseException e) {
            statusPort = selectedPort + " is in use. (" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusPort);
        } catch (Exception e) {
            statusPort = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusPort);
        }
    }

    public void disconnect() {
         try {
            serialPort.removeEventListener();
            serialPort.close();
            inPut.close();
            setConnected(false);
            statusPort = "PORT disconnect successfully";            
            JOptionPane.showMessageDialog(null, statusPort);
            window.bConn.setText("Connect");
        } catch (Exception e) {
            statusPort = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusPort);
        }
    }

    public boolean initIOStream() {
        boolean successful = false;
        try {
            inPut = serialPort.getInputStream();
            outPut = serialPort.getOutputStream();
            
            successful = true;
            return successful;
        } catch (IOException e) {
            statusPort = "I/O Streams failed to open. (" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusPort);
            return successful;
        }
    }

    public void kirimData(Byte a){
        try {
            outPut.write(a);
            outPut.write(10);
            outPut.flush();
        } catch (IOException ex) {
            System.out.println("Kirim Gagal");
        }
    }
    
    public void initListener() {
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }
    
    public static String right(String value, int length) {
	// To get right characters from a string, change the begin index.
	return value.substring(value.length() - length);
    }
    
    public static String left(String value, int length) {
	// To get right characters from a string, change the begin index.
	return value.substring(0,length);
    }
    
    public static String customFormat(String pattern, String text) {
      Double value = Double.parseDouble(text);
      DecimalFormat myFormatter = new DecimalFormat(pattern);
      String output = myFormatter.format(value);
      return output;
      //System.out.println(value + "  " + pattern + "  " + output);
    }

    public void serialEvent(SerialPortEvent evt) {
        char dataSerial = 0; // Untuk menampung input dari serial port 
        String asal = "";
        String hasil = "";
        String hasil2 = "";
        String koma = "";
        String olah2 = "";
        //byte[] singleData = new byte[1000];
        DecimalFormat numFormat;
        
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            //some ascii values for for certain things
        final int SPACE_ASCII = 32;
        final int DASH_ASCII = 45;
        final int NEW_LINE_ASCII = 10;
            try {
                /*
                dataSerial = (char) inPut.read();
                asal = String.valueOf(dataSerial);
                System.out.println(asal);
                */
                //window.txtDataIn.setText(String.valueOf(dataSerial));
                //int availableBytes = inPut.available();
                //if (availableBytes > 0) {
                //    inPut.read(singleData, 0, availableBytes);
                    
                byte singleData = (byte)inPut.read();
                //System.out.println(new String(singleData, 0, availableBytes));
                //asal = new String(singleData, 0, availableBytes);
                
                if ((singleData != 3) && (singleData != 2)){
                    asal = new String(new byte[] {singleData});
                    //System.out.println(asal);
                    if(asal.equals("+")){
                        olah.setLength(0);
                    } else {
                        olah.append(asal);
                    }
                }
                if(!olah.toString().equals(olah2)){
                olah = new StringBuilder(olah.toString().replaceAll("\\s", ""));
                olah = new StringBuilder(olah.toString().replaceAll("\\n", ""));
                
                    if(olah.length()==9){
                    System.out.println(olah);
                    hasil = olah.toString();
                    hasil = hasil.substring(1);
                    koma = right(hasil,4);
                    koma = left(koma,1);
                    hasil = hasil.substring(0,hasil.length()-4);
                    hasil2 = customFormat("###,###.#",hasil+"."+koma);
                        if(!window.txtBerat.getText().equals(hasil2)){
                            window.txtBerat.setText(hasil2);
                        }
                    }
                }
                olah2 = olah.toString();
                
                //}
            } catch (IOException ex) {
                //JOptionPane.showMessageDialog(null, ex.toString());
                System.out.println(ex);
            } catch (Exception ex) {
                //JOptionPane.showMessageDialog(null, ex.toString());
                System.out.println(ex);
            }
        }
    }
}
