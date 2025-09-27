package com.p2ppayment.fileparsers;

import java.util.List;

public interface FileParser<T> {
    List<T> parse() throws Exception;
}
