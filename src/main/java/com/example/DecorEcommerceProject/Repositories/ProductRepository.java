package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(
            value = "SELECT * FROM products p where p.category_id = :cateId",
            nativeQuery = true
    )
    List<Product> getAllProductByCategoryID(Long cateId);

    @Query(
            value = "SELECT * from products p " +
                    "where p.name like %:keyword% ",
            nativeQuery = true
    )
    List<Product> getAllProductsByKeyword(String keyword);
    @Query(
            value = "SELECT p.id, p.name, p.description, p.inventory, p.unit_price, p.main_image, p.extraImages,p.status, p.category_id, sum(c.quantity) " +
                    " from products p inner join order_items c on p.id = c.product_id " +
                    " inner join orders o on c.order_id = o.id " +
                    " where o.status = 'AVAILABLE'  " +
                    " group by p.category_id " +
                    " order by sum(c.quantity) desc " +
                    " limit 0, :topNumber",
            nativeQuery = true
    )
    List<Tuple> getTop_Number_Product_Best_Seller(int topNumber);

}
