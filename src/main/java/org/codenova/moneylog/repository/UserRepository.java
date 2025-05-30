package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.User;


@Mapper
public interface UserRepository {

    public int save (User user);
    public User findByEmail(String email);
    public User findByProviderAndProviderId(@Param("provider") String provider,
                                            @Param("providerId") String providerId);

    public int updatePasswordByEmail(@Param("email") String email,
                                     @Param("password") String password);

    public int updateVerifiedByEmail(String email);

}
