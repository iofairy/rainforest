package com.iofairy.test.zip;

import com.iofairy.rainforest.zip.config.PasswordProvider;
import com.iofairy.rainforest.zip.config.ZipPassword;
import com.iofairy.top.G;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author GG
 * @version 1.0
 * @date 2023/5/15 7:26
 */
public class ZipPasswordTest {
    @Test
    void testZipPassword() {
        ZipPassword password1 = ZipPassword.of("a【文】b[]()-+$^cde.??", "password1_$%^");
        ZipPassword password2 = ZipPassword.of("a【文】b[]()-+$^*fg", "password2_$%^");
        ZipPassword password3 = ZipPassword.of("a【文】b[]()-+$^?fg", "password3_$%^");
        ZipPassword password4 = ZipPassword.of("a【文】b[]()-+$^cde.*", "password4_$%^");
        ZipPassword password5 = ZipPassword.of("a【文】b[]()", "password5_$%^");
        ZipPassword password6 = ZipPassword.of("a【文】b[]()-cd", "password6_$%^");
        ZipPassword password7 = ZipPassword.of("?【文】???()", "password7_$%^");
        ZipPassword password8 = ZipPassword.of("***a【文】**b?", "password8_$%^");
        ZipPassword password9 = ZipPassword.of("?【文】???()", "password9_$%^");

        System.out.println("password1: " + password1);
        System.out.println("password2: " + password2);
        System.out.println("password3: " + password3);
        System.out.println("password4: " + password4);
        System.out.println("password5: " + password5);
        System.out.println("password6: " + password6);
        System.out.println("password7: " + password7);
        System.out.println("password8: " + password8);
        System.out.println("password9: " + password9);

        assertEquals(password1.getPattern().toString(), "a【文】b\\[]\\(\\)\\-\\+\\$\\^cde\\...");
        assertEquals(password2.getPattern().toString(), "a【文】b\\[]\\(\\)\\-\\+\\$\\^.*fg");
        assertEquals(password3.getPattern().toString(), "a【文】b\\[]\\(\\)\\-\\+\\$\\^.fg");
        assertEquals(password4.getPattern().toString(), "a【文】b\\[]\\(\\)\\-\\+\\$\\^cde\\..*");
        assertEquals(password5.getPattern().toString(), "a【文】b\\[]\\(\\)");
        assertEquals(password6.getPattern().toString(), "a【文】b\\[]\\(\\)\\-cd");
        assertEquals(password7.getPattern().toString(), ".【文】...\\(\\)");
        assertEquals(password8.getPattern().toString(), ".*a【文】.*b.");
        assertEquals(password9.getPattern().toString(), ".【文】...\\(\\)");

        PasswordProvider passwordProvider = PasswordProvider.of(password1, password2, password3, password4, password5, password6, password7, password8, password9);
        List<ZipPassword> zipPasswordList = passwordProvider.getZipPasswordList();
        System.out.println(zipPasswordList);
        assertEquals(zipPasswordList.toString(), "[ZipPassword(fileName=a【文】b[](), pattern=a【文】b\\[]\\(\\)), " +
                "ZipPassword(fileName=a【文】b[]()-cd, pattern=a【文】b\\[]\\(\\)\\-cd), " +
                "ZipPassword(fileName=a【文】b[]()-+$^cde.??, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^cde\\...), " +
                "ZipPassword(fileName=a【文】b[]()-+$^cde.*, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^cde\\..*), " +
                "ZipPassword(fileName=a【文】b[]()-+$^?fg, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^.fg), " +
                "ZipPassword(fileName=a【文】b[]()-+$^*fg, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^.*fg), " +
                "ZipPassword(fileName=?【文】???(), pattern=.【文】...\\(\\)), " +
                "ZipPassword(fileName=*a【文】*b?, pattern=.*a【文】.*b.)]");

        System.out.println("============================================================");
        String password01 = passwordProvider.getPassword("【文】") == null ? null : new String(passwordProvider.getPassword("【文】"));
        String password02 = new String(passwordProvider.getPassword("a【文】b[]()"));
        String password03 = new String(passwordProvider.getPassword("a【文】b[]()-cd"));
        String password04 = new String(passwordProvider.getPassword("a【文】bcd()"));
        String password05 = new String(passwordProvider.getPassword("a【文】b[]()-+$^cde.-"));
        String password06 = new String(passwordProvider.getPassword("a【文】b[]()-+$^cde.--"));
        String password07 = new String(passwordProvider.getPassword("a【文】b[]()-+$^cde.--a"));
        String password08 = new String(passwordProvider.getPassword("a【文】b[]()-+$^.fg"));
        String password09 = new String(passwordProvider.getPassword("a【文】b[]()-+$^fg"));
        String password10 = new String(passwordProvider.getPassword("a【文】b[]()-+$^bcfg"));
        System.out.println("password01: " + password01);
        System.out.println("password02: " + password02);
        System.out.println("password03: " + password03);
        System.out.println("password04: " + password04);
        System.out.println("password05: " + password05);
        System.out.println("password06: " + password06);
        System.out.println("password07: " + password07);
        System.out.println("password08: " + password08);
        System.out.println("password09: " + password09);
        System.out.println("password10: " + password10);

        assertNull(password01);
        assertEquals(password02, "password5_$%^");
        assertEquals(password03, "password6_$%^");
        assertEquals(password04, "password9_$%^");
        assertEquals(password05, "password4_$%^");
        assertEquals(password06, "password1_$%^");
        assertEquals(password07, "password4_$%^");
        assertEquals(password08, "password3_$%^");
        assertEquals(password09, "password2_$%^");
        assertEquals(password10, "password2_$%^");

        passwordProvider.addPassword(ZipPassword.of(null, "common_password"));
        List<ZipPassword> zipPasswordList1 = passwordProvider.getZipPasswordList();
        System.out.println(zipPasswordList1);
        assertEquals(zipPasswordList1.toString(), "[ZipPassword(fileName=a【文】b[](), pattern=a【文】b\\[]\\(\\)), " +
                "ZipPassword(fileName=a【文】b[]()-cd, pattern=a【文】b\\[]\\(\\)\\-cd), " +
                "ZipPassword(fileName=a【文】b[]()-+$^cde.??, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^cde\\...), " +
                "ZipPassword(fileName=a【文】b[]()-+$^cde.*, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^cde\\..*), " +
                "ZipPassword(fileName=a【文】b[]()-+$^?fg, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^.fg), " +
                "ZipPassword(fileName=a【文】b[]()-+$^*fg, pattern=a【文】b\\[]\\(\\)\\-\\+\\$\\^.*fg), " +
                "ZipPassword(fileName=?【文】???(), pattern=.【文】...\\(\\)), " +
                "ZipPassword(fileName=*a【文】*b?, pattern=.*a【文】.*b.), " +
                "ZipPassword(fileName=*, pattern=.*)]");

        String password11 = passwordProvider.getPassword("【文】") == null ? null : new String(passwordProvider.getPassword("【文】"));
        String password12 = new String(passwordProvider.getPassword("a【文】b[]()"));
        String password13 = new String(passwordProvider.getPassword("a【文】b[]()-cd"));
        String password14 = new String(passwordProvider.getPassword("a【文】bcd()"));
        String password15 = new String(passwordProvider.getPassword("a【文】b[]()-+$^cde.-"));
        String password16 = new String(passwordProvider.getPassword("a【文】b[]()-+$^cde.--"));
        String password17 = new String(passwordProvider.getPassword("a【文】b[]()-+$^cde.--a"));
        String password18 = new String(passwordProvider.getPassword("a【文】b[]()-+$^.fg"));
        String password19 = new String(passwordProvider.getPassword("a【文】b[]()-+$^fg"));
        String password20 = new String(passwordProvider.getPassword("a【文】b[]()-+$^bcfg"));
        System.out.println("password11: " + password11);
        System.out.println("password12: " + password12);
        System.out.println("password13: " + password13);
        System.out.println("password14: " + password14);
        System.out.println("password15: " + password15);
        System.out.println("password16: " + password16);
        System.out.println("password17: " + password17);
        System.out.println("password18: " + password18);
        System.out.println("password19: " + password19);
        System.out.println("password20: " + password20);
        assertEquals(password11, "common_password");
        assertEquals(password12, "password5_$%^");
        assertEquals(password13, "password6_$%^");
        assertEquals(password14, "password9_$%^");
        assertEquals(password15, "password4_$%^");
        assertEquals(password16, "password1_$%^");
        assertEquals(password17, "password4_$%^");
        assertEquals(password18, "password3_$%^");
        assertEquals(password19, "password2_$%^");
        assertEquals(password20, "password2_$%^");

        System.out.println("============================================================");
        PasswordProvider passwordProvider1 = PasswordProvider.of();
        char[] password = passwordProvider1.getPassword("【文】");
        System.out.println(G.toString(password));
        assertNull(password);

    }

}
