package com.rosan.installer;

import com.rosan.installer.IPrivilegedService;

interface INewProcess {
    IPrivilegedService getPrivilegedService() = 21;
}
