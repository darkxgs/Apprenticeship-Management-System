public class CheckUCanAccess {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.ucanaccess.complex.Attachment");
        for (java.lang.reflect.Constructor<?> c : clazz.getConstructors()) {
            System.out.println(c);
        }
    }
}
