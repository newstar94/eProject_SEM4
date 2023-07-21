package com.example.DecorEcommerceProject.Service;

import com.example.DecorEcommerceProject.Entities.DTO.ProductDto;
import com.example.DecorEcommerceProject.Entities.DTO.SoldDTO;
import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    Product createProduct(ProductDto productDto, MultipartFile imageFile, List<MultipartFile> extraImages);
    List<Product> getAllProducts();
    List<Product> getAllProductByCategoryID(Long cateID);
    Optional<Product> getProductByID(long id);
    String deleteProduct(Long id);
    Product updateProduct(Long id, ProductDto productDto, MultipartFile mainImageFile, List<MultipartFile> extraImageFiles);
    List<Product> getAllProductsByKeyword(String keyword);
    List<Product> getAllProductByCateIDAndKeyword(long cateID, String keyword);
    List<SoldDTO> getTopSold(int top);
    List<SoldDTO> getAllTopSold(int top);
//    List<ProductTopSellerDto> getTopSellerOfProduct(int topNumber);
//    List<Book> getListBook_InOrder(String orderId);
}
