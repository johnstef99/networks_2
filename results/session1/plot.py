import json
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.pyplot import figure


echo_delay = np.loadtxt('./E4831_DELAY.txt')
echo_no_delay = np.loadtxt('./E4831_NO_DELAY.txt')
song1_dpcm_subs = np.loadtxt('./song1_DPCM_Subs')
song1_dpcm_samples = np.loadtxt('./song1_DPCM_Samples')
generator_dpcm_samples = np.loadtxt('generator_DPCM_Samples')
song1_aqdpcm_samples = np.loadtxt('./song1_AQ-DPCM_Samples')
song1_aqdpcm_subs = np.loadtxt('./song1_AQ-DPCM_Subs')
song1_aqdpcm_m = np.loadtxt('./song1_AQ-DPCM_M')
song2_aqdpcm_m = np.loadtxt('./song2_AQ-DPCM_M')
song1_aqdpcm_b = np.loadtxt('./song1_AQ-DPCM_B')
song2_aqdpcm_b = np.loadtxt('./song2_AQ-DPCM_B')


# G1
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g1_data = echo_delay
total_packets = len(g1_data)
plt.title("G1")
plt.xlabel("packet num (total=" + str(total_packets) + ")")
plt.ylabel("response time in ms")
plt.plot(g1_data)
plt.savefig('../../graphs/G1.png')
plt.close()

# G2 throuput
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g2_data = []
ms = 0
number_of_packets = 0
for time in echo_delay:
    ms += time
    number_of_packets += 1
    if(ms >= 8000):
        g2_data.append(number_of_packets/8)
        number_of_packets = 0
        ms = 0
plt.title("G2")
plt.xlabel("")
plt.ylabel("throuput average for 8s")
plt.plot(g2_data)
plt.savefig('../../graphs/G2.png')
plt.close()

# G3
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g3_data = echo_no_delay
total_packets = len(g3_data)
plt.title("G3")
plt.xlabel("packet num (total=" + str(total_packets) + ")")
plt.ylabel("response time in ms")
plt.plot(g3_data)
plt.savefig('../../graphs/G3.png')
plt.close()

# G4 throuput
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g4_data = []
ms = 0
number_of_packets = 0
for time in echo_no_delay:
    ms += time
    number_of_packets += 1
    if(ms >= 8000):
        g4_data.append(number_of_packets/8)
        number_of_packets = 0
        ms = 0
plt.title("G4")
plt.xlabel("")
plt.ylabel("throuput average for 8s")
plt.plot(g4_data)
plt.savefig('../../graphs/G4.png')
plt.close()

# G5
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g5_range = (min(echo_delay), max(echo_delay))
plt.hist(echo_delay, 10, g5_range, histtype='bar', rwidth=0.95)
plt.title("G5")
plt.xlabel("response time in ms")
plt.ylabel("number of packets")
plt.savefig('../../graphs/G5.png')
plt.close()

# G6 throuput histogram
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g6_range = (min(g2_data), max(g2_data))
plt.hist(g2_data, 10, g6_range, histtype='bar', rwidth=0.96)
plt.title("G6")
plt.xlabel("response time in ms")
plt.ylabel("number of packets")
plt.savefig('../../graphs/G6.png')
plt.close()

# G7
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g7_range = (min(echo_no_delay), max(echo_no_delay))
plt.hist(echo_no_delay, 10, g7_range, histtype='bar', rwidth=0.95)
plt.title("G7")
plt.xlabel("response time in ms")
plt.ylabel("number of packets")
plt.savefig('../../graphs/G7.png')
plt.close()

# G8 histogram throuput
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g8_range = (min(g4_data), max(g4_data))
plt.hist(g4_data, 10, g8_range, histtype='bar', rwidth=0.98)
plt.title("G8")
plt.xlabel("response time in ms")
plt.ylabel("number of packets")
plt.savefig('../../graphs/G8.png')
plt.close()

# R1
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
rtt = g1_data
a = 0.9
b = 0.8
c = 4

srtt = [None] * len(rtt)
srtt[0] = (1-a) * rtt[0]
for i in range(1, len(rtt)):
    srtt[i] = a * srtt[i-1] + (1-a) * rtt[i]

sigma = [None] * len(rtt)
sigma[0] = (1-b) * abs(srtt[i]-rtt[i])
for i in range(1, len(rtt)):
    sigma[i] = b*sigma[i-1] + (1-b) * abs(srtt[i]-rtt[i])

rto = [None] * len(rtt)
for i in range(len(rtt)):
    rto[i] = srtt[i] + c * sigma[i]

plt.plot(rtt, label="RTT",)
plt.plot(srtt, label="SRTT")
plt.plot(sigma, label="σ")
plt.plot(rto, label="RTO")
plt.legend()
plt.savefig('../../graphs/R1.png')
plt.close()

