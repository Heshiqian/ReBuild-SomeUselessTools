package cn.heshiqian.alipaydemo.repo;

import cn.heshiqian.alipaydemo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {

    Optional<Order> findByOrderNumber(String orderNumber);

}
