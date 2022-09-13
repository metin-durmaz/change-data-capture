package com.tubitak.cdcdemo.model.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;

import com.tubitak.cdcdemo.model.entity.DDLChange;

public interface DDLChangeRepository extends CassandraRepository<DDLChange, String> {

    // @AllowFiltering

}