# G9
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g9_data = song1_dpcm_samples
plt.title("G9")
plt.plot(g9_data[:2000])
plt.savefig('../../graphs/G9.png')
plt.close()

# G10
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g10_data = generator_dpcm_samples
plt.title("G10")
plt.plot(g10_data[:2000])
plt.savefig('../../graphs/G10.png')
plt.close()

# G11
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g11_data = song1_dpcm_subs
plt.title("G11")
plt.hist(g11_data, 10, (min(g11_data), max(g11_data)), rwidth=0.95)
plt.savefig('../../graphs/G11.png')
plt.close()

# G12
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g12_data = song1_dpcm_samples
plt.title("G12")
plt.hist(g12_data, 10, (min(g12_data), max(g12_data)), rwidth=0.95)
plt.savefig('../../graphs/G12.png')
plt.close()

# G13
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g13_data = song1_aqdpcm_subs
plt.title("G13")
plt.hist(g13_data, 10, (min(g13_data), max(g13_data)), rwidth=0.95)
plt.savefig('../../graphs/G13.png')
plt.close()

# G14
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
g14_data = song1_aqdpcm_samples
plt.title("G14")
plt.hist(g14_data, 10, (min(g14_data), max(g14_data)), rwidth=0.95)
plt.savefig('../../graphs/G14.png')
plt.close()

# G15
figure(num=None, figsize=(12, 6), dpi=150, facecolor='w', edgecolor='k')
g15_data = song1_aqdpcm_m
plt.title("G15")
plt.plot(g15_data)
plt.savefig('../../graphs/G15.png')
plt.close()

# G16
figure(num=None, figsize=(12, 6), dpi=160, facecolor='w', edgecolor='k')
g16_data = song1_aqdpcm_b
plt.title("G16")
plt.plot(g16_data)
plt.savefig('../../graphs/G16.png')
plt.close()

# G17
figure(num=None, figsize=(12, 6), dpi=170, facecolor='w', edgecolor='k')
g17_data = song2_aqdpcm_m
plt.title("G17")
plt.plot(g17_data)
plt.savefig('../../graphs/G17.png')
plt.close()

# G18
figure(num=None, figsize=(12, 6), dpi=180, facecolor='w', edgecolor='k')
g18_data = song1_aqdpcm_b
plt.title("G18")
plt.plot(g18_data)
plt.savefig('../../graphs/G18.png')
plt.close()



# G19-G20
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
with open('./ITHAKICOPTER.json') as f:
    ithaki_copter = json.load(f)
l_motor = [None] * len(ithaki_copter)
r_motor = [None] * len(ithaki_copter)
altitude = [None] * len(ithaki_copter)
temperature = [None] * len(ithaki_copter)
pressure = [None] * len(ithaki_copter)
for i in range(len(ithaki_copter)):
    l_motor[i] = ithaki_copter[i]['LMOTOR']
    r_motor[i] = ithaki_copter[i]['RMOTOR']
    altitude[i] = ithaki_copter[i]['ALTITUDE']
    temperature[i] = ithaki_copter[i]['TEMPERATURE']
    pressure[i] = ithaki_copter[i]['PRESSURE']/1000
plt.title("G19 - G20")
plt.plot(l_motor, label='LMOTOR')
plt.plot(r_motor, label='RMOTOR')
plt.plot(altitude, label='ALTITUDE')
plt.plot(temperature, label='TEMPERATURE')
plt.plot(pressure, label='PRESSURE')
plt.legend()
plt.savefig("../../graphs/G19-G20.png")
plt.close()

# G21
figure(num=None, figsize=(12, 6), dpi=100, facecolor='w', edgecolor='k')
with open('./VV5069.json') as f:
    vehicle = json.load(f)
engine_runtime = [None] * len(vehicle)
intake_air_temp = [None] * len(vehicle)
throttle_position = [None] * len(vehicle)
engine_rpm = [None] * len(vehicle)
vehicle_speed = [None] * len(vehicle)
coolant_temp = [None] * len(vehicle)
for i in range(len(vehicle)):
    engine_runtime[i] = vehicle[i]['engine_run_time']
    intake_air_temp[i] = vehicle[i]['intake_air_temperature']
    throttle_position[i] = vehicle[i]['throttle_position']
    engine_rpm[i] = vehicle[i]['engine_rpm']
    vehicle_speed[i] = vehicle[i]['vehicle_speed']
    coolant_temp[i] = vehicle[i]['coolant_temperature']
plt.title("G21")
plt.plot(engine_runtime, label='engine_runtime')
plt.plot(intake_air_temp, label='intake_air_temp')
plt.plot(throttle_position, label='throttle_position')
plt.plot(engine_rpm, label='engine_rpm')
plt.plot(vehicle_speed, label='vehicle_speed')
plt.plot(coolant_temp, label='coolant_temp')
plt.legend()
plt.savefig("../../graphs/G21.png")
plt.close()
