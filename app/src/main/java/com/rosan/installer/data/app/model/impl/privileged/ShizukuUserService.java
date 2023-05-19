package com.rosan.installer.data.app.model.impl.privileged;

import android.os.RemoteException;

import androidx.annotation.Keep;

import com.rosan.installer.IPrivilegedService;
import com.rosan.installer.IShizukuUserService;
import com.rosan.installer.data.app.model.impl.privileged.PrivilegedServiceImpl;

public class ShizukuUserService extends IShizukuUserService.Stub {
    @Keep
    public ShizukuUserService() {
        super();
    }

    @Override
    public void destroy() throws RemoteException {
        System.exit(0);
    }

    @Override
    public IPrivilegedService getPrivilegedService() throws RemoteException {
        return new PrivilegedServiceImpl();
    }
}
