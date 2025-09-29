package com.p2ppayment.network.authprocess;

/**
 * Base interface for all authentication methods that mey come
 * It is advised to maintain a different class for Client Side and Server Side
 */
public interface IauthProcess {
    boolean authenticate();
}