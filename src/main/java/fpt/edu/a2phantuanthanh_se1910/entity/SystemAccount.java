// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "SystemAccount", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_SystemAccount_AccountEmail", columnNames = "AccountEmail")
})
@Check(name = "CK_SystemAccount_Role", constraints = "AccountRole IN (1, 2)")
public class SystemAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID", nullable = false, updatable = false)
    private Long accountId;

    @NotBlank
    @Size(max = 100)
    @Nationalized
    @Column(name = "AccountName", length = 100, nullable = false)
    private String accountName;

    @NotBlank
    @Email
    @Size(max = 254)
    @Nationalized
    @Column(name = "AccountEmail", length = 254, nullable = false)
    private String accountEmail;

    @NotNull
    @Min(1)
    @Max(2)
    @Column(name = "AccountRole", nullable = false)
    private Integer accountRole;

    @NotBlank
    @Size(max = 255)
    @Nationalized
    @Column(name = "AccountPassword", length = 255, nullable = false)
    private String accountPassword;

    protected SystemAccount() {
    }

    public SystemAccount(String accountName, String accountEmail, Integer accountRole,
                         String accountPassword) {
        this.accountName = accountName;
        this.accountEmail = accountEmail;
        this.accountRole = accountRole;
        this.accountPassword = accountPassword;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public Integer getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(Integer accountRole) {
        this.accountRole = accountRole;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

}
