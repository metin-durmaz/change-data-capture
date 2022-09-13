package com.tubitak.cdcdemo.model.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;

import com.tubitak.cdcdemo.model.entity.DMLChange;

public interface DMLChangeRepository extends CassandraRepository<DMLChange, String> {

    // @AllowFiltering

}