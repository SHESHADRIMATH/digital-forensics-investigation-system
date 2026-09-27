package com.digitalforensics.model;

/**
 * Parent of every model class (inheritance).
 * Holds the database primary key that all entities share.
 */
public abstract class BaseEntity {

    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** Human-readable code shown in the UI, e.g. "EV-0007". Each subclass decides its own format (polymorphism). */
    public abstract String getDisplayCode();
}
