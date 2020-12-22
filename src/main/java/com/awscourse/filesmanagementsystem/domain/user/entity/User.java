package com.awscourse.filesmanagementsystem.domain.user.entity;

import com.awscourse.filesmanagementsystem.domain.auditedobject.AuditedObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

import static com.awscourse.filesmanagementsystem.infrastructure.jpa.CacheRegions.USER_ENTITY_CACHE;
import static com.awscourse.filesmanagementsystem.infrastructure.jpa.CacheRegions.USER_NATURAL_ID_CACHE;
import static com.awscourse.filesmanagementsystem.infrastructure.jpa.PersistenceConstants.ID_GENERATOR;

@Entity
@Table(name = "user")
@SQLDelete(sql = "UPDATE user SET object_state = 'REMOVED' WHERE id = ?")
@Loader(namedQuery = "findUserById")
@NamedQuery(name = "findUserById", query = "SELECT u FROM User u WHERE u.id = ?1 AND u.objectState = com.awscourse.filesmanagementsystem.domain.auditedobject.ObjectState.ACTIVE")
@Where(clause = AuditedObject.IS_ACTIVE_OBJECT)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = USER_ENTITY_CACHE)
@NaturalIdCache(region = USER_NATURAL_ID_CACHE)
public class User extends AuditedObject {

    @Id
    @GeneratedValue(generator = ID_GENERATOR)
    private Long id;

    @NotBlank
    @Size(max = 40)
    private String name;

    @NotBlank
    @Size(max = 40)
    private String surname;

    @NaturalId
    @NotBlank
    @Size(max = 40)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;

    @Email
    @NotBlank
    @Size(max = 40)
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(max = 40)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        User otherUser = (User) obj;
        return Objects.equals(otherUser.getUsername(), getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername());
    }

}
