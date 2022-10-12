package com.example.modernjavainaction.day1;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;

class Files {
    void method_ref_before() {
        File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isHidden();
            }
        });
    }

    void method_ref_after() {
        File[] hiddenFiles = new File(".").listFiles(File::isHidden);
    }
}
