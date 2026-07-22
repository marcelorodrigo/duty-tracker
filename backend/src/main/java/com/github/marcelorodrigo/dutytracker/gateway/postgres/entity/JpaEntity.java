package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.proxy.HibernateProxy;

@MappedSuperclass
public abstract class JpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    protected JpaEntity() {}

    protected JpaEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JpaEntity that) || !persistentClass(this).equals(persistentClass(that))) {
            return false;
        }
        Long thisId = identifier(this);
        return thisId != null && thisId.equals(identifier(that));
    }

    @Override
    public final int hashCode() {
        return persistentClass(this).hashCode();
    }

    private static Class<?> persistentClass(JpaEntity entity) {
        if (entity instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }

    private static Long identifier(JpaEntity entity) {
        if (entity instanceof HibernateProxy proxy) {
            return (Long) proxy.getHibernateLazyInitializer().getIdentifier();
        }
        return entity.id;
    }
}
