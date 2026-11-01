package com.sefa.sensor.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private SensorType type;

    @Embedded
    private GeoLocation location;

    private boolean active;

    @Version
    private long version;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Sensor() {
    }

    public Sensor(String name, SensorType type, GeoLocation location, boolean active) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.active = active;
    }

    /** Kayıt veritabanına ilk yazılmadan hemen önce Hibernate çağırır. */
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SensorType getType() {
        return type;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {

        if (!active) {

            throw new IllegalStateException("Sensor zaten pasif. sensor adı:" + name);
        }

        active = false;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
