package com.DwqeGroup.tools;

import java.lang.reflect.Array;

public class CRC {

    public static int CRC_Remainder(byte[] bytes) {
        int crc = 0xFFFF, polynomial = 0x1021;

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = (b >> (7 - i) & 1) == 1;
                boolean c15 = (crc >> 15 & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return crc;
    }

    public static byte[] CRC_Bytes(byte[] bytes) {
        int crc = CRC_Remainder(bytes), l = bytes.length;
        byte[] crc_bytes = new byte[l + 2];
        System.arraycopy(bytes, 0, crc_bytes, 0, l);
        crc_bytes[l] = (byte)((crc & 0xFF00) >> 8);
        crc_bytes[l + 1] = (byte)(crc &0x00FF);
        return crc_bytes;
    }


}
