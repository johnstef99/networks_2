package ithaki_api;

class Temperature {
  public final int sensor;
  public final String temperature;

  public Temperature(int sensor, String temp) {
    this.sensor = sensor;
    this.temperature = temp;
  }

  @Override
  public String toString() {
    return "Sensor " + Integer.toString(sensor) + ": " + temperature + "°C";
  }
}

public class Packet {
  public final String packetString;
  public final long responseTime;
  public final boolean hasTempInfo;
  public final Temperature temperature;

  /**
   * Packet
   *
   * @param packetString
   * @param responseTime
   */
  public Packet(String packetString, long responseTime) {
    this.packetString = packetString;
    this.responseTime = responseTime;
    String[] splitted = packetString.split(" ");
    if (splitted[3].charAt(0) == 'T') {
      hasTempInfo = true;
      String sensor = splitted[3].substring(1);
      String temp = splitted[6];
      temperature = new Temperature(Integer.valueOf(sensor), temp);
    } else {
      hasTempInfo = false;
      temperature = null;
    }
  }

  @Override
  public String toString() {
    if (hasTempInfo)
      return packetString + " with response time = " + responseTime + "ms | " + temperature.toString();
    else
      return packetString + " with response time = " + responseTime + "ms";
  }

}
