import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestThrowableLog {

    private static Logger logger = LogManager.getLogger("com.kubeman");

    public static void main(String[] arg) {
        try {
            Integer.parseInt("abc");
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("error: ", t);
        }
    }

}
