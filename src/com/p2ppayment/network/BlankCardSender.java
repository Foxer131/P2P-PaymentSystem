package com.p2ppayment.network;

import com.p2ppayment.network.authprocess.AuthenticationHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Base class for all other Sender classes that mey come
 */
public abstract class BlankCardSender {
    /**
     * Self-explanatory
     */
    public boolean performAuthentication(BufferedReader in, PrintWriter out, String nomeRemetente, String caminhoChavePrivada) {
        return AuthenticationHandler.handleClientAuthentication(nomeRemetente, caminhoChavePrivada, in, out);
    }
}
