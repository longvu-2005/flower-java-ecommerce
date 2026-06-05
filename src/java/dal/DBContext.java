package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DBContext {
    public Connection getConnection() throws Exception {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ConnectDB.properties")) {
            if (is != null) {
                props.load(is);
            }
        }
        
        String url = props.getProperty("url");
        String user = props.getProperty("userID");
        String pass = props.getProperty("password");
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, pass);
    }
}