package ithaki_api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Sound {
  public long responseTime;
  public final boolean isAdaptive;
  public final int numOfPackets;
  private byte[] sound_bytes;

  /**
   *
   * @param isAdaptive
   * @param numOfPackets
   */
  public Sound(boolean isAdaptive, int numOfPackets) {
    this.isAdaptive = isAdaptive;
    this.numOfPackets = numOfPackets;
    sound_bytes = new byte[256 * numOfPackets * (isAdaptive ? 2 : 1)];
  }

  /**
   *
   * @param buffer array of bytes recieved
   * @param p      number of packet
   */
  public void addPakcet(byte[] buffer, int p) {
    int m = 0;
    int b = 2;
    int leftCompressedByte = 0;
    int rightCompressedByte = 0;
    int leftByte = 0;
    int rightByte = 0;
    int start = 0;
    if (isAdaptive) {
      byte[] m_bytes = new byte[4];
      byte sign = (byte) ((buffer[1] & 0x80) != 0 ? 0xff : 0x00);
      m_bytes[3] = sign;
      m_bytes[2] = sign;
      m_bytes[1] = buffer[1];
      m_bytes[0] = buffer[0];
      m = ByteBuffer.wrap(m_bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

      sign = (byte) ((buffer[3] & 0x80) != 0 ? 0xff : 0x00);
      byte[] b_bytes = new byte[4];
      b_bytes[3] = sign;
      b_bytes[2] = sign;
      b_bytes[1] = buffer[3];
      b_bytes[0] = buffer[2];
      b = ByteBuffer.wrap(b_bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

      start = 4;
      int ll;
      int rr;
      int song_pos;
      int prev = 0;
      for (int i = 0; i <= 127; i++) {
        int pair = (int) buffer[i + start];
        song_pos = 2 * (i * 2 + p * 256);

        leftCompressedByte = (pair >>> 4) & 15;
        rightCompressedByte = pair & 15;
        leftByte = (leftCompressedByte - 8) * b;
        rightByte = (rightCompressedByte - 8) * b;

        ll = prev + leftByte + m;
        prev = rightByte;
        rr = leftByte + rightByte + m;

        sound_bytes[song_pos + 0] = (byte) (ll & 0x000000FF);
        sound_bytes[song_pos + 1] = (byte) ((ll & 0x0000FF00) >> 8);
        sound_bytes[song_pos + 2] = (byte) (rr & 0x000000FF);
        sound_bytes[song_pos + 3] = (byte) ((rr & 0x0000FF00) >> 8);
      }

    } else {

      for (int i = 0; i <= 127; i++) {
        int pair = (int) buffer[i + start];
        leftCompressedByte = (pair >>> 4) & 15;
        rightCompressedByte = pair & 15;
        leftByte = (leftCompressedByte - 8) * b;
        rightByte = (rightCompressedByte - 8) * b;
        int song_pos = i * 2 + p * 256;
        if (i == 0)
          sound_bytes[i] = (byte) leftByte;
        else
          sound_bytes[song_pos] = (byte) (leftByte + (int) sound_bytes[song_pos - 1]);
        sound_bytes[song_pos + 1] = (byte) (rightByte + (int) sound_bytes[song_pos]);
      }

    }
  }

  public void play() {
    AudioFormat af = new AudioFormat(8000, (isAdaptive ? 16 : 8), 1, true, false);
    try {
      SourceDataLine player = AudioSystem.getSourceDataLine(af);
      ITHAKI.ithakiPrint("Start playing sound");
      try {
        player.open(af, 32000);
        player.start();
        player.write(sound_bytes, 0, sound_bytes.length);
        player.stop();
        player.close();
        ITHAKI.ithakiPrint("Stop playing sound");
      } catch (LineUnavailableException e) {
        ITHAKI.errorPrint("Could not open player");
        e.printStackTrace();
      }
    } catch (LineUnavailableException e) {
      ITHAKI.errorPrint("Could not create player");
      e.printStackTrace();
    }
  }

  /**
   * Writes imageBytes to file
   *
   * @param filename The name of the file to write the image (eg. image1.jpg)
   */
  public void writeToFile(String filename) {
    File file = new File(filename);
    try {
      FileWriter fw = new FileWriter(file, false);
      fw.write("");
      for (byte b : sound_bytes) {
        fw.append(String.valueOf(b) + "\n");
      }
      fw.close();
      ITHAKI.ithakiPrint("Sound exported to " + filename);
    } catch (IOException e) {
      ITHAKI.errorPrint("Could not write to file: " + filename);
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return "Sound bytes length= " + String.valueOf(sound_bytes.length) + ", responseTime= "
        + String.valueOf(responseTime);
  }

}
