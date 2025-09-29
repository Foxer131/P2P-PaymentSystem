package com.p2ppayment.network;

import com.p2ppayment.network.authprocess.AuthenticationHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Base class for all future handler classes. Its only responsibility is to try to authenticate the user sending in a request.
 */
public abstract class BlankCardHandler {
    /**
     * @param in Receives data sent over socket, will be used to receive the handshake request.
     * @param out Used to send data over the socket. Will be used by AuthenticationHandler
     * @param clientSocket The socket itself
     * @return Returns the result of the authentication attempt
     */
    public boolean performAuthentication(BufferedReader in, PrintWriter out, Socket clientSocket) {
        try {
            String handshake = in.readLine();

            if (handshake == null)
                return false;

            return AuthenticationHandler.handleServerAuthentication(handshake, in, out, clientSocket);
        } catch (IOException  e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
}
