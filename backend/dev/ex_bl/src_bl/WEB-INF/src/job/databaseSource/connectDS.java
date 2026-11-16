package job.databaseSource;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class connectDS {

	public static QueryRunner getQueryRunner() throws NamingException {
		InitialContext ic = new InitialContext();
		DataSource ds = (DataSource) ic.lookup("java:/MySqlDS_DQ");
		return new QueryRunner(ds);
	}

	public static final RowProcessor BEAN_ROW_PROCESSOR =
			new BasicRowProcessor(new GenerousBeanProcessor() {
				@Override
				public Object processColumn(java.sql.ResultSet rs, int index, Class<?> propType)
						throws java.sql.SQLException {
					Object v = super.processColumn(rs, index, propType);
					// MySQL 8 把 DATETIME/DATE 读成 java.time，实体用 java.util.Date，这里统一转换
					if (v instanceof java.time.LocalDateTime && java.util.Date.class.isAssignableFrom(propType)) {
						return java.sql.Timestamp.valueOf((java.time.LocalDateTime) v);
					}
					if (v instanceof java.time.LocalDate && java.util.Date.class.isAssignableFrom(propType)) {
						return java.sql.Date.valueOf((java.time.LocalDate) v);
					}
					return v;
				}
			});



}


