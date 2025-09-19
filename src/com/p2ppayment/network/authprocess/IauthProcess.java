package com.p2ppayment.network.authprocess;

public interface IauthProcess {
    boolean authenticateAsClient();
    boolean authenticateAsServer();
}