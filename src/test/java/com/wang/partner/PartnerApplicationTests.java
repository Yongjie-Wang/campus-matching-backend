package com.wang.partner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


@SpringBootTest
class PartnerApplicationTests {

    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String s4="abc";
        String s5=s4.replace("a","m");
        System.out.println(s4);
        System.out.println(s5);
    }


    @Test
    void contextLoads() {
        String a="123";
        a.equals("hello");
        a=new String("345");

        System.out.println(a);

    }
    @Test
    void bubble() {
        String a=1+"";
        int []arr={1,4,5,2,3,7,5,3,2,7,9};
        for (int i = 0; i <arr.length-1 ; i++) {
            for (int j=0;j<arr.length-i-1;j++){
                if(arr[j]>arr[j+1]){
                    int t=arr[j];
                    arr[j]=arr[j+1];
                    arr[j+1]=t;
                }
            }

        }
        System.out.println(Arrays.toString(arr));
    }

}
