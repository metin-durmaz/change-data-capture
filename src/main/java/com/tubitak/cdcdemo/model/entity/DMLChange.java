package com.tubitak.cdcdemo.model.entity;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(value = "dml_change")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DMLChange implements Serializable {

    public DMLChange(String tableName, String operation, String date, String before, String after) {
        this.tableName = tableName;
        this.operation = operation;
        this.date = date;
        this.before = before;
        this.after = after;
    }

    @PrimaryKey
    private UUID id = UUID.randomUUID();

    @Column(value = "table_name")
    private String tableName;

    @Column(value = "operation")
    private String operation;

    @Column(value = "date")
    private String date;

    @Column(value = "before")
    private String before;

    @Column(value = "after")
    private String after;

}