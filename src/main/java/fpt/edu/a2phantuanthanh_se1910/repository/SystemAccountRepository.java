// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SystemAccountRepository extends JpaRepository<SystemAccount, Long> {

    @Query("""
            SELECT a FROM SystemAccount a
            WHERE LOWER(a.accountName) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
                OR LOWER(a.accountEmail) LIKE CONCAT('%', :keyword, '%') ESCAPE '!'
            ORDER BY a.accountId
            """)
    List<SystemAccount> search(@Param("keyword") String keyword);

    Optional<SystemAccount> findByAccountEmailIgnoreCase(String accountEmail);

    boolean existsByAccountEmailIgnoreCase(String accountEmail);
}
