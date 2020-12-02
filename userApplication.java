import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;

import ithaki_api.CAMERAS;
import ithaki_api.ITHAKI;
import ithaki_api.Image;
import ithaki_api.Packet;

public class userApplication {
  // GLOBAL
  static int SERVER_PORT = 38024;
  static int CLIENT_PORT = 48024;
  static int ECHO_CODE = 4389;
  static int IMG_CODE = 6059;
  static int SOUND_CODE = 101;
  static String resultsDir = "../results/";
  static DatagramSocket SEND_SOCKET;
  static DatagramSocket RECIEVE_SOCKET;
  static InetAddress SERVER_ADDRESS;

  public static void main(String[] args) {
    System.out.println("===========================");
    System.out.println("           ITHAKI          ");
    System.out.println("===========================\n");

    ITHAKI ithaki = new ITHAKI(SERVER_PORT, CLIENT_PORT, ECHO_CODE, IMG_CODE, SOUND_CODE);

    echo(ithaki, 2, true);
    // echo(ithaki, 30, false);
    // images(ithaki);
    // temperatures(ithaki);
    ithaki.getSound();
  }

  /**
   * Get echo packets
   *
   * @param ithaki     Ithaki's API instant
   * @param whoManySec How many seccond to get packets
   * @param withDelay  Whether to have delay or not
   */
  private static void echo(ITHAKI ithaki, int whoManySec, boolean withDelay) {
    String delay = "_NO_DELAY";
    if (withDelay)
      delay = "_DELAY";
    File echo_packets_file = new File(resultsDir + "E" + Integer.toString(ECHO_CODE) + delay + ".txt");
    try {
      if (echo_packets_file.createNewFile()) {
        System.out.println("File created: " + echo_packets_file.getName());
      } else {
        System.out.println(echo_packets_file.getName() + " already exist");
      }
      FileWriter echo_writer = new FileWriter(echo_packets_file, true);
      double startTime = System.currentTimeMillis();
      System.out.println("Progress\tPacket");
      DecimalFormat per = new DecimalFormat("#0.00");
      for (double now = System.currentTimeMillis(); now < startTime + whoManySec * 1000; now = System
          .currentTimeMillis()) {
        Packet aPacket = ithaki.getPacket(withDelay, -1);
        double progress = ((now - startTime) / (whoManySec * 1000)) * 100;
        System.out.println(per.format(progress) + "%\t" + aPacket.toString());
        echo_writer.write(String.valueOf(aPacket.responseTime) + "\n");
      }
      System.out.println("100%\tGetting echo packets finished");
      System.out.println("Exported to file: " + echo_packets_file.getName());
      echo_writer.close();
    } catch (IOException e) {
      System.out.println("Error creating " + echo_packets_file.getName());
      e.printStackTrace();
    }
  }

  private static void images(ITHAKI ithaki) {
    Image e1 = ithaki.getImage(CAMERAS.FIX);
    e1.writeToFile(resultsDir + "E1.jpg");
    Image e2 = ithaki.getImage(CAMERAS.PTZ);
    e2.writeToFile(resultsDir + "E2.jpg");
  }

  private static void temperatures(ITHAKI ithaki) {
    Packet temPacket = ithaki.getPacket(true, 0);
    File temp_file = new File(resultsDir + "E" + Integer.toString(ECHO_CODE) + "_TEMP.txt");
    try {
      if (temp_file.createNewFile()) {
        System.out.println("File created: " + temp_file.getName());
      } else {
        System.out.println(temp_file.getName() + " already exist");
      }
      FileWriter temp_writer = new FileWriter(temp_file, true);
      temp_writer.write(temPacket.toString() + "\n");
      temp_writer.close();
    } catch (IOException e) {
      System.out.println("Error creating " + temp_file.getName());
      e.printStackTrace();
    }
  }
}
