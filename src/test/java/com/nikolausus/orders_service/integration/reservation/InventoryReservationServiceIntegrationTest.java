package com.nikolausus.orders_service.integration.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolausus.orders_service.entity.Product;
import com.nikolausus.orders_service.entity.Reservation;
import com.nikolausus.orders_service.integration.config.BaseIntegrationTest;
import com.nikolausus.orders_service.repository.ProductRepository;
import com.nikolausus.orders_service.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class InventoryReservationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void cleanup() {
        reservationRepository.deleteAll();
        productRepository.deleteAll();
    }

    /**
     * Резервация создается успешно при существующем продукте и quantity <= stock
     */
    @Test
    void shouldCreateReservationSuccessfully() throws Exception {
        Long productId = createProduct("test", 10);

        mockMvc.perform(post("/reservations")
                .param("productId", String.valueOf(productId))
                .param("quantity", "5"))
                .andExpect(status().isOk());

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getStatus()).isEqualTo(Reservation.Status.ACTIVE);
    }

    /**
     * Резервация не создается, тк product не существует
     */
    @Test
    void shouldFailWhenNotProductDontExists() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("productId", String.valueOf(1))
                        .param("quantity", "5"))
                .andExpect(status().isBadRequest());

        assertThat(reservationRepository.findAll()).hasSize(0);
    }

    /**
     * Резервация не создается, тк quantity > stock
     */
    @Test
    void shouldFailWhenNotEnoughStock() throws Exception {
        Long productId = createProduct("test", 10);

        mockMvc.perform(post("/reservations")
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "500"))
                .andExpect(status().isBadRequest());

        assertThat(reservationRepository.findAll()).hasSize(0);
    }

    /**
     * После создания резервации stock продукта должен уменьшиться
     */
    @Test
    void shouldDecreaseStockOnCreateReservation() throws Exception {
        Long productId = createProduct("test", 10);
        mockMvc.perform(post("/reservations")
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "5"))
                .andExpect(status().isOk());

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getStock()).isEqualTo(5);
    }

    /**
     * После создания и отмены резервации stock продукта должен остаться прежним
     */
    @Test
    void shouldBeEqualsStockAfterCreateAndCancelReservation() throws Exception {
        Long productId = createProduct("test", 10);
        Long reservationId = Long.parseLong(mockMvc.perform(post("/reservations")
                        .param("productId", String.valueOf(productId))
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        cancelReservationIsOk(reservationId);

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getStock()).isEqualTo(10);
    }

    /**
     * После отмены существующей резервации stock должен увеличиться
     */
    @Test
    void shouldIncreaseStockOnCancelReservation() throws Exception {
        Long productId = createProduct("test", 10);
        cancelReservationIsOk(createReservation(productId, 10, LocalDateTime.now(), Reservation.Status.ACTIVE));

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getStock()).isEqualTo(20);
    }

    /**
     * Просроченную резервацию нельзя подтвердить
     */
    @Test
    void shouldFailToConfirmExpiredReservation() throws Exception {
        Long productId = createProduct("test", 10);
        Long reservationId = createReservation(
                productId,
                5,
                LocalDateTime.now().minusMinutes(Reservation.expireDelayInMinutes + 1),
                Reservation.Status.EXPIRED);

        confirmReservationIsBad(reservationId);
    }

    /**
     * Просроченная резервация (now > reservation.expires_at) со статусом ACTIVE нельзя подтвердить
     * Также проверяем, что статус резервации автоматически изменился
     */
    @Test
    void shouldFailToConfirmExpiredReservationAutoSwitchReservationStatus() throws Exception {
        Long productId = createProduct("test", 10);
        Long reservationId = createReservation(
                productId,
                5,
                LocalDateTime.now().minusMinutes(Reservation.expireDelayInMinutes + 1),
                Reservation.Status.ACTIVE);

        confirmReservationIsBad(reservationId);
        assertThat(reservationRepository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo(Reservation.Status.EXPIRED);
    }

    /**
     * Просроченная резервация со статусом ACTIVE при попытке подтверждения автоматически становится EXPIRED
     * Stock продукта повышается, тк резервация была "активна"
     */
    @Test
    void shouldIncreaseStockAfterTryToConfirmExpiredReservation() throws Exception {
        Long productId = createProduct("test", 10);
        Long reservationId = createReservation(
                productId,
                10,
                LocalDateTime.now().minusMinutes(Reservation.expireDelayInMinutes + 1),
                Reservation.Status.ACTIVE);

        confirmReservationIsBad(reservationId);
        assertThat(reservationRepository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo(Reservation.Status.EXPIRED);
        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(20);
    }

    /**
     * Резервация после подтверждения должна иметь статус CONFORMED
     */
    @Test
    void reservationShouldHaveStatusConfirm() throws Exception {
        Long productId = createProduct("test", 10);
        Long reservationId = createReservation(productId, 5, LocalDateTime.now(), Reservation.Status.ACTIVE);

        confirmReservationIsOk(reservationId);

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(Reservation.Status.CONFIRMED);
    }

    /**
     * Резервацию нельзя подтвердить дважды (второе подтверждение выкидывает ошибку)
     */
    @Test
    void shouldFailToConfirmTwice() throws Exception {
        Long productId = createProduct("test", 10);
        Long reservationId = createReservation(
                productId,
                5,
                LocalDateTime.now(),
                Reservation.Status.ACTIVE);

        confirmReservationIsOk(reservationId);
        confirmReservationIsBad(reservationId);
    }

    /**
     * Проверка ручки /products/top-reserved
     * Результат приходит в формате сортированного (по SUM(reservation.quantity) со статусом CONFIRMED) json
     * Также проверяется, что старые (созданные больше чем 24 часа назад) резервации не влияют на результат
     */
    @Test
    void shouldReturnTopReservedProducts() throws Exception {
        Long p1 = createProduct("product_1", 100);
        Long p2 = createProduct("product_2", 100);
        Long p3 = createProduct("product_3", 100);
        Long p4 = createProduct("product_4", 100);
        Long p5 = createProduct("product_5", 100);
        Long p6 = createProduct("product_6", 100);

        confirmReservationIsOk(createReservation(p1, 5, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p1, 5, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p2, 60, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p3, 1, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p3, 1, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p3, 1, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p4, 1, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p5, 1, LocalDateTime.now(), Reservation.Status.ACTIVE));
        confirmReservationIsOk(createReservation(p6, 1, LocalDateTime.now(), Reservation.Status.ACTIVE));
        // Старая резервация (createdAt > чем сутки назад), НЕ виляет на результат top-confirmed
        createReservation(p6, 1000, LocalDateTime.now().minusDays(2), Reservation.Status.CONFIRMED);

        ObjectMapper mapper = new ObjectMapper();

        String jsonResponse = mockMvc.perform(get("/products/top-reserved"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LinkedHashMap<String, Long> result = mapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );

        List<String> keys = result.keySet().stream().toList();

        // Проверка, что в ответе не более 5 продуктов
        assertThat(keys.size()).isLessThanOrEqualTo(5);

        assertThat(keys.get(0)).contains("id=" + p2);
        assertThat(result.get(keys.get(0))).isEqualTo(60);

        assertThat(keys.get(1)).contains("id=" + p1);
        assertThat(result.get(keys.get(1))).isEqualTo(10);

        assertThat(keys.get(2)).contains("id=" + p3);
        assertThat(result.get(keys.get(2))).isEqualTo(3);
    }

    private Long createProduct(String name, long stock) {
        return productRepository.save(
                Product.builder()
                        .name(name)
                        .stock(stock)
                        .build()
        ).getId();
    }

    private Long createReservation(Long productId, long quantity, LocalDateTime now, Reservation.Status status) {
        return reservationRepository.save(
                Reservation.builder()
                        .product(productRepository.findById(productId).orElseThrow())
                        .quantity(quantity)
                        .status(status)
                        .createdAt(now)
                        .expiresAt(Reservation.getExpiresTime(now))
                        .build()
        ).getId();
    }

    private void confirmReservationIsOk(Long id) throws Exception {
        mockMvc.perform(post("/reservations/" + id + "/confirm"))
                .andExpect(status().isOk());
    }

    private void confirmReservationIsBad(Long id) throws Exception {
        mockMvc.perform(post("/reservations/" + id + "/confirm"))
                .andExpect(status().isBadRequest());
    }

    private void cancelReservationIsOk(Long id) throws Exception {
        mockMvc.perform(post("/reservations/" + id + "/cancel"))
                .andExpect(status().isOk());
    }

}
