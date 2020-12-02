package ithaki_api;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Image {
  final ArrayList<Byte> imageBytes;
  final long responseTime;

  /**
   *
   * @param imageBytes
   * @param responseTime
   */
  public Image(ArrayList<Byte> imageBytes, long responseTime) {
    this.imageBytes = imageBytes;
    this.responseTime = responseTime;
  }

  /**
   * Writes imageBytes to file
   *
   * @param filename The name of the file to write the image (eg. image1.jpg)
   */
  public void writeToFile(String filename) {
    FileOutputStream os;
    try {
      os = new FileOutputStream(filename, false);
      for (int i = 0; i < imageBytes.size(); i++) {
        // write the first byte but append the rest, to replace the image
        if (i == 0) {
          os.write(imageBytes.get(i));
          os = new FileOutputStream(filename, true);
        } else
          os.write(imageBytes.get(i));
      }
      os.close();
      ITHAKI.ithakiPrint("Image exported to " + filename);
    } catch (FileNotFoundException e) {
      System.out.println("Could not write to file " + filename);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Could not write to file " + filename);
      e.printStackTrace();
    }
  }

}
