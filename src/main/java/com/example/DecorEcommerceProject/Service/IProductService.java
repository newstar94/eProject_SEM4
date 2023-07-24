package com.example.DecorEcommerceProject.Service;

import com.example.DecorEcommerceProject.Entities.DTO.ProductDto;
import com.example.DecorEcommerceProject.Entities.DTO.ResponseProductDTO;
import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductService {
    Product createProduct(ProductDto productDto, MultipartFile imageFile, List<MultipartFile> extraImages);
    List<ResponseProductDTO> getAllProducts();
    List<ResponseProductDTO> getRandomAmountOfProducts(int size );
    List<ResponseProductDTO> getAllProductByCategoryID(Long cateID);
    ResponseProductDTO getProductByID(long id);
    String deleteProduct(Long id);
    Product updateProduct(Long id, ProductDto productDto, MultipartFile mainImageFile, List<MultipartFile> extraImageFiles);
    List<ResponseProductDTO> getAllProductsByKeyword(String keyword);
    List<ResponseProductDTO> getAllProductByCateIDAndKeyword(long cateID, String keyword);
    List<ResponseProductDTO> getTopSold(int top);
    List<ResponseProductDTO> getAllTopSold(int top);
    Integer getTotalByCategoryId(Long Id);
//    List<ProductTopSellerDto> getTopSellerOfProduct(int topNumber);
//    List<Book> getListBook_InOrder(String orderId);
}
