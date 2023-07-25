package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.*;
import com.example.DecorEcommerceProject.Entities.DTO.ProductDto;
import com.example.DecorEcommerceProject.Entities.DTO.ResponseProductDTO;
import com.example.DecorEcommerceProject.Entities.Enum.ProductStatus;
import com.example.DecorEcommerceProject.Repositories.CategoryRepository;
import com.example.DecorEcommerceProject.Repositories.ProductImageRepository;
import com.example.DecorEcommerceProject.Repositories.ProductRepository;
import com.example.DecorEcommerceProject.Service.IProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinary;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository,
                              CloudinaryService cloudinary
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public Product createProduct(ProductDto productDto, MultipartFile imageFile, List<MultipartFile> extraImages) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setInventory(productDto.getInventory());
        product.setStatus(ProductStatus.AVAILABLE);
        product.setPrice(productDto.getPrice());
        product.setTotal_sold(0);
        String mainImageUrl = cloudinary.saveProductImageToCloudinary(imageFile);
        product.setMainImage(mainImageUrl);
        product.setWeight(productDto.getWeight());
        product.setDeliveryAvailable(productDto.isDeliveryAvailable());
        product.setCreatedAt(LocalDateTime.now());
        long categoryId = productDto.getCategory();
        if (categoryId != 0) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }
        productRepository.save(product);
        try {
            saveExtraImages(extraImages, product);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return product;
    }

    @Override
    public List<ResponseProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return getList(products);
    }

    @Override
    public List<ResponseProductDTO> getRandomAmountOfProducts() {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new EntityNotFoundException("Couldn't find any product in DB");
        }
        Collections.shuffle(productList);
        List<Product> randomProducts = productList.subList(0, 5);
        return getList(randomProducts);
    }

    @Override
    public List<ResponseProductDTO> getAllProductByCategoryID(Long cateID) {
        List<Product> products = productRepository.getAllProductByCategoryID(cateID);
        return getList(products);
    }

    @Override
    public ResponseProductDTO getProductByID(long id) {
        Product product = productRepository.findById(id).orElse(null);
        assert product != null;
        ResponseProductDTO responseProductDTO = new ResponseProductDTO();
        getResponseProductDTO(product, responseProductDTO);
        return responseProductDTO;
    }

    @Override
    public String deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (!product.isPresent()) {
            return "Not found product with id: " + id;
        } else {
            productRepository.delete(product.get());
            return "Product with id " + id + " has been deleted!";
        }
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDto productDto, MultipartFile mainImageFile, List<MultipartFile> extraImages) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setInventory(productDto.getInventory());
        existingProduct.setStatus(productDto.getProductStatus());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setDeliveryAvailable(productDto.isDeliveryAvailable());
        existingProduct.setWeight(productDto.getWeight());
        existingProduct.setUpdatedAt(LocalDateTime.now());
        long categoryId = productDto.getCategory();
        if (categoryId != 0) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            try {

                String mainImageUrl = cloudinary.saveProductImageToCloudinary(mainImageFile);
                deleteMainImage(existingProduct);
                existingProduct.setMainImage(mainImageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload main image");
            }
        }
        List<ProductImage> updatedExtraImages = new ArrayList<>();
        deleteProductImages(existingProduct);
        try {
            saveExtraImages(extraImages, existingProduct);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload extra image");
        }
        existingProduct.setImages(updatedExtraImages);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        productRepository.save(existingProduct);
        return existingProduct;
    }

    private void deleteProductImages(Product product) {
        List<ProductImage> productImages = product.getImages();
        if (productImages != null && !productImages.isEmpty()) {
            for (ProductImage productImage : productImages) {
                // Delete the image from Cloudinary
                cloudinary.deleteProductImageFromCloudinary(productImage.getImageUrl());
            }
            productImageRepository.deleteByProductId(product.getId());
        }
    }

    private void saveExtraImages(List<MultipartFile> extraImages, Product product) {
        List<String> extraImageUrls = new ArrayList<>();
        for (MultipartFile extraImageFile : extraImages) {
            String extraImageUploadResult = cloudinary.saveProductImageToCloudinary(extraImageFile);
            extraImageUrls.add(extraImageUploadResult);
        }
        for (String extraImageUrl : extraImageUrls) {
            ProductImage extraImage = new ProductImage(extraImageUrl, product);
            productImageRepository.save(extraImage);
        }
    }

    private void deleteMainImage(Product product) {
        String mainImage = product.getMainImage();
        if (mainImage != null && !mainImage.isEmpty()) {
            cloudinary.deleteProductImageFromCloudinary(mainImage);
        }
    }

    @Override
    public List<ResponseProductDTO> getAllProductsByKeyword(String keyword) {
        List<Product> products = productRepository.getAllProductsByKeyword(keyword);
        return getList(products);
    }

    @Override
    public List<ResponseProductDTO> getAllProductByCateIDAndKeyword(long cateID, String keyword) {
        List<Product> getByKeyword = productRepository.getAllProductsByKeyword(keyword);
        List<Product> getByCateIDAndKeyword = new ArrayList<>();
        for (Product product : getByKeyword) {
            if (product.getCategory().getId() == cateID) {
                getByCateIDAndKeyword.add(product);
            }
        }
        return getList(getByCateIDAndKeyword);
    }

    @Override
    public List<ResponseProductDTO> getTopSold(int top) {
        List<Product> list = productRepository.getTopSelling(top);
        List<Product> topSold = new ArrayList<>();
        for (Product sold : list) {
            if (sold.getStatus() != ProductStatus.UNAVAILABLE && sold.getStatus() != ProductStatus.OUT_OF_STOCK) {
                topSold.add(sold);
            }
        }
        return getList(topSold);
    }

    @Override
    public List<ResponseProductDTO> getAllTopSold(int top) {
        return getList(productRepository.getTopSelling(top));
    }

    @Override
    public Integer getTotalByCategoryId(Long Id) {
        return productRepository.getTotalByCategoryId(Id);
    }

    public List<ResponseProductDTO> getList(List<Product> products) {
        List<ResponseProductDTO> responseProductDTOS = new ArrayList<>();
        for (Product product : products) {
            ResponseProductDTO responseProductDTO = new ResponseProductDTO();
            getResponseProductDTO(product, responseProductDTO);
            responseProductDTOS.add(responseProductDTO);
        }
        return responseProductDTOS;
    }

    private void getResponseProductDTO(Product product, ResponseProductDTO responseProductDTO) {
        responseProductDTO.setProduct(product);
        List<DiscountHistory> discountHistories = product.getDiscountHistories();
        if (discountHistories.size() == 0) {
            responseProductDTO.setPrice_discount(product.getPrice());
        }
        for (DiscountHistory discountHistory : discountHistories) {
            Discount discount = discountHistory.getDiscount();
            if (discount.getStart().isBefore(LocalDateTime.now())
                    && discount.getEnd().isAfter(LocalDateTime.now()) && discount.getLimit() > 0) {
                double discountAmount = Math.min(discount.getDiscountAmountMax(),
                        product.getPrice() * discount.getDiscountPercentage() / 100);
                responseProductDTO.setPrice_discount(product.getPrice() - discountAmount);
                break;
            }
            responseProductDTO.setPrice_discount(product.getPrice());
        }
    }
}
