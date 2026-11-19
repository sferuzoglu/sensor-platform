import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

/**
 * Simulates sensors that send measurements over UDP as defined in docs/protocol.md.
 *
 * Written independently from sensor-service on purpose: like a real device, it shares
 * nothing with the server except the ICD. A misunderstanding of the format in one side
 * therefore cannot hide the same misunderstanding in the other.
 *
 * Run (Java 21, no build step):
 *   java simulator/SensorSimulator.java
 *   java simulator/SensorSimulator.java --sensors 1,2,3 --interval-ms 500 --drop 0.1
 *   java simulator/SensorSimulator.java --self-test
 *
 * Options:
 *   --host <name>          target host                      (default localhost)
 *   --port <n>             target UDP port                  (default 9876)
 *   --sensors <ids>        comma separated sensor ids       (default 1,2,3)
 *   --interval-ms <n>      time between rounds              (default 1000)
 *   --count <n>            rounds to send, 0 = forever      (default 0)
 *   --drop <p>             probability a packet is lost     (default 0)
 *   --duplicate <p>        probability a packet is sent twice
 *   --reorder <p>          probability a packet is held back and sent after the next one
 *   --corrupt <p>          probability the magic is broken
 *   --restart-after <n>    a sensor restarts (sequence back to 0) every n packets, 0 = never
 *   --self-test            check the encoder against the ICD example packet and exit
 */
public class SensorSimulator {

    // ---- Protocol constants (docs/protocol.md) ----
    static final int MAGIC = 0x5346;
    static final int VERSION = 1;
    static final int TYPE_MEASUREMENT = 1;
    static final int PAYLOAD_SIZE = 16;
    static final int PACKET_SIZE = 31;

    // ICD section 3 example: sensor 42, sequence 7, 2026-01-01T00:00:00Z, value 21.5
    static final String ICD_EXAMPLE_HEX =
            "534601000100100000002a00000007" + "0000019b76daa800" + "4035800000000000";

    /** Encodes one measurement packet exactly as described in the ICD. */
    static byte[] encode(long sensorId, long sequence, Instant timestamp, double value) {
        ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);   // big-endian by default
        buffer.putShort((short) MAGIC);                         // uint16
        buffer.put((byte) VERSION);                             // uint8
        buffer.putShort((short) TYPE_MEASUREMENT);              // uint16
        buffer.putShort((short) PAYLOAD_SIZE);                  // uint16
        buffer.putInt((int) sensorId);                          // uint32
        buffer.putInt((int) sequence);                          // uint32
        buffer.putLong(timestamp.toEpochMilli());               // int64
        buffer.putDouble(value);                                // float64
        return buffer.array();
    }

    /** State of one simulated sensor. */
    static final class Sensor {
        final long id;
        long sequence;
        long sentSinceRestart;
        double value;
        byte[] heldBack;          // packet delayed to simulate reordering

        Sensor(long id, double startValue) {
            this.id = id;
            this.value = startValue;
        }
    }

    public static void main(String[] args) throws Exception {
        Options o = Options.parse(args);

        if (o.selfTest) {
            System.exit(selfTest() ? 0 : 1);
        }

        Random random = new Random();
        List<Sensor> sensors = new ArrayList<>();
        for (long id : o.sensorIds) {
            sensors.add(new Sensor(id, 15 + random.nextDouble() * 10));   // start between 15 and 25
        }

        InetAddress address = InetAddress.getByName(o.host);
        System.out.printf("Sending to %s:%d, sensors %s, every %d ms%n",
                o.host, o.port, Arrays.toString(o.sensorIds), o.intervalMs);

        try (DatagramSocket socket = new DatagramSocket()) {
            for (long round = 1; o.count == 0 || round <= o.count; round++) {
                for (Sensor s : sensors) {
                    if (o.restartAfter > 0 && s.sentSinceRestart >= o.restartAfter) {
                        s.sequence = 0;
                        s.sentSinceRestart = 0;
                        System.out.printf("sensor %d RESTARTED (sequence back to 0)%n", s.id);
                    }

                    s.sequence++;
                    s.sentSinceRestart++;
                    s.value += (random.nextDouble() - 0.5);   // random walk, +-0.5 per step

                    byte[] packet = encode(s.id, s.sequence, Instant.now(), s.value);
                    String line = String.format("sensor %d seq %d value %.2f", s.id, s.sequence, s.value);

                    if (random.nextDouble() < o.corrupt) {
                        packet[0] = 0x12;
                        line += "  [CORRUPTED]";
                    }

                    if (random.nextDouble() < o.drop) {
                        System.out.println(line + "  [DROPPED]");
                        continue;
                    }

                    if (s.heldBack == null && random.nextDouble() < o.reorder) {
                        s.heldBack = packet;
                        System.out.println(line + "  [HELD BACK]");
                        continue;
                    }

                    send(socket, address, o.port, packet);
                    if (random.nextDouble() < o.duplicate) {
                        send(socket, address, o.port, packet);
                        line += "  [DUPLICATED]";
                    }
                    System.out.println(line);

                    if (s.heldBack != null) {
                        send(socket, address, o.port, s.heldBack);
                        s.heldBack = null;
                        System.out.printf("sensor %d held-back packet sent after newer one  [REORDERED]%n", s.id);
                    }
                }
                Thread.sleep(o.intervalMs);
            }
        }
    }

    static void send(DatagramSocket socket, InetAddress address, int port, byte[] packet) throws Exception {
        socket.send(new DatagramPacket(packet, packet.length, address, port));
    }

    /** Golden-vector check: the encoder must produce the ICD example byte for byte. */
    static boolean selfTest() {
        byte[] actual = encode(42, 7, Instant.parse("2026-01-01T00:00:00Z"), 21.5);
        String actualHex = HexFormat.of().formatHex(actual);
        boolean ok = actualHex.equals(ICD_EXAMPLE_HEX);
        System.out.println("expected " + ICD_EXAMPLE_HEX);
        System.out.println("actual   " + actualHex);
        System.out.println(ok ? "SELF-TEST PASSED" : "SELF-TEST FAILED");
        return ok;
    }

    /** Minimal "--key value" argument parsing. */
    static final class Options {
        String host = "localhost";
        int port = 9876;
        long[] sensorIds = {1, 2, 3};
        long intervalMs = 1000;
        long count = 0;
        double drop, duplicate, reorder, corrupt;
        long restartAfter = 0;
        boolean selfTest;

        static Options parse(String[] args) {
            Options o = new Options();
            for (int i = 0; i < args.length; i++) {
                String key = args[i];
                if (key.equals("--self-test")) {
                    o.selfTest = true;
                    continue;
                }
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for " + key);
                }
                String value = args[++i];
                switch (key) {
                    case "--host" -> o.host = value;
                    case "--port" -> o.port = Integer.parseInt(value);
                    case "--sensors" -> o.sensorIds = Arrays.stream(value.split(","))
                            .map(String::trim).mapToLong(Long::parseLong).toArray();
                    case "--interval-ms" -> o.intervalMs = Long.parseLong(value);
                    case "--count" -> o.count = Long.parseLong(value);
                    case "--drop" -> o.drop = Double.parseDouble(value);
                    case "--duplicate" -> o.duplicate = Double.parseDouble(value);
                    case "--reorder" -> o.reorder = Double.parseDouble(value);
                    case "--corrupt" -> o.corrupt = Double.parseDouble(value);
                    case "--restart-after" -> o.restartAfter = Long.parseLong(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + key);
                }
            }
            return o;
        }
    }
}
