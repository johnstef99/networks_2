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
  private byte[] sound_code;
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
  public ITHAKI(int server_port, int client_port, int echo_code, int image_code, int sound_code) {
    this.server_port = server_port;
    this.client_port = client_port;
    this.echo_code = new String("E" + Integer.toString(echo_code)).getBytes();
    // UDP=1024 to recieve image faster
    this.image_code = new String("M" + Integer.toString(image_code)).getBytes();
    this.sound_code = new String("A" + Integer.toString(sound_code)).getBytes();
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
      recieveSocket.setSoTimeout(10000);
    } catch (SocketException e) {
      errorPrint("Error creating recieveSocket");
      e.printStackTrace();
    }
  }

  /**
   * @param withDelay Whether to have delay or not between packets
   * @param sensor    pass -1 to not get temperature info or pass 0-7
   * @return {@link Packet}
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

  /**
   *
   * @param camera chouse a camera from {@link CAMERAS} to use
   * @return {@link Image}
   */
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

  /**
   *
   * @param numOfPackets number of packets between 1 and 999
   * @param b            multiplyer
   * @param sound_type   pass 0 to get sound from generator or 1-99 to get a song
   * @param adaptive     pass true for AQ-DPCM and false for DPCM
   * @return {@link Sound}
   */
  public Sound getSound(int numOfPackets, int sound_type, boolean adaptive) {
    long startTime = System.currentTimeMillis();
    String Y = "T";
    String L = "";
    String AQ = "";
    if (adaptive)
      AQ = "AQ";
    if (sound_type >= 1 && sound_type <= 99) {
      Y = "F";
      L = "L";
      if (sound_type <= 9)
        L += "0";
      L += String.valueOf(sound_type);
    }
    byte[] code = new String(new String(sound_code) + L + AQ + Y + String.valueOf(numOfPackets)).getBytes();
    DatagramPacket sendPacket = new DatagramPacket(code, code.length, server_address, server_port);
    try {
      ithakiPrint(new String(code) + " sent");
      sendSocket.send(sendPacket);
    } catch (IOException e) {
      errorPrint("Could not send sound packet");
      e.printStackTrace();
    }
    byte[] buffer = new byte[132];
    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);

    Sound sound = new Sound(adaptive, numOfPackets);
    ithakiPrint("Start getting sound packets");
    for (int p = 0; p < numOfPackets; p++) {
      if (p % 100 == 0) {
        ithakiPrint(String.valueOf(p) + " packets of " + String.valueOf(numOfPackets) + " received");
      }
      try {
        recieveSocket.receive(recievePacket);
        sound.addPakcet(buffer, p);
      } catch (SocketTimeoutException e) {
        errorPrint("Timeout on packet num: #" + Integer.toString(p));
      } catch (IOException e) {
        errorPrint("Could not get sound packet");
        e.printStackTrace();
        System.exit(1);
      }
    }
    long elapsedTime = System.currentTimeMillis() - startTime;
    sound.responseTime = elapsedTime;
    return sound;
  }

  // beautify
  static void errorPrint(String error) {
    System.out.println("ITHAKI: -_- " + error + " -_-");
  }

  static void ithakiPrint(String str) {
    System.out.println("ITHAKI: " + str);
  }

}