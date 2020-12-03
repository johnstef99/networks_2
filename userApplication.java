import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Scanner;

import ithaki_api.CAMERAS;
import ithaki_api.ITHAKI;
import ithaki_api.Image;
import ithaki_api.IthakiCopterPacket;
import ithaki_api.Packet;
import ithaki_api.Sound;
import ithaki_api.VehiclePacket;

public class userApplication {
  // GLOBAL
  static int SERVER_PORT = 38002;
  static int CLIENT_PORT = 48002;
  static int COPTER_PORT = 48038;
  static int ECHO_CODE = 4491;
  static int IMG_CODE = 6059;
  static int SOUND_CODE = 6153;
  static int VEHICLE_CODE = 2774;
  static String resultsDir = "../results/";
  static DatagramSocket SEND_SOCKET;
  static DatagramSocket RECIEVE_SOCKET;
  static InetAddress SERVER_ADDRESS;

  public static void main(String[] args) {
    System.out.println("===========================");
    System.out.println("           ITHAKI          ");
    System.out.println("===========================\n");

    ITHAKI ithaki = new ITHAKI(SERVER_PORT, CLIENT_PORT, ECHO_CODE, IMG_CODE, SOUND_CODE, VEHICLE_CODE);

    // echo(ithaki, 2, true);
    // echo(ithaki, 30, false);
    // images(ithaki);vac userApplication.java && java userApplication
    // temperatures(ithaki);
    // sound(ithaki);
    // telemetry(ithaki, 10);
    VehiclePacket vehiclePacket = ithaki.getVehicle();
    System.out.println(vehiclePacket.toString());

  }

  private static void telemetry(ITHAKI ithaki, int runTime) {
    File copter_packets_file = new File(resultsDir + "ITHAKICOPTER.txt");
    try {
      if (copter_packets_file.createNewFile()) {
        System.out.println("File created: " + copter_packets_file.getName());
      } else {
        System.out.println(copter_packets_file.getName() + " already exist");
      }
      FileWriter echo_writer = new FileWriter(copter_packets_file, true);
      double startTime = System.currentTimeMillis();
      System.out.println("Progress\tPacket");
      DecimalFormat per = new DecimalFormat("#0.00");
      for (double now = System.currentTimeMillis(); now < startTime + runTime * 1000; now = System
          .currentTimeMillis()) {
        IthakiCopterPacket aPacket = ithaki.getTelemetry();
        double progress = ((now - startTime) / (runTime * 1000)) * 100;
        System.out.println(per.format(progress) + "%\t" + aPacket.toString());
        echo_writer.write(String.valueOf(aPacket.responseTime) + "\n");
      }
      System.out.println("100%\tGetting echo packets finished");
      System.out.println("Exported to file: " + copter_packets_file.getName());
      echo_writer.close();
    } catch (IOException e) {
      System.out.println("Error creating " + copter_packets_file.getName());
      e.printStackTrace();
    }

  }

  void getCodes() {
    File codesFile = new File("./codes");
    try {
      Scanner reader = new Scanner(codesFile);
      int line = 0;
      while (reader.hasNextLine()) {
        switch (line) {
          case 1:
            System.out.println("client port: " + reader.nextLine());
            break;
          case 3:
            System.out.println("server port: " + reader.nextLine());
            break;
          case 4:
            System.out.println("ECHO_CODE: " + reader.nextLine());
            break;
          case 5:
            System.out.println("IMG_CODE: " + reader.nextLine());
            break;
          case 6:
            System.out.println("SOUND_CODE: " + reader.nextLine());
            break;
          case 7:
            System.out.println("COPTER_CODE: " + reader.nextLine());
            break;
          case 8:
            System.out.println("VEHICLE_CODE: " + reader.nextLine());
            break;
          default:
            reader.nextLine();
            break;
        }
        line++;
      }
      reader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void sound(ITHAKI ithaki) {
    Sound aSound = ithaki.getSound(300, 1, true);
    aSound.writeToFile(resultsDir + "song1");
    Sound bSound = ithaki.getSound(300, 1, false);
    bSound.writeToFile(resultsDir + "song1");
    aSound.play();
    bSound.play();
  }

  /**
   * Get echo packets
   *
   * @param ithaki    Ithaki's API instant
   * @param runTime   How many second to get packets
   * @param withDelay Whether to have delay or not
   */
  private static void echo(ITHAKI ithaki, int runTime, boolean withDelay) {
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
      for (double now = System.currentTimeMillis(); now < startTime + runTime * 1000; now = System
          .currentTimeMillis()) {
        Packet aPacket = ithaki.getPacket(withDelay, -1);
        double progress = ((now - startTime) / (runTime * 1000)) * 100;
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
