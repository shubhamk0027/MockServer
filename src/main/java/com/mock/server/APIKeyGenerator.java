package com.mock.server;

import java.util.concurrent.atomic.AtomicLong;

public class APIKeyGenerator {

    private final static int KEY_LEN = 8;
    private final static String INDICES = "Za0Yb1Xc2Wd3Ve4Uf5Tg6Sh7Ri8Qj9PkOlNmMnLoKpJqIrHsGtFuEvDwCxByAz";

    private static AtomicLong counter;

    public static void setCounter(int val){
        counter=new AtomicLong(val);
    }

    public static String generateKEY(){
        // 62^8 > 2^47 => 2^47 numbers can be generated without duplicates!
        // 10^5 billion urls!
        long num = counter.incrementAndGet();
        String binary = Long.toBinaryString((1L<<47)|num).substring(1);
        binary = new StringBuilder(binary).reverse().toString();
        num =  Long.parseLong(binary,2);

        StringBuilder key= new StringBuilder();
        for(int i=0;i<KEY_LEN;i++){
            long SZ = 62;
            key.append(INDICES.charAt((int) (num%SZ)));
            num=num/SZ;
        }

        return key.toString();
    }

    public static long getCounter(){
        return  counter.longValue();
    }
}

