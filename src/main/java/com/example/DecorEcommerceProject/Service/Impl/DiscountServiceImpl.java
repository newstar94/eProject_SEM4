package com.example.DecorEcommerceProject.Service.Impl;

import org.springframework.stereotype.Service;

import com.example.DecorEcommerceProject.Entities.Discount;
import com.example.DecorEcommerceProject.Entities.DiscountHistory;
import com.example.DecorEcommerceProject.Entities.Product;
import com.example.DecorEcommerceProject.Entities.DTO.DiscountDTO;
import com.example.DecorEcommerceProject.Repositories.DiscountHistoryRepository;
import com.example.DecorEcommerceProject.Repositories.DiscountRepository;
import com.example.DecorEcommerceProject.Repositories.ProductRepository;
import com.example.DecorEcommerceProject.Service.IDiscountService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class DiscountServiceImpl implements IDiscountService {
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    private final DiscountHistoryRepository discountHistoryRepository;
    public DiscountServiceImpl(DiscountRepository discountRepository, ProductRepository productRepository, DiscountHistoryRepository discountHistoryRepository) {
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
        this.discountHistoryRepository = discountHistoryRepository;
    }
    @Override
    public List<Discount> getAllDiscount() {
        return discountRepository.findAll();
    }
    @Override
    public Optional<DiscountDTO> getDiscountById(Long id) {
        Optional<Discount> discount = discountRepository.findById(id);
        if (discount.isPresent()) {
            DiscountDTO discountDTO = new DiscountDTO();
            List<Product> products = discountRepository.getAllProductByDiscountId(id);
            discountDTO.setDiscount(discount.get());
            discountDTO.setProducts(products);
            return Optional.of(discountDTO);
        }
        return Optional.empty();
    }
    @Override
    @Transactional
    public Discount createDiscount(DiscountDTO discountDTO) {
        Discount createdDiscount = discountRepository.save(discountDTO.getDiscount());
        return saveDiscountHistory(discountDTO,createdDiscount);
    }
    @Override
    @Transactional
    public Discount updateDiscount(Long id, DiscountDTO discountDTO) {
        Discount discount = discountRepository.findById(id).orElse(null);
        assert discount != null;
        if (discount.getStart().isAfter(LocalDateTime.now())) {
            discount = discountDTO.getDiscount();
            discount.setId(id);
            discount = discountRepository.save(discount);
            for (Product product : discountRepository.getAllProductByDiscountId(discount.getId())) {
                discountHistoryRepository.deleteByProductIdAndDiscountId(product.getId(), discount.getId());
            }
            return saveDiscountHistory(discountDTO,discount);
        }
        return null;
    }
    @Override
    public List<Discount> getAllDiscountByProductId(Long id) {
        return discountRepository.getAllDiscountByProductId(id);
    }
    @Override
    public List<Product> getAllProductByDiscountId(Long id){
        return discountRepository.getAllProductByDiscountId(id);
    }
    private boolean isProductAlreadyApplied(Long productId, Discount newDiscount) {
        LocalDateTime startDate = newDiscount.getStart();
        LocalDateTime endDate = newDiscount.getEnd();
        Product product = productRepository.findById(productId).orElse(null);
        assert product != null;
        for (DiscountHistory discountHistory : product.getDiscountHistories()) {
            Discount existingDiscount = discountHistory.getDiscount();
            if (newDiscount.getId()==existingDiscount.getId()){
                continue;
            }
            if (startDate.isEqual(existingDiscount.getStart()) || endDate.isEqual(existingDiscount.getEnd())
                    || (startDate.isAfter(existingDiscount.getStart()) && startDate.isBefore(existingDiscount.getEnd()))
                    || (endDate.isAfter(existingDiscount.getStart()) && endDate.isBefore(existingDiscount.getEnd()))
                    || (startDate.isBefore(existingDiscount.getStart()) && endDate.isAfter(existingDiscount.getEnd()))) {
                return true;
            }
        }
        return false;
    }
    private Discount saveDiscountHistory(DiscountDTO discountDTO ,Discount discount){
        List<String> message = new ArrayList<>();
        for (Product product : discountDTO.getProducts()) {
            if (isProductAlreadyApplied(product.getId(), discount)) {
                message.add("Product " + productRepository.findById(product.getId()).get().getName() + " already has a discount applied within the desired time range");
            }
        }
        if (message.size()!=0){
            throw new IllegalStateException(message.toString());
        }
        for (Product product : discountDTO.getProducts()) {
            DiscountHistory discountHistory = new DiscountHistory();
            discountHistory.setDiscount(discount);
            discountHistory.setProduct(product);
            discountHistoryRepository.save(discountHistory);
        }
        return discount;
    }
}

