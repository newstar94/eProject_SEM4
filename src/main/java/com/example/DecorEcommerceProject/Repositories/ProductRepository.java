package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
//    @Query(
//            value = "SELECT p.id, p.name, p.description, p.inventory, p.price, p.imageUrl, sum(c.quantity) " +
//                    " from products p inner join order_item c on p.id = c.book_id " +
//                    " inner join orders o on c.order_id = o.order_id " +
//                    " where o.status = 'AVAILABLE' or o.status = 'COMPLETED' " +
//                    " group by p.title " +
//                    " order by sum(c.quantity) desc " +
//                    " limit 0, :topNumber",
//            nativeQuery = true
//    )
//    List<Tuple> getTop_Number_Product_Best_Seller(int topNumber);

}
