package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.codenova.moneylog.entity.Verification;

@Mapper
public interface VerificationRepository {
    public int create (Verification verification);
    public Verification findByToken (String token);


}
