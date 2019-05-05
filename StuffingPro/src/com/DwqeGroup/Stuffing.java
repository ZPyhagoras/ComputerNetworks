package com.DwqeGroup;

import org.ini4j.Ini;

import java.io.File;

public class Stuffing {

    public static void main(String[] args) throws Exception {
	// write your code here
        Ini ini = new Ini(new File("./Stuffing.ini"));
        byte flagZeroBit = Byte.parseByte(ini.get("ZeroBitStuffing", "FlagString"), 2);
        byte flagByte = (byte) Integer.parseInt(ini.get("ByteStuffing", "FlagString"), 16);
        byte ESC = (byte) Integer.parseInt(ini.get("ByteStuffing", "ESC"), 16);
        byte[] ByteInfoString = { (byte) 0x34, 0x7D, 0x7E, (byte) 0x80, 0x7E, 0x40, (byte) 0xAA, 0x7D};
        byte[] ZeroBitInfoString = {(byte) 0xCF, (byte) 0xDD, (byte) 0x7F, (byte) 0xA9};    //0b11001111110111010111111110101001

        System.out.println("字节填充开始结束标志: " + BytesToHexString(new byte[] {flagByte}));
        System.out.println("字节填充转义字符: " + BytesToHexString(new byte[] {ESC}));
        System.out.println("待字节填充的字符串: " + BytesToHexString(ByteInfoString));
        byte[] byteStuffString = ByteStuffing(ByteInfoString, flagByte, ESC);
        System.out.println("字节填充后字符串: " + BytesToHexString(byteStuffString));
        byte[] UnByteStuffString = UnByteStuffing(byteStuffString, flagByte, ESC);
        System.out.println("去掉字节填充的字符串: " + BytesToHexString(UnByteStuffString));
        System.out.println();

        System.out.println("零比特填充开始结束标志: " + BytesToBinaryString(new byte[] {flagZeroBit}));
        System.out.println("待零比特填充的字符串: " + BytesToBinaryString(ZeroBitInfoString));
        byte[] zeroBitStuffString = ZeroBitStuffing(ZeroBitInfoString, flagZeroBit);
        System.out.println("零比特填充后字符串: " + BytesToBinaryString(zeroBitStuffString));
        byte[] UnZeroBitStuffString = UnZeroBitStuffing(zeroBitStuffString, flagZeroBit);
        System.out.println("去掉零比特填充的字符串：" + BytesToBinaryString(UnZeroBitStuffString));
    }

    private static byte UpdateByte(byte b, int num, int index) {
        index = 7 - index;
        byte checkNum = -1;
        if (num == 1) {
            checkNum = (byte) (num << index);
            return (byte) (b | checkNum);
        }
        else if (num == 0) {
            checkNum = (byte) (checkNum ^ (1 << index));
            return (byte) (b & checkNum);
        }
        return checkNum;
    }

    private static int GetBit(byte b, int index) {
        index = 7 - index;
        int checkNum = 1 << index;
        return (b & checkNum) >> index;
    }

    private static byte[] ZeroBitStuffing(byte[] bytes, byte flag) {
        byte[] ans = new byte[8];
        ans[0] = flag;
        int index = 0, i, j;
        int aindex = 8, ai, aj;
        int bit, cnt = 0;
        do {
            i = index / 8; j = index % 8;
            ai = aindex / 8; aj = aindex % 8;
            bit = GetBit(bytes[i], j);
            ans[ai] = UpdateByte(ans[ai], bit, aj);
            if (bit == 1) {
                cnt++;
                if (cnt == 5) {
                    cnt = 0;
                    aindex++;
                    ai = aindex / 8; aj = aindex % 8;
                    ans[ai] = UpdateByte(ans[ai], 0, aj);
                }
            }
            else {
                cnt = 0;
            }
            //System.out.println("bit: " + bit + ", cnt: "+ cnt +", index: " + index + ", aindex: " + aindex);
            index++;
            aindex++;
        } while (index != 32);
        byte[] tmp = new byte[ai + 2];
        System.arraycopy(ans, 0, tmp, 0, ai + 1);
        for (int k = 0; k < 8; k++) {
            ai  = aindex / 8; aj = aindex % 8;
            bit = GetBit(flag, k);
            tmp[ai] = UpdateByte(tmp[ai], bit, aj);
            aindex++;
        }
        return tmp;
    }

