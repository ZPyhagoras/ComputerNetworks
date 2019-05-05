package com.DwqeGroup.run;

import com.DwqeGroup.tools.CRC;
import com.DwqeGroup.tools.FileBytesList;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class SWReceiver {

    public static void main(String[] args) throws IOException {
        Ini ini = new Ini(new File("./StayWait.ini"));
        int myPort = Integer.parseInt(ini.get("Port", "receiverPort"));
        int hisPort = Integer.parseInt(ini.get("Port", "senderPort"));

        DatagramSocket datagramSocket = new DatagramSocket(myPort);
        DatagramPacket datagramReceivePacket;
        DatagramPacket datagramACKPacket;
        InetAddress inetAddress = InetAddress.getLocalHost();

        ArrayList<byte[]> fileArrayList = new ArrayList<>();

        byte[] lr = new byte[16];
        datagramReceivePacket = new DatagramPacket(lr, lr.length);
        datagramSocket.receive(datagramReceivePacket);
        String lengthString = new String(lr);
        int fileLength = Integer.parseInt(lengthString.trim());
        datagramACKPacket = new DatagramPacket(new byte[]{1}, 1, inetAddress, hisPort);
        datagramSocket.send(datagramACKPacket);
        System.out.println("file length: " + fileLength);
        System.out.println("file length successfully");

        int frame_excepted = 0, i = 0;
        byte[] r = new byte[7];
        do {
            System.out.println("\nFrame receiving...");
            datagramReceivePacket = new DatagramPacket(r, r.length);
            datagramSocket.receive(datagramReceivePacket);
            byte[] rack = new byte[1];
            System.out.println("frame_excepted:" + i);
            if (r[0] == frame_excepted){
                //System.out.println(Arrays.toString(r));
                byte[] receive_data = new byte[6];
                System.arraycopy(r, 1, receive_data, 0, 6);
                int mod = CRC.CRC_Remainder(receive_data);
                if (mod == 0) {
                    rack[0] = 1;
                    i++;
                    frame_excepted ^= 1;
                    fileArrayList.add(new byte[]{receive_data[0], receive_data[1], receive_data[2], receive_data[3]});
                    DatagramPacket ackP = new DatagramPacket(rack, rack.length, inetAddress, hisPort);
                    datagramSocket.send(ackP);
                    System.out.println("receiving correct");
                    System.out.println("ack: " + rack[0]);
                }
                else {
                    System.out.println("frame error");
                }
            }
        } while (i != fileLength);
        FileBytesList.WriteFile("zz.txt", fileArrayList);
    }
}
