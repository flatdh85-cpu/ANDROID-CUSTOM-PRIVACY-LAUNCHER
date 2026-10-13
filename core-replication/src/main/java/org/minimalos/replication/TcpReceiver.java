package org.minimalos.replication;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** One binding of a direct receiver. Closing it also interrupts an accepted session. */
public final class TcpReceiver implements Closeable {
    public interface Listener {
        void listening(int port);
        void session(Socket socket) throws IOException;
        void sessionFailed(IOException failure);
    }
    private final InetAddress address;
    private final int port;
    private boolean closed;
    private ServerSocket server;
    private Socket client;

    public TcpReceiver(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }
    public synchronized boolean isClosed() { return closed; }

    public void serve(Listener listener) throws IOException {
        try (ServerSocket bound = new ServerSocket()) {
            synchronized (this) {
                if (closed) return;
                server = bound;
            }
            bound.setReuseAddress(true);
            bound.bind(new InetSocketAddress(address, port), 4);
            if (isClosed()) return;
            listener.listening(bound.getLocalPort());
            while (!isClosed()) {
                // An accept failure ends this binding; never spin on a closed socket.
                try (Socket accepted = bound.accept()) {
                    synchronized (this) {
                        if (closed) return;
                        client = accepted;
                    }
                    try { listener.session(accepted); }
                    catch (IOException failure) { if (!isClosed()) listener.sessionFailed(failure); }
                    finally { synchronized (this) { client = null; } }
                }
            }
        } catch (IOException failure) {
            if (!isClosed()) throw failure;
        } finally { close(); }
    }

    @Override public synchronized void close() {
        closed = true;
        try { if (client != null) client.close(); } catch (IOException ignored) { }
        try { if (server != null) server.close(); } catch (IOException ignored) { }
    }
}
