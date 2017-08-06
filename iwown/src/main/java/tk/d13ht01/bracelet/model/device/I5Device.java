/*
 * Copyright (c) 2017 PT Studio. All rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet.model.device;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import tk.d13ht01.bracelet.service.BleService;
import tk.d13ht01.bracelet.utils.CommunicationUtils;

/**
 * Created by ptthanh on 7/30/2017.
 */

public class I5Device extends GenericDevice {
    public I5Device(BleService bleService) {
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
