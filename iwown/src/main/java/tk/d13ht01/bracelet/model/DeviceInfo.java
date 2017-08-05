package tk.d13ht01.bracelet.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import tk.d13ht01.bracelet.utils.CommunicationUtils;

/**
 * Created by Dmitry on 30.08.2015.
 */
public class DeviceInfo implements Serializable {
    private String model;
    private int oadmode;
    private String swversion;
    private String bleAddr;
    private int displayWidthFont;

    public static DeviceInfo fromData(byte[] data) {
        DeviceInfo info = new DeviceInfo();
        info.setModel(CommunicationUtils.ascii2String(Arrays.copyOfRange(data, 6, 10)));
        info.setOadmode((data[10] * 255) + data[11]);
        info.setSwversion(data[12] + "." + data[13] + "." + data[14] + "." + data[15]);
        info.setBleAddr(CommunicationUtils.byteArrayToString(Arrays.copyOfRange(data, 16, 22)));
        if (data.length == 29)
            info.setDisplayWidthFont(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 28, 29)));
        else if (data.length == 28)
            info.setDisplayWidthFont(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 27, 28)));
        return info;
    }

    public String toString() {
        String buff = "";
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)
                continue;
            try {
                buff += field.getName() + ": " + field.get(this) + " ";
            } catch (Exception e) {
            }
        }
        return buff;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    public int getOadmode() {
        return this.oadmode;
    }

    public void setOadmode(int value) {
        this.oadmode = value;
    }

    public String getSwversion() {
        return this.swversion;
    }

    public void setSwversion(String value) {
        this.swversion = value;
    }

    public String getBleAddr() {
        return this.bleAddr;
    }

    public void setBleAddr(String value) {
        this.bleAddr = value;
    }

    public int getDisplayWidthFont() {
        return this.displayWidthFont;
    }

    public void setDisplayWidthFont(int value) {
        this.displayWidthFont = value;
    }
}
