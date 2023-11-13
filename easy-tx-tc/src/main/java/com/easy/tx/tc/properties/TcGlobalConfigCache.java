package com.easy.tx.tc.properties;

import com.easy.tx.constant.GlobalConfig;
import com.easy.tx.remote.AddressInfo;
import lombok.Data;

@Data
public class TcGlobalConfigCache {
    
    public static TcGlobalConfig tcGlobalConfig =new TcGlobalConfig();
}
