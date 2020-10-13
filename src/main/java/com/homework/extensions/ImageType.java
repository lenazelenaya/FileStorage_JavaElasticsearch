package com.homework.extensions;

public enum ImageType implements FileType{
    BMP, JPG, PNG, GIF, ICO;

    @Override
    public String getType() {
        return "image";
    }
}
