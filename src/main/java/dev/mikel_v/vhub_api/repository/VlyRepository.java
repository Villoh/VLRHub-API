package dev.mikel_v.vhub_api.repository;

import dev.mikel_v.vhub_api.entity.VlyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "vlyes", collectionResourceRel = "vlyes")
public interface VlyRepository extends PagingAndSortingRepository<VlyEntity, Long>, JpaRepository<VlyEntity, Long> {

}
