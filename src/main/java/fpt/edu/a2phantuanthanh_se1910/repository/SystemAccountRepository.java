// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.repository;

import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemAccountRepository extends JpaRepository<SystemAccount, Long> {

    Optional<SystemAccount> findByAccountEmailIgnoreCase(String accountEmail);

    boolean existsByAccountEmailIgnoreCase(String accountEmail);
}
