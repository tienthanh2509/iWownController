package ru.wilix.device.geekbracelet.device;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.wilix.device.geekbracelet.service.BLEService;
import ru.wilix.device.geekbracelet.utils.CommunicationUtils;

/**
 * Created by ptthanh on 7/30/2017.
 */

public class I5Device extends GenericDevice {
    public I5Device(BLEService bleService) {
        super(bleService);
    }

    /**
     * Set date and time to device internal clock
     */
    @Override
    public void setDate() {
        GregorianCalendar date = new GregorianCalendar();
        ArrayList<Byte> data = new ArrayList<>();

        data.add(((byte) (date.get(Calendar.YEAR) - 2000)));
        data.add(((byte) (date.get(Calendar.MONTH))));
        data.add(((byte) (date.get(Calendar.DAY_OF_MONTH) - 1)));
        data.add(((byte) date.get(Calendar.HOUR_OF_DAY)));
        data.add(((byte) date.get(Calendar.MINUTE)));
        data.add(((byte) date.get(Calendar.SECOND)));

        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 0), data));
    }
}
