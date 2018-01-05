package com.ctvit.nlp.userportrait.tfidf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * mysql连接管理类
 * 
 * @author zhilin zhang
 * @date   2017年10月27日下午4:43:30
 * @version 1.0
 */
public class DbConnections {
	private static Logger logger = LoggerFactory.getLogger(DbConnections.class);
	private static Properties properties = new Properties();
	private static String MYSQL_DRIVER;
	private static String MYSQL_CONNECT;
	private static String MYSQL_DB_USER;
	private static String MYSQL_DB_PASSWD;
	private static String MYSQL_HOST;
	private static String MYSQL_PORT;
	private static String MYSQL_DB;
	
	static {
		String configFilePath = "/mysql.jdbc.properties";
		try {
			properties.load(DbConnections.class.getResourceAsStream(configFilePath));
			MYSQL_DRIVER = properties.getProperty("jdbc.driver");
			Class.forName(MYSQL_DRIVER);
		} catch (IOException e) {
			logger.error("加载配置文件出错,path= " + configFilePath, e);
		} catch (ClassNotFoundException e) {
			logger.error("加载数据库驱动出错", e);
		}

		MYSQL_HOST = properties.getProperty("jdbc.host");
		MYSQL_PORT = properties.getProperty("jdbc.port");
		MYSQL_DB = properties.getProperty("jdbc.database");
		MYSQL_CONNECT = "jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/" + MYSQL_DB + "?useSSL=true";
		MYSQL_DB_USER = properties.getProperty("jdbc.username");
		MYSQL_DB_PASSWD = properties.getProperty("jdbc.password");
	}
	
	/**
	 * 获取内容数据对应的数据库连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getContentDataConnection() throws SQLException {
		return DriverManager.getConnection(MYSQL_CONNECT, MYSQL_DB_USER, MYSQL_DB_PASSWD);
	}
}
