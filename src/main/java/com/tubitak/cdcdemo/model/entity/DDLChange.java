package com.tubitak.cdcdemo.model.entity;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(value = "ddl_change")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DDLChange implements Serializable {

    public DDLChange(String tableName, String operation, String date) {
        this.tableName = tableName;
        this.operation = operation;
        this.date = date;
    }

    @PrimaryKey

    private UUID id = UUID.randomUUID();

    @Column(value = "object_name")
    private String tableName;

    @Column(value = "operation")
    private String operation;

    @Column(value = "date")
    private String date;

}