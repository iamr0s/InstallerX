package com.rosan.installer;

import com.rosan.installer.IPrivilegedService;

interface IAppProcessService {
    void quit();

    IPrivilegedService getPrivilegedService();
}
