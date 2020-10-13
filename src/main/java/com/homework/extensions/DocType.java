package com.homework.extensions;

public enum DocType implements FileType{
    PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT;

    @Override
    public String getType() {
        return "document";
    }
}