    private static byte[] UnZeroBitStuffing(byte[] bytes, byte flag) {
        byte[] ans = new byte[4];
        int index = 0, i, j;
        int aindex = 0, ai, aj;
        int bit, cnt = 0;
        boolean zeroFlag = false;
        while (true) {
            i = index / 8; j = index % 8;
            bit = GetBit(bytes[i], j);
            if (bit == 0) {
                zeroFlag = true;
                cnt = 0;
            }
            else if (zeroFlag && bit == 1) {
                cnt++;
            }
            if (cnt == 6) {
                index++;
                i = index / 8; j = index % 8;
                if (GetBit(bytes[i], j) == 0) {
                    index++;
                    break;
                }
            }
            index++;
        }
        cnt = 0;
        do {
            i = index / 8; j = index % 8;
            ai = aindex / 8; aj = aindex % 8;
            bit = GetBit(bytes[i], j);
            if (bit == 1) {
                cnt++;
                if (cnt == 5) {
                    index++;
                    cnt = 0;
                }
            }
            else {
                cnt = 0;
            }
            ans[ai] = UpdateByte(ans[ai], bit, aj);
            index++;
            aindex++;
        } while (aindex != 32);
        return ans;
    }

    private static byte[] ByteStuffing(byte[] bytes, byte flag, byte esc) {
        int l = bytes.length ,j = 1;
        byte[] ans = new byte[18];
        ans[0] = 0x7E;
        for (int i = 0; i < l; i++) {
            if (bytes[i] == flag || bytes[i] == esc) {
                ans[i + j] = esc;
                ans[i + j + 1] = bytes[i];
                j += 1;
            }
            else {
                ans[i + j] = bytes[i];
            }
        }
        ans[l + j] = 0x7E;
        byte[] tmp = new byte[l + j + 1];
        System.arraycopy(ans, 0, tmp, 0, l + j + 1);
        return tmp;
    }

    private static byte[] UnByteStuffing(byte[] bytes, byte flag, byte esc) {
        int i = 0, j = 0;
        boolean startFlag = false;
        byte[] ans = new byte[8];
        while (true){
            if (!startFlag) {
                if (bytes[i + j] == esc && bytes[i + j + 1] == flag) {
                    j += 2;
                }
                else {
                    if (bytes[i + j] == flag) {
                        startFlag = true;
                    }
                    j += 1;
                }
            }
            else {
                if (bytes[i + j] == esc) {
                    j += 1;
                    ans[i] = bytes[i + j];
                    i++;
                }
                else {
                    if (bytes[i + j] == flag) {
                        break;
                    }
                    ans[i] = bytes[i + j];
                    i++;
                }
            }
        }
        return ans;
    }

    private static String BytesToHexString(byte[] bs) {
        StringBuilder hexString = new StringBuilder();
        // hexString.append("0x");
        for(byte b : bs) {
            hexString.append(Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase());
        }
        return hexString.toString();
    }

    private static String BytesToBinaryString(byte[] bs) {
        StringBuilder binaryString = new StringBuilder();
        // hexString.append("0b");
        for(byte b : bs) {
            String tmpString = Integer.toBinaryString(Byte.toUnsignedInt(b));
            int l = tmpString.length();
            StringBuilder zeroString = new StringBuilder();
            for (int i = 0; i < 8 - l; i++) {
                zeroString.append("0");
            }
            binaryString.append(zeroString);
            binaryString.append(tmpString);
        }
        return binaryString.toString();
    }
}
