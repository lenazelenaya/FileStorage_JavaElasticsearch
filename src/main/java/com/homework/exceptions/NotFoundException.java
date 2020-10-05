package com.homework.exceptions;

import java.io.FileNotFoundException;

public class NotFoundException extends FileNotFoundException {
    public NotFoundException(){}
    public NotFoundException(String s) {
        super(s);
    }
}
