package cn.heshiqian.database.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ReUsableObjectOutputStream{

    private OutputStream out;

    public ReUsableObjectOutputStream(OutputStream out) throws IOException {
        this.out = out;
    }

    public void write(Object o) throws IOException{
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(o);
        oos.flush();
    }
}
