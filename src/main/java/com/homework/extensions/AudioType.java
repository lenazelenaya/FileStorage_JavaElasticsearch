package com.homework.extensions;

public enum AudioType implements FileType{
    MP3, WMA, AAC, WAV, FLAC;

    @Override
    public String getType() {
        return "audio";
    }
}
