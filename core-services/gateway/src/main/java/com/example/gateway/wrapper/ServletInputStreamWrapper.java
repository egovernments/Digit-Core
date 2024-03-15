package com.example.gateway.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;

public class ServletInputStreamWrapper extends ServletInputStream {
    private byte[] data;
    private int idx = 0;

    public ServletInputStreamWrapper(byte[] data) {
        if (data == null) {
            data = new byte[0];
        }

        this.data = data;
    }

    public int read() throws IOException {
        return this.idx == this.data.length ? -1 : this.data[this.idx++] & 255;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }
}

