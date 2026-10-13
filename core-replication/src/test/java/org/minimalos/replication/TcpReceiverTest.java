package org.minimalos.replication;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class TcpReceiverTest {
    private static final MediaRepository NO_MEDIA = new MediaRepository() {
        @Override public List<MediaRecord> list() { throw new AssertionError("La sonde ne lit pas de galerie"); }
        @Override public InputStream open(MediaRecord record) { throw new AssertionError(); }
        @Override public PendingWrite begin(MediaRecord record) { throw new AssertionError(); }
    };

    @Test(timeout = 10000) public void receiverSurvivesBadCodeThenAcceptsAValidProbe() throws Exception {
        byte[] key = LanSync.parseKey(LanSync.newKey());
        AtomicInteger failures = new AtomicInteger();
        ArrayBlockingQueue<Integer> ports = new ArrayBlockingQueue<>(1);
        TcpReceiver receiver = new TcpReceiver(InetAddress.getLoopbackAddress(), 0);
        FutureTask<Void> task = run(receiver, new TcpReceiver.Listener() {
            @Override public void listening(int port) { ports.add(port); }
            @Override public void session(Socket socket) throws IOException { LanSync.server(socket, key, NO_MEDIA); }
            @Override public void sessionFailed(IOException failure) { failures.incrementAndGet(); }
        });
        try {
            Integer port = ports.poll(2, TimeUnit.SECONDS);
            assertNotNull("Le récepteur doit ouvrir son port", port);
            try {
                probe(port, LanSync.parseKey(LanSync.newKey()));
                fail("Mauvais code accepté");
            } catch (LanSync.AuthenticationException expected) { }
            probe(port, key);
            assertEquals(1, failures.get());
        } finally { receiver.close(); }
        task.get(2, TimeUnit.SECONDS);
    }

    @Test(timeout = 10000) public void closingInterruptsAcceptedSocketAndAllowsRebinding() throws Exception {
        ArrayBlockingQueue<Integer> ports = new ArrayBlockingQueue<>(1);
        CountDownLatch accepted = new CountDownLatch(1);
        TcpReceiver receiver = new TcpReceiver(InetAddress.getLoopbackAddress(), 0);
        FutureTask<Void> task = run(receiver, new TcpReceiver.Listener() {
            @Override public void listening(int port) { ports.add(port); }
            @Override public void session(Socket socket) throws IOException {
                accepted.countDown();
                socket.getInputStream().read(); // No bytes: close() must unblock it.
            }
            @Override public void sessionFailed(IOException failure) { fail("Arrêt volontaire signalé comme échec"); }
        });
        Integer port = ports.poll(2, TimeUnit.SECONDS);
        assertNotNull(port);
        try (Socket client = new Socket("127.0.0.1", port)) {
            assertTrue(accepted.await(2, TimeUnit.SECONDS));
            receiver.close();
            task.get(2, TimeUnit.SECONDS);
            client.setSoTimeout(2000);
            assertEquals(-1, client.getInputStream().read());
        } finally { receiver.close(); }
        TcpReceiver replacement = new TcpReceiver(InetAddress.getLoopbackAddress(), port);
        FutureTask<Void> next = run(replacement, new TcpReceiver.Listener() {
            @Override public void listening(int openedPort) { ports.add(openedPort); }
            @Override public void session(Socket socket) { }
            @Override public void sessionFailed(IOException failure) { }
        });
        try { assertEquals(port, ports.poll(2, TimeUnit.SECONDS)); }
        finally { replacement.close(); }
        next.get(2, TimeUnit.SECONDS); // Also verifies close interrupts a blocked accept().
    }

    @Test(timeout = 5000) public void closedBeforeStartNeverOpensAPort() throws Exception {
        TcpReceiver receiver = new TcpReceiver(InetAddress.getLoopbackAddress(), 0);
        receiver.close();
        run(receiver, new TcpReceiver.Listener() {
            @Override public void listening(int port) { fail("Ancien récepteur rouvert après arrêt"); }
            @Override public void session(Socket socket) { fail(); }
            @Override public void sessionFailed(IOException failure) { fail(); }
        }).get(2, TimeUnit.SECONDS);
    }

    private static FutureTask<Void> run(TcpReceiver receiver, TcpReceiver.Listener listener) {
        FutureTask<Void> task = new FutureTask<>(() -> { receiver.serve(listener); return null; });
        Thread worker = new Thread(task);
        worker.setDaemon(true);
        worker.start();
        return task;
    }
    private static void probe(int port, byte[] key) throws IOException {
        try (Socket client = new Socket("127.0.0.1", port)) {
            client.setSoTimeout(2000);
            LanSync.probeClient(client.getInputStream(), client.getOutputStream(), key);
        }
    }
}
