// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class A2PhanTuanThanhSe1910ApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void contextLoads() {
        assertThat(entityManagerFactory.isOpen()).isTrue();
    }

    @Test
    void connectsToAssignmentDatabaseOnSqlServer() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT DB_NAME(), 1")) {
            assertThat(connection.getMetaData().getDatabaseProductName())
                    .isEqualTo("Microsoft SQL Server");
            assertThat(result.next()).isTrue();
            assertThat(result.getString(1)).isEqualTo("A2PhanTuanThanh_SE1910");
            assertThat(result.getInt(2)).isEqualTo(1);
        }
    }

}
