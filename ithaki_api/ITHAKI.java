package ithaki_api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ITHAKI {
  private int server_port;
  private int client_port;
  private InetAddress server_address;
  private byte[] echo_code;
  private byte[] image_code;
  private DatagramSocket sendSocket;
  private DatagramSocket recieveSocket;

  /**
   * API to communicate with Ithaki
   *
   * @param server_port
   * @param client_port
   * @param echo_code
   * @param image_code
   */
  public ITHAKI(int server_port, int client_port, int echo_code, int image_code) {
    this.server_port = server_port;
    this.client_port = client_port;
    this.echo_code = new String("E" + Integer.toString(echo_code)).getBytes();
    // UDP=1024 to recieve image faster
    this.image_code = new String("M" + Integer.toString(image_code) + " UDP=1024").getBytes();
    setup();
    ithakiPrint("initialised");
  }

  // Init some global vars
  private void setup() {
    byte[] ithaki_ip = { (byte) 155, (byte) 207, (byte) 18, (byte) 208 };
    // get inetaddress
    try {
      InetAddress ithaki_address = InetAddress.getByAddress(ithaki_ip);
      server_address = ithaki_address;
    } catch (UnknownHostException e) {
      errorPrint("Error geting ithaki's address from the given ip");
      e.printStackTrace();
      System.exit(1);
    }
    // create sendSocket
    try {
      sendSocket = new DatagramSocket();
    } catch (SocketException e) {
      errorPrint("Error creating sendSocket");
      e.printStackTrace();
    }
    // create recieveSocket
    try {
      recieveSocket = new DatagramSocket(client_port);
      recieveSocket.setSoTimeout(3600);
    } catch (SocketException e) {
      errorPrint("Error creating recieveSocket");
      e.printStackTrace();
    }
  }

  /**
   * @param withDelay Whether to have delay or not between packets
   * @param sensor    pass -1 to not get temperature info or pass 0-7
   * @return Packet
   */
  public Packet getPacket(boolean withDelay, int sensor) {
    long startTime = System.currentTimeMillis();
    byte[] code = echo_code;
    if (!withDelay)
      code = new String("E0000").getBytes();
    if (sensor >= 0 && sensor <= 8) {
      code = new String(new String(code) + "T0" + Integer.toString(sensor)).getBytes();
    }
    DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
    try {
      ithakiPrint(new String(code) + " sent");
      sendSocket.send(sendPacket);
    } catch (IOException e) {
      errorPrint("Could not send echo packet");
      e.printStackTrace();
    }
    byte[] buffer = new byte[2048];
    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
    while (true) {
      try {
        recieveSocket.receive(recievePacket);
        break;
      } catch (SocketTimeoutException e) {
        errorPrint("Timeout");
      } catch (IOException e) {
        errorPrint("Could not get echo packet");
        e.printStackTrace();
        System.exit(1);
      }
    }
    long elapsedTime = System.currentTimeMillis() - startTime;
    Packet packet = new Packet(new String(buffer, 0, recievePacket.getLength()), elapsedTime);
    return packet;
  }

  public Image getImage(CAMERAS camera) {
    byte[] code = image_code;
    switch (camera) {
      case FIX:
        break;
      case PTZ:
        code = new String(new String(image_code) + " CAM=PTZ").getBytes();
        break;
      default:
        break;
    }
    ArrayList<Byte> imageBytes = new ArrayList<Byte>();
    long startTime = System.currentTimeMillis();
    DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
    try {
      ithakiPrint(new String(code) + " sent");
      sendSocket.send(sendPacket);
    } catch (IOException e) {
      errorPrint("Could not send image packet");
      e.printStackTrace();
    }
    byte[] buffer = new byte[1024];
    byte ff = Integer.decode("0XFF").byteValue();
    byte d9 = Integer.decode("0XD9").byteValue();
    byte d8 = Integer.decode("0XD8").byteValue();
    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
    boolean image_recieved = false;
    boolean start_writing = false;
    while (true) {
      try {
        recieveSocket.receive(recievePacket);
        for (int i = 0; i < recievePacket.getLength(); i++) {
          if (start_writing)
            imageBytes.add(buffer[i]);
          // start writing bytes only when you find 0XFF0xD8
          if (!start_writing && i != 0 && buffer[i] == d8 && buffer[i - 1] == ff) {
            imageBytes.add(ff);
            imageBytes.add(d8);
            start_writing = true;
          }
          // stop writing bytes only when you find 0XFF0xD9
          if (i == recievePacket.getLength() - 1 && buffer[i] == d9 && buffer[i - 1] == ff) {
            image_recieved = true;
            break;
          }
        }
        if (image_recieved)
          break;
      } catch (SocketTimeoutException e) {
        errorPrint("Timeout");
      } catch (IOException e) {
        errorPrint("Could not get image packet");
        e.printStackTrace();
        System.exit(1);
      }
    }
    ithakiPrint("Image downloaded!");
    long elapsedTime = System.currentTimeMillis() - startTime;
    return new Image(imageBytes, elapsedTime);
  }

  // beautify
  private void errorPrint(String error) {
    System.out.println("ITHAKI: -_- " + error + " -_-");
  }

  static void ithakiPrint(String str) {
    System.out.println("ITHAKI: " + str);
  }

}
