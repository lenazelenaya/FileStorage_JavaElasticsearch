package com.homework.exceptions;

import java.io.FileNotFoundException;
import java.util.function.Supplier;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String s) {
        super(s);
    }
}
