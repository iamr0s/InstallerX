package com.rosan.installer.data.app.model.impl.privileged;

import android.os.RemoteException;

import androidx.annotation.Keep;

import com.rosan.installer.IPrivilegedService;

import java.io.File;

public class PrivilegedServiceImpl extends IPrivilegedService.Stub {
    @Keep
    public PrivilegedServiceImpl() {
        super();
    }

    @Override
    public void deletePath(String path) throws RemoteException {
        if (path == null) return;
        File file = new File(path);
        if (file.exists()) file.delete();
    }
}